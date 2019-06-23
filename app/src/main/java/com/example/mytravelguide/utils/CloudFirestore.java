package com.example.mytravelguide.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.Objects;

public class CloudFirestore {

    private Map<String, String> placeMap;
    private Map<String, Bitmap> placeImageMap;
    private FirebaseUser currentUser;

    public CloudFirestore(Map<String, String> placeMap, FirebaseUser currentUser) {
        this.placeMap = placeMap;
        this.currentUser = currentUser;
    }

    public void addPlace() {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new document with a generated ID
        db.collection("VisitedPlaces")
                .document(currentUser.getUid())
                .collection("MyPlaces").add(placeMap)
                .addOnCompleteListener(task -> {
                });
    }

    private void addImage() {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String name = Objects.requireNonNull(document.get("Place Name")).toString();
                            if (name.equals(placeMap.get("Place Name"))) {
                                db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                                        .document(document.getId())
                                        .collection("Place Image")
                                        .add(placeImageMap)
                                        .addOnCompleteListener(task1 -> Log.d("Firestore", "DocumentSnapshot added with ID: "));
                            }
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }
}
