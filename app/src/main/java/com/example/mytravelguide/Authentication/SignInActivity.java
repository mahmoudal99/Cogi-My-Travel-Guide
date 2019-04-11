package com.example.mytravelguide.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // Widgets
    TextView createAccountText;
    Button loginBtn;
    EditText emailET, passwordET;

    Intent intent;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Variables
    String email, password, userID, user_id;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();
        checkUserSignedIn();
        setUpWidgets();
        setUpFirebaseAuthentication();

    }

    private void init() {
        createAccountText = findViewById(R.id.CreateAccountText);
        loginBtn = findViewById(R.id.loginBTN);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        context = SignInActivity.this;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setUpWidgets() {
        createAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccountIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(createAccountIntent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void checkUserSignedIn() {
        if (currentUser != null) {
            Intent intent = new Intent(context, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
    }

    public boolean isStringNull(String string) {
        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    private void signIn() {
        authentication = FirebaseAuth.getInstance();

        email = emailET.getText().toString();
        password = passwordET.getText().toString();

        Toast.makeText(SignInActivity.this, email + " " +  password, Toast.LENGTH_LONG).show();


        if (isStringNull(email) && isStringNull(password)) {
            Toast.makeText(context, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
        } else {

            authentication.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            currentUser = authentication.getCurrentUser();

                            if(!task.isSuccessful()){
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                Toast.makeText(SignInActivity.this, getString(R.string.auth_failed),
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                if (currentUser.isEmailVerified()) {

                                    Log.d(TAG, "onComplete: success. email is verified.");
                                    intent = new Intent(SignInActivity.this, HomePageActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                } else {

                                    Toast.makeText(context, "Email is not verified \n check your email inbox.", Toast.LENGTH_SHORT).show();
                                    authentication.signOut();

                                }
                            }
                        }
                    });
        }

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
