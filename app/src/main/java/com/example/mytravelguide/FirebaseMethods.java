package com.example.mytravelguide;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            userID = auth.getCurrentUser().getUid();
        }

    }

    public void registerNewEmail(final String email, String password, final String firstname, final String lastname) {

        auth.createUserWithEmailAndPassword(email, password)

                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override

                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                        } else if (task.isSuccessful()) {
                            //send verificaton email

                            FirebaseUser user = auth.getCurrentUser();
                            String id = user.getUid();

//                            addUser(id, firstname, lastname, email);
                            sendVerificationEmail();
                            user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user != null) {
                                // User is signed in
                                auth = FirebaseAuth.getInstance();
                                auth.signOut();

                            } else {
                                // User is signed out
                                Log.d(TAG, "NO USER");
                            }
                        }
                    }

                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override

                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Success");
                            } else {
                                Log.d(TAG, "Fail");
                            }
                        }

                    });
        }
    }
}




















































