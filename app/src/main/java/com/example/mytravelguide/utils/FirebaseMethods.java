package com.example.mytravelguide.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mytravelguide.authentication.SignInActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.models.AttractionObject;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
        FirebaseAuth.getInstance().signOut();
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

    public void getLandmarkInformation(String landmarkName, TextView landmarkInformation){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child("Landmarks").orderByChild("placeName").equalTo(landmarkName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        landmarkInformation.setText(issue.child("description").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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




















































