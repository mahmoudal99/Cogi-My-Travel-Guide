package com.example.mytravelguide;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.example.mytravelguide.Attractions.AttractionsActivity;
import com.example.mytravelguide.Settings.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = "HomePageActivity";

    private CardView attractionsCard, travelGuideCard, timelineCard;
    private ImageView settings;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_home_page);

        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
    }

    private void init() {
        attractionsCard = findViewById(R.id.attractionsCard);
        travelGuideCard = findViewById(R.id.travelGuideCard);
        timelineCard = findViewById(R.id.timelineCard);
        settings = findViewById(R.id.settings);
        authentication = FirebaseAuth.getInstance();
    }

    private void setUpWidgets() {
        attractionsCard.setOnClickListener(v -> {
            Intent attractionsIntent = new Intent(HomePageActivity.this, AttractionsActivity.class);
            startActivity(attractionsIntent);
        });

        travelGuideCard.setOnClickListener(v -> {
            Intent travelGuideIntent = new Intent(HomePageActivity.this, TravelGuideActivity.class);
            startActivity(travelGuideIntent);
        });

        timelineCard.setOnClickListener(v -> {
            Intent visitedIntent = new Intent(HomePageActivity.this, VisitedActivity.class);
            startActivity(visitedIntent);
        });

        settings.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, SettingsActivity.class)));
    }

    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        // save data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", lang);
        editor.commit();
    }

    public void loadLocale(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("Language", "");
        Log.d("MAHMOUD", language);
        setLocale(language);
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







































