package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.mytravelguide.Attractions.AttractionsActivity;
import com.example.mytravelguide.Authentication.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = "HomePageActivity";

    CardView attractionsCard, travelGuideCard, timelineCard;
    ImageView settings;

    GoogleSignInClient googleSignInClient;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
    }

    private void init(){
        attractionsCard = findViewById(R.id.attractionsCard);
        travelGuideCard = findViewById(R.id.travelGuideCard);
        timelineCard = findViewById(R.id.timelineCard);
        settings = findViewById(R.id.settings);
        authentication = FirebaseAuth.getInstance();
    }

    private void setUpWidgets() {
        attractionsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent attractionsIntent = new Intent(HomePageActivity.this, AttractionsActivity.class);
                startActivity(attractionsIntent);
            }
        });

        travelGuideCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent travelGuideIntent = new Intent(HomePageActivity.this, TravelGuideActivity.class);
                startActivity(travelGuideIntent);
            }
        });

        timelineCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent visitedIntent = new Intent(HomePageActivity.this, VisitedActivity.class);
                startActivity(visitedIntent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startActivity(new Intent(HomePageActivity.this, SettingsActivity.class));
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







































