package com.example.mytravelguide.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.BuildConfig;
import com.example.mytravelguide.R;
import com.example.mytravelguide.TravelGuideActivity;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Landmark {

    private static final String API_KEY = BuildConfig.APIKEY;
    private Context context;

    private Map<String, String> placeMap;

    boolean landmarkAdded = false;

    public Landmark(Context context) {
        this.context = context;
        placeMap = new HashMap<>();
    }

    public void setLandmarkAddedTrue() {
        landmarkAdded = true;
    }

    public void addLandmarkToTimeline(FirebaseUser currentUser, String placeID, String landmarkName) {

        if (placeID == null) {
            Toast.makeText(context, "Landmark not added: No Id available", Toast.LENGTH_SHORT).show();
        } else {
            placeMap.put("Place Name", landmarkName);
            placeMap.put("ID", placeID);

            CloudFirestore cloudFirestore = new CloudFirestore(placeMap, currentUser);
            cloudFirestore.addPlace();
            setLandmarkAddedTrue();
            Toast.makeText(context, "Landmark added to timeline", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkLandmarkAlreadyAdded(String landmarkName, String placeID, FirebaseUser currentUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(context.getString(R.string.VisitedPlaces)).document(currentUser.getUid()).collection(context.getString(R.string.MyPlaces))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (Objects.requireNonNull(document.get("Place Name")).toString().equals(landmarkName)) {
                                setLandmarkAddedTrue();
                                Toast.makeText(context, "Landmark Already Added To Timeline", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                    addLandmarkToTimeline(currentUser, placeID, landmarkName);
                });
    }

    public void getLandmarkFromImage(Bitmap bitmap, TextView textView) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionCloudLandmarks -> {
                    textView.setText(firebaseVisionCloudLandmarks.get(0).getLandmark());
                });
    }

    public Intent landmarkPicker() {
        if (!Places.isInitialized()) {
            Places.initialize(context.getApplicationContext(), API_KEY);
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL, Place.Field.PHONE_NUMBER, Place.Field.VIEWPORT,
                Place.Field.WEBSITE_URI, Place.Field.ADDRESS);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(context);
        return intent;
    }
}































