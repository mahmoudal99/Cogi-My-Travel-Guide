package com.example.mytravelguide.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private String email;

    private ImageView backArrow, changePasswordArrow;
    private LinearLayout logoutLinearLayout;
    private TextView emailTextView;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseMethods firebaseMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        changePasswordArrow = findViewById(R.id.changePasswordArrow);
    }

    private void setupWidgets() {
        backArrow.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, HomePageActivity.class)));
        logoutLinearLayout.setOnClickListener(v -> firebaseMethods.logout());
        emailTextView.setText(email);
        changePasswordArrow.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class)));
    }

    private void getCurrentUserInstance(){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void userInformation(){
        getCurrentUserInstance();
        email = Objects.requireNonNull(currentUser).getEmail();
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
