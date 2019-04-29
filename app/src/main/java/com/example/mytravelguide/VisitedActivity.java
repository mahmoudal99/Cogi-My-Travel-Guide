package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.Utils.GooglePlacesApi;
import com.example.mytravelguide.Utils.TimelineAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.errors.ApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VisitedActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity";
    private static final String API_KEY = "AIzaSyDVuZm4ZWwkzJdxeSOFEBWk37srFby2e4Q";

    // Widgets
    private ImageView backArrow;
    private RecyclerView listView;

    Context context;

    private VisitedPlaceObject landmark;
    private PlacesClient placesClient;
    PhotoMetadata photoMetadata;

    private ArrayList<VisitedPlaceObject> landmarksList;
    private RecyclerView.Adapter mAdapter;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited);

        init();
        setUpListView();
        setUpWidgets();
        setUpFirebaseAuthentication();
        setUpTimeline();
    }

    private void init() {
        backArrow = findViewById(R.id.backArrow);
        context = VisitedActivity.this;
        listView = findViewById(R.id.list);
        Places.initialize(context, API_KEY);
        placesClient = Places.createClient(context);
        landmarksList = new ArrayList<>();
        mAdapter = new TimelineAdapter(landmarksList, VisitedActivity.this);
        listView = (RecyclerView) findViewById(R.id.list);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setUpWidgets() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(VisitedActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });

    }

    private void setUpListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(mAdapter);
    }

    private void setUpTimeline() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS);

                                String id = Objects.requireNonNull(document.get("ID")).toString();
                                FetchPlaceRequest request = FetchPlaceRequest.builder(id, placeFields).build();
                                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                                    Place place = response.getPlace();
                                    callAdapter(document.get("Place Name").toString(), document.get("Date Visited").toString(), place.getPhotoMetadatas().get(0));
                                }).addOnFailureListener((exception) -> {
                                    if (exception instanceof ApiException) {
                                        ApiException apiException = (ApiException) exception;
                                    }
                                });


                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void callAdapter(String name, String dateVisited, PhotoMetadata photoMetadata){
        landmark = new VisitedPlaceObject();
        landmark.placeName = name;
        landmark.dateVisited = dateVisited;
        landmark.photoMetadata = photoMetadata;
        landmarksList.add(landmark);
        mAdapter.notifyDataSetChanged();
    }

    //---------- Firebase ----------//
    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    Log.d(TAG, "Success");
                } else {
                    Log.d(TAG, "signed out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        authentication.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            authentication.removeAuthStateListener(authStateListener);
        }
    }
}




























