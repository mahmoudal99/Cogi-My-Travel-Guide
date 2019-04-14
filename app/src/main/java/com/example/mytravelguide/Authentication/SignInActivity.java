package com.example.mytravelguide.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.Utils.FirebaseMethods;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private static final int RC_SIGN_IN = 1;

    // Widgets
    TextView createAccountText;
    Button loginBtn;
    EditText emailET, passwordET;
    SignInButton googleSignInButton;

    Intent intent;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseMethods firebaseMethods;

    // Variables
    String email, password, userID, user_id;

    Context context;

    // Google
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    AuthCredential credential, emailCredentials;

    // Shared Preferences
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Shared Preferences
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        init();
        setUpWidgets();
        setUpFirebaseAuthentication();

    }

    private void init() {
        createAccountText = findViewById(R.id.CreateAccountText);
        loginBtn = findViewById(R.id.loginBTN);
        googleSignInButton = findViewById(R.id.sign_in_button);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        context = SignInActivity.this;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        account = GoogleSignIn.getLastSignedInAccount(this);
        firebaseMethods = new FirebaseMethods(context);
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


        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
    }

    public boolean isStringNull(String string) {
        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    // Email & Password Sign In
    private void signIn() {
        authentication = FirebaseAuth.getInstance();

        email = emailET.getText().toString();
        password = passwordET.getText().toString();

        if (isStringNull(email) && isStringNull(password)) {
            Toast.makeText(context, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
        } else {

            authentication.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            currentUser = authentication.getCurrentUser();

                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                checkUserExists(email, password);
                            } else {
                                Log.d(TAG, "onComplete: success. email is verified.");
                                intent = new Intent(SignInActivity.this, HomePageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                saveEmailCredntials(email, password);
                                linkGoogleCredentials(email);
                                finish();
                            }
                        }
                    });
        }
    }

    // Google Sign In
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            saveGoogleCredential(acct.getEmail(), acct.getIdToken());
                            openHomePage();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void openHomePage() {
        startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
    }

    // Authentication Credentials
    private void saveGoogleCredential(String email, String token) {
        editor.putString(email + "Email", email); // Storing string
        editor.putString(email + "Token", token); // Storing string
        editor.apply();
    }

    private void saveEmailCredntials(String email, String password) {
        editor.putString(email + "Email", email); // Storing string
        editor.putString(email + "Password", password); // Storing string
        editor.apply();
    }

    private void linkGoogleCredentials(final String email) {
        if (pref.contains(email + "Token")) {

            deleteUser();
            String token = pref.getString(email + "Token", "");
            credential = GoogleAuthProvider.getCredential(token, null);
            authentication.signInWithCredential(credential);
            authentication.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            linkEmailCredentials(email);
                        }
                    });
        }
    }

    private void linkEmailCredentials(String email) {
        if (pref.contains(email + "Email")) {

            String newEmail = pref.getString(email + "Email", "");
            String newPassword = pref.getString(email + "Password", "");
            credential = EmailAuthProvider.getCredential(newEmail, newPassword);

            authentication.signInWithCredential(credential);

            authentication.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "linkWithCredential:success");
                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                            }
                        }
                    });
        }
    }

    private void checkUserExists(String email, String password) {
        if (pref.contains(email + "Token")) {
            firebaseMethods.registerNewEmail(email, password, "Mahmoud", "Almahroum");
            // Intent
            intent = new Intent(SignInActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignInActivity.this, getString(R.string.auth_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
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
                    startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
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
