package com.example.mytravelguide.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mytravelguide.Utils.FirebaseMethods;
import com.example.mytravelguide.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    // Variables
    private String firstname, surname, email;

    // Widgets
    private TextView alreadyHaveAccount;
    private EditText firstnameET, surnameET, emailET, passwordET;
    private Button signUpBTN;

    private Context context;

    //firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        setUpFirebaseAuthentication();
        setUpWidget();
    }

    private void init() {
        alreadyHaveAccount = findViewById(R.id.AlreadyAccountText);
        firebaseMethods = new FirebaseMethods(context);
        firstnameET = findViewById(R.id.firstnameET);
        surnameET = findViewById(R.id.surnameET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        signUpBTN = findViewById(R.id.signUpBTN);
        context = SignUpActivity.this;
    }

    private void setUpWidget() {
        alreadyHaveAccount.setOnClickListener(v -> {
            Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(signInIntent);
        });

        signUpBTN.setOnClickListener(v -> SignUp());
    }

    private void SignUp() {
        email = emailET.getText().toString();
        firstname = firstnameET.getText().toString();
        surname = surnameET.getText().toString();

        if (passwordET.getText().toString().length() < 8 && !isValidPassword(passwordET.getText().toString())) {
            invalidPassword();
        } else {
            registerUser();
            signInPage();
        }
    }

    private void invalidPassword() {
        Toast.makeText(context,
                "Password must be over 8 characters & contain atleast\n one lowercase, one uppercase, one number & a special character (@!#$)",
                Toast.LENGTH_SHORT).show();

        passwordET.getText().clear();
    }

    private void signInPage() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void registerUser() {
        String password = passwordET.getText().toString();
        firebaseMethods.registerNewEmail(email, password, firstname, surname);
        Toast.makeText(context, "Check your inbox", Toast.LENGTH_SHORT).show();
        clearWidgets();
    }

    private void clearWidgets() {

        emailET.getText().clear();
        passwordET.getText().clear();
        firstnameET.getText().clear();
        surnameET.getText().clear();
    }

    public static boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "[a-zA-Z0-9\\!\\@\\#\\$]{8,24}";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    //---------- Firebase ----------//
    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                Log.d(TAG, "Connected");

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
