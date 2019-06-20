package com.example.mytravelguide.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mytravelguide.Authentication.SignInActivity;
import com.example.mytravelguide.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth authentication;
    private GoogleSignInClient googleSignInClient;
    private Context context;

    public FirebaseMethods(Context context) {
        this.context = context;
        authentication = FirebaseAuth.getInstance();
    }

    public void registerNewEmail(final String email, String password, final String firstname, final String lastname) {
        authentication.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        FirebaseUser user = authentication.getCurrentUser();
                        String id = Objects.requireNonNull(user).getUid();

//                            addUser(id, firstname, lastname, email);
                        user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null) {
                            authentication = FirebaseAuth.getInstance();
                            authentication.signOut();
                        } else {
                            Log.d(TAG, "NO USER");
                        }
                    }
                });
    }

    public void logout() {
        setUpGooogleSignin();
//        authentication.signOut();
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        googleSignInClient.signOut();
        Intent intent = new Intent(this.context, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.context.startActivity(intent);
    }

    private void setUpGooogleSignin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this.context, gso);
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Success");
                        } else {
                            Log.d(TAG, "Fail");
                        }
                    });
        }
    }


}




















































