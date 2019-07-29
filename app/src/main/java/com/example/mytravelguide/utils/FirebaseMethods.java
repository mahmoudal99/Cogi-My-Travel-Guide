package com.example.mytravelguide.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import com.example.mytravelguide.authentication.SignInActivity;
import com.example.mytravelguide.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FirebaseMethods {

    private GoogleSignInClient googleSignInClient;
    private Context context;

    public FirebaseMethods(Context context) {
        this.context = context;
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

    public void getLandmarkInformation(String landmarkName, TextView landmarkInformation) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("Landmarks").orderByChild("placeName").equalTo(landmarkName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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

}




















































