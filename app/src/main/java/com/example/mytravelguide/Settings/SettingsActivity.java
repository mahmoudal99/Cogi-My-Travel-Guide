package com.example.mytravelguide.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private String email;

    private ImageView backArrow, languageArrow;
    private LinearLayout logoutLinearLayout;
    private TextView emailTextView;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseMethods firebaseMethods;

    private String[] Languages = {"English", "French", "Arabic"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings);

        init();
        userInformation();
        setupWidgets();
        setUpFirebaseAuthentication();
    }

    private void init() {
        backArrow = findViewById(R.id.backArrow);
        logoutLinearLayout = findViewById(R.id.logoutLayout);
        firebaseMethods = new FirebaseMethods(SettingsActivity.this);
        emailTextView = findViewById(R.id.emailTextView);
        languageArrow = findViewById(R.id.languageArrow);
    }

    private void setupWidgets() {
        backArrow.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, HomePageActivity.class)));
        logoutLinearLayout.setOnClickListener(v -> firebaseMethods.logout());
        emailTextView.setText(email);
        languageArrow.setOnClickListener(v -> showLanguageOptions());
    }

    private void getCurrentUserInstance() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void userInformation() {
        getCurrentUserInstance();
        email = Objects.requireNonNull(currentUser).getEmail();
    }

    private void showLanguageOptions() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Choose Language....");
        alertDialog.setSingleChoiceItems(Languages, -1, (dialog, which) -> {
            if (which == 0) {
                setLocale("en");
                recreate();
            } else if (which == 1) {
                setLocale("fr");
                recreate();
            }else if(which == 2){
                setLocale("ar");
                recreate();
            }

            dialog.dismiss();
        });

        AlertDialog dialog = alertDialog.create();
        dialog.show();
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
                Log.d("Firebase Authentication", "Success");
            } else {
                Log.d("Firebase Authentication", "signed out");
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



































