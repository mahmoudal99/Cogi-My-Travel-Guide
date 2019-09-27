package com.cogi.mytravelguide.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.Toast;

import com.cogi.mytravelguide.BuildConfig;
import com.cogi.mytravelguide.R;
import com.cogi.mytravelguide.TravelGuideActivity;
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

    public Landmark(Context context) {
        this.context = context;
        placeMap = new HashMap<>();
    }

    private void addLandmarkToTimeline(FirebaseUser currentUser, String placeID, String landmarkName, String dateVisited) {
        if (placeID == null) {
            Toast.makeText(context, "Landmark not added: No Id available", Toast.LENGTH_SHORT).show();
        } else {
            placeMap.put("Place Name", landmarkName);
            placeMap.put("Date Visited", dateVisited);
            placeMap.put("ID", placeID);

            CloudFirestore cloudFirestore = new CloudFirestore(placeMap, currentUser);
            cloudFirestore.addPlace();
            Toast.makeText(context, "Landmark added to timeline", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkLandmarkAlreadyAdded(String landmarkName, String placeID, FirebaseUser currentUser, String dateVisited) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(context.getString(R.string.VisitedPlaces)).document(currentUser.getUid()).collection(context.getString(R.string.MyPlaces))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (Objects.requireNonNull(document.get("Place Name")).toString().equals(landmarkName)) {
                                Toast.makeText(context, "Landmark Already Added To Timeline", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                    addLandmarkToTimeline(currentUser, placeID, landmarkName, dateVisited);
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
                Place.Field.WEBSITE_URI, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        return new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(context);
    }
}































