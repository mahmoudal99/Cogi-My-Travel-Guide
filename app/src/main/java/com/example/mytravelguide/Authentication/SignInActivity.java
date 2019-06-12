package com.example.mytravelguide.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private static final int SIGN_IN = 1;

    // Widgets
    private TextView createAccountText;
    private Button loginBtn;
    private EditText emailET, passwordET;
    private SignInButton googleSignInButton;

    private Intent intent;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseMethods firebaseMethods;

    // Variables
    private String email, password;

    private Context context;

    // Google
    private GoogleSignInClient mGoogleSignInClient;
    private AuthCredential credential;

    // Shared Preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Shared Preferences
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        editor.apply();

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
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        firebaseMethods = new FirebaseMethods(context);
    }

    private void setUpWidgets() {
        createAccountText.setOnClickListener(v -> {
            Intent createAccountIntent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(createAccountIntent);
        });

        loginBtn.setOnClickListener(v -> signIn());
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(v -> googleSignIn());
    }

    public boolean isStringNull(String string) {
        return string.equals("");
    }

    /*---------------------------------------------------------------------- Email & Password Sign In ----------------------------------------------------------------------*/
    private void signIn() {
        authentication = FirebaseAuth.getInstance();

        email = emailET.getText().toString();
        password = passwordET.getText().toString();

        if (isStringNull(email) && isStringNull(password)) {
            Toast.makeText(context, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
        } else {
            authentication.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignInActivity.this, task -> {
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
                    });
        }
    }

    /*---------------------------------------------------------------------- Google Sign In ----------------------------------------------------------------------*/
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(Objects.requireNonNull(account));
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveGoogleCredential(acct.getEmail(), acct.getIdToken());
                        openHomePage();
                    } else {
                        Toast.makeText(context, "Sign In Failed, Please Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openHomePage() {
        intent = new Intent(SignInActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /*---------------------------------------------------------------------- Authentication Credentials ----------------------------------------------------------------------*/

    private void saveGoogleCredential(String email, String token) {
        editor.putString(email + "Email", email);
        editor.putString(email + "Token", token);
        editor.apply();
    }

    private void saveEmailCredntials(String email, String password) {
        editor.putString(email + "Email", email);
        editor.putString(email + "Password", password);
        editor.apply();
    }

    /*---------------------------------------------------------------------- Link Credentials ----------------------------------------------------------------------*/

    private void linkGoogleCredentials(final String email) {
        if (pref.contains(email + "Token")) {
            deleteUser();
            String token = pref.getString(email + "Token", "");
            credential = GoogleAuthProvider.getCredential(token, null);
            authentication.signInWithCredential(credential);
            authentication.signInWithCredential(credential)
                    .addOnCompleteListener(task -> linkEmailCredentials(email));
        }
    }

    private void linkEmailCredentials(String email) {
        if (pref.contains(email + "Email")) {

            String newEmail = pref.getString(email + "Email", "");
            String newPassword = pref.getString(email + "Password", "");
            credential = EmailAuthProvider.getCredential(Objects.requireNonNull(newEmail), Objects.requireNonNull(newPassword));

            authentication.signInWithCredential(credential);

            Objects.requireNonNull(authentication.getCurrentUser()).linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                        }
                    });
        }
    }

    private void checkUserExists(String email, String password) {
        if (pref.contains(email + "Token")) {
            firebaseMethods.registerNewEmail(email, password, "Mahmoud", "Almahroum");
            intent = new Intent(SignInActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignInActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Objects.requireNonNull(user).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User account deleted.");
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
