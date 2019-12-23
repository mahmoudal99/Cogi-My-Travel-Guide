package com.cogi.mytravelguide;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cogi.mytravelguide.utils.FirebaseMethods;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "TravelGuideActivity";
    private ImageView backArrow;
    private LinearLayout logoutLinearLayout;
    private RelativeLayout languageRelativeLayout;
    private TextView emailTextView, languageTextView, logoutTextView, logoutTitle;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseMethods firebaseMethods;

    private String[] Languages = {"English", "French", "Spanish"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings);

        init();
        setupWidgets();
        setUpFirebaseAuthentication();
    }

    private void init() {
        backArrow = findViewById(R.id.backArrow);
        logoutLinearLayout = findViewById(R.id.logoutLayout);
        firebaseMethods = new FirebaseMethods(SettingsActivity.this);
        emailTextView = findViewById(R.id.emailTextView);
        languageTextView = findViewById(R.id.languageTextView);
        logoutTextView = findViewById(R.id.logoutTextView);
        logoutTitle = findViewById(R.id.logoutTitle);
        setLanguageTextView();
        languageRelativeLayout = findViewById(R.id.languageLayout);
    }

    private void setLanguageTextView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("Language", "");
        if (language.contains("en")) {
            languageTextView.setText("English");
        } else if (language.contains("es")) {
            languageTextView.setText("Spanish");
        } else if (language.contains("fr")) {
            languageTextView.setText("French");
        }
    }

    private void setupWidgets() {
        backArrow.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, HomePageActivity.class)));
        logoutLinearLayout.setOnClickListener(v -> firebaseMethods.logout());
        authentication = FirebaseAuth.getInstance();
        if (!isSignedIn()) {
            Log.d(TAG, "No user signed in");
        } else {
            emailTextView.setText(authentication.getCurrentUser().getProviderData().get(1).getEmail());
        }
        languageRelativeLayout.setOnClickListener(v -> showLanguageOptions());
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
            } else if (which == 2) {
                setLocale("es");
                recreate();
            }

            dialog.dismiss();
        });

        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        // save data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("Language", "");
        setLocale(language);
    }

    //---------- Firebase ----------//
    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d("Firebase Auth" + "GS", "Success" + currentUser.getEmail());
            } else {
                Log.d("Firebase Authentication", "signed out");
                logoutTextView.setText(getResources().getString(R.string.sign_in));
                logoutTitle.setText(getResources().getString(R.string.sign_in_capital));
            }
        };
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(SettingsActivity.this) != null;
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



































