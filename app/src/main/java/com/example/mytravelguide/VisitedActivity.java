package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.Utils.TimelineAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class VisitedActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity";

    ImageView backArrow;
    ListView listView;
    ImageView addPlace;

    // Variables
    ArrayList<VisitedPlaceObject> places;
    String placeName, date;
    Context context;

    VisitedPlaceObject model;

    TimelineAdapter timelineAdapter;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited);

        init();
        setUpWidgets();
        setUpFirebaseAuthentication();

    }

    private void init(){
        backArrow = findViewById(R.id.backArrow);
        context = VisitedActivity.this;
        addPlace = findViewById(R.id.addPlace);
        listView = findViewById(R.id.list);
    }

    private void setUpWidgets(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(VisitedActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceToTimeline();
            }
        });
    }

    /// Time Line

    public void addPlaceToTimeline(){

        // Array List
        places = new ArrayList<VisitedPlaceObject>();
        // Adapter
        timelineAdapter = new TimelineAdapter(VisitedActivity.this, places, placeName, date);

        // Set Adapater
        listView = findViewById(R.id.list);
        listView.setAdapter(timelineAdapter);

        // Notify Data
        timelineAdapter.notifyDataSetChanged();

        // Read Data from Database
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GOT IT", document.getId() + " => " + document.getData());
                                model = new VisitedPlaceObject();
                                model.placeName = document.get("Place Name").toString();
                                model.dateVisited = document.get("Date Visited").toString();


                                places.add(model);
                                timelineAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

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




























