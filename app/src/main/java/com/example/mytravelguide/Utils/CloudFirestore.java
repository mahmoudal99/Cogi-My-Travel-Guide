package com.example.mytravelguide.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class CloudFirestore {

    Map<String, String> placeMap;
    Map<String, PhotoMetadata> placeImageMap;
    private FirebaseUser currentUser;

    public CloudFirestore(Map<String, String> placeMap, Map<String, PhotoMetadata> placeImageMap, FirebaseUser currentUser) {
        this.placeMap = placeMap;
        this.placeImageMap = placeImageMap;
        this.currentUser = currentUser;
    }

    public void addPlace() {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new document with a generated ID
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces").add(placeMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        addImage();
                    }
                });
    }

    private void addImage() {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GOT IT", document.getId() + " => " + document.getData());

                                String name = document.get("Place Name").toString();
                                if (name.equals(placeMap.get("Place Name"))) {
                                    db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces").document(document.getId()).collection("Place Image").add(placeImageMap)
                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    Log.d("Firestore", "DocumentSnapshot added with ID: ");
                                                }
                                            });
                                }
                            }
                        } else {
                            Log.w("Firestore", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
