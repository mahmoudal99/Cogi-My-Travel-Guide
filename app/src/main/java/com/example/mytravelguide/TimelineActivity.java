package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.mytravelguide.models.VisitedPlaceObject;
import com.example.mytravelguide.utils.TimelineAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.errors.ApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TimelineActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity";
    private static final String API_KEY = BuildConfig.APIKEY;

    // Widgets
    private ImageView backArrow;
    private RecyclerView listView;

    Context context;

    private PlacesClient placesClient;

    private ArrayList<VisitedPlaceObject> landmarksList;
    private RecyclerView.Adapter timelineAdapter;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        init();
        setUpListView();
        setUpWidgets();
        setUpFirebaseAuthentication();
        setUpTimeline();
    }

    private void init() {
        backArrow = findViewById(R.id.backArrow);
        context = TimelineActivity.this;
        listView = findViewById(R.id.list);
        Places.initialize(context, API_KEY);
        placesClient = Places.createClient(context);
        landmarksList = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(landmarksList, TimelineActivity.this);
        listView = findViewById(R.id.list);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setUpWidgets() {
        backArrow.setOnClickListener(v -> {
            Intent backIntent = new Intent(TimelineActivity.this, HomePageActivity.class);
            startActivity(backIntent);
        });
    }

    private void setUpListView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(timelineAdapter);
    }

    private void setUpTimeline() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS);
                            Log.d("WHAT", document.toString());
                            if (document != null) {
                                if (document.get("ID") != null) {
                                    String landmarkID = Objects.requireNonNull(document.get("ID")).toString();
                                    FetchPlaceRequest request = FetchPlaceRequest.builder(landmarkID, placeFields).build();

                                    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                                        Place place = response.getPlace();
                                        Log.d("MOMOMOM", place.getName());
                                        callAdapter(Objects.requireNonNull(document.get("Place Name")).toString(), document.get("Date Visited").toString(), Objects.requireNonNull(place.getPhotoMetadatas()).get(0));

                                    }).addOnFailureListener((exception) -> {
                                        if (exception instanceof ApiException) {
                                            ApiException apiException = (ApiException) exception;
                                            Log.e(TAG, apiException.getMessage());
                                        }
                                    });
                                }
                            }

                        }

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void callAdapter(String name, String dateVisited, PhotoMetadata photoMetadata) {
        VisitedPlaceObject landmark = new VisitedPlaceObject();
        landmark.placeName = name;
        landmark.photoMetadata = photoMetadata;
        landmark.dateVisited = dateVisited;
        landmarksList.add(landmark);
        timelineAdapter.notifyDataSetChanged();
    }

    //---------- Firebase ----------//
    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "Success");
            } else {
                Log.d(TAG, "signed out");
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




























