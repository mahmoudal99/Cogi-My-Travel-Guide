package com.example.mytravelguide.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.L;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private static final int SIGN_IN = 1;

    // Widgets
    private SignInButton googleSignInButton;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private Context context;

    // Facebook
    private CallbackManager callbackManager;
    private LoginButton loginButton;

    // Google
    private GoogleSignInClient mGoogleSignInClient;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Shared Preferences
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        editor.apply();


        initGoogleAuth();
        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
    }

    private void init() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        googleSignInButton = findViewById(R.id.sign_in_button);
        context = SignInActivity.this;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initGoogleAuth() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setUpWidgets() {
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(v -> googleSignIn());

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
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

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveGoogleCredentials(acct.getIdToken(), acct.getEmail());
                        linkGoogleToFacebook(acct.getEmail());
                    } else {
                        Toast.makeText(context, "Sign In Failed, Please Try Again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*---------------------------------------------------------------------- Facebook Sign In ----------------------------------------------------------------------*/

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveFacebookCredentials(token.getToken());
                        String googleToken = pref.getString(getFacebookUserEmail() + "Google", "0");
                        checkTokenExists(googleToken);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*---------------------------------------------------------------------- Link Credentials ----------------------------------------------------------------------*/

    private void saveFacebookCredentials(String token) {

        authentication = FirebaseAuth.getInstance();
        FirebaseUser user = authentication.getCurrentUser();

        if (user != null) {
            for (UserInfo userInfo : user.getProviderData()) {
                Toast.makeText(SignInActivity.this, userInfo.getProviderId(), Toast.LENGTH_SHORT).show();
                if (userInfo.getProviderId().contains("facebook.com")) {
                    editor.putString(userInfo.getEmail() + "Facebook", token);
                    editor.apply();
                }
            }
        }
    }

    private void saveGoogleCredentials(String token, String email) {
        editor.putString(email + "Google", token);
        editor.apply();
    }

    private void checkTokenExists(String token){
        if (token.equals("0")) {
            Log.d(TAG, "No google token saved");
        } else {
            String isLinked = pref.getString(getFacebookUserEmail() + "Linked", "NotLinked");
            if (isLinked.equals("NotLinked")) {
                Toast.makeText(this, "Accounts not linked", Toast.LENGTH_SHORT).show();
                deleteUser();
            } else {
                Toast.makeText(this, "Accounts already linked", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void linkGoogleToFacebook(String email) {
        authentication = FirebaseAuth.getInstance();
        FirebaseUser user = authentication.getCurrentUser();

        String token = pref.getString(email + "Facebook", "");
        if(!token.equals("")){
            AuthCredential credential = FacebookAuthProvider.getCredential(token);
            Toast.makeText(this, token + email + credential.getProvider(), Toast.LENGTH_LONG).show();
            user.linkWithCredential(credential)
                    .addOnCompleteListener(this,
                            task -> {
                                if (!task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                                } else {
                                    editor.putString(getFacebookUserEmail() + "Linked", "Linked");
                                    editor.apply();
                                }
                            });
        }

    }

    private String getFacebookUserEmail() {
        authentication = FirebaseAuth.getInstance();
        FirebaseUser user = authentication.getCurrentUser();

        if (user != null) {
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().equals("facebook.com")) {
                    return userInfo.getEmail();
                }
            }
        }
        return "Email";
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
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "Success");
                startActivity(new Intent(SignInActivity.this, HomePageActivity.class));
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
