package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.maps.errors.ApiException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class CameraActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String API_KEY = "AIzaSyDVuZm4ZWwkzJdxeSOFEBWk37srFby2e4Q";
    private final static int FINE_LOCATION = 100;
    private final static int PLACE_PICKER_REQUEST = 5;

    String placeId;

    CardView choosePhotoCard, cameraCard;
    Context context;

    ImageView imageView;

    ArrayList<String> nearByPlaces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        init();
        setUpWidgets();
    }

    private void init() {
        choosePhotoCard = findViewById(R.id.choosePhotoCard);
        cameraCard = findViewById(R.id.cameraCard);
        context = CameraActivity.this;
        imageView = findViewById(R.id.image);
    }

    private void setUpWidgets() {
        choosePhotoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Places.initialize(getApplicationContext(), API_KEY);
//                PlacesClient placesClient = Places.createClient(CameraActivity.this);
//
//                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.PHOTO_METADATAS);
//                // Use the builder to create a FindCurrentPlaceRequest.
//                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();
//                if (ContextCompat.checkSelfPermission(CameraActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//                    placesClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {
////                        PhotoMetadata photoMetadata = response.getPlaceLikelihoods().get(0).getPlace().getPhotoMetadatas().get(0);
////                        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
////                                .setMaxWidth(500) // Optional.
////                                .setMaxHeight(300) // Optional.
////                                .build();
////
////                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
////                            Bitmap bitmap = fetchPhotoResponse.getBitmap();
////                            imageView.setImageBitmap(bitmap);
////                        }).addOnFailureListener((exception) -> {
////                            if (exception instanceof ApiException) {
////                                ApiException apiException = (ApiException) exception;
////                                // Handle error with given status code.
////                                Log.e("MAHMOUD", "Place not found: " + exception.getMessage());
////                            }
////                        });
//
//                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
//                            Log.i("VISION", String.format("Place '%s' has likelihood: %f", placeLikelihood.getPlace().getName(), placeLikelihood.getLikelihood()));
//                            Log.d("HELLOPLACE", response.getPlaceLikelihoods().get(0).getPlace().getId());
//                            placeId = response.getPlaceLikelihoods().get(0).getPlace().getId();
//                        }
//
//                    })).addOnFailureListener((exception) -> {
//                        if (exception instanceof ApiException) {
//                            ApiException apiException = (ApiException) exception;
//
//                        }
//                    });
//                } else {
//                    // A local method to request required permissions;
//                    // See https://developer.android.com/training/permissions/requesting
//                    requestPermission();
//                }
            }
        });

        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void requestPermission() {

        //Check whether our app has the fine location permission, and request it if necessary//

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to detect your location!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void getLandmark(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        Log.d("VISION", firebaseVisionCloudLandmarks.get(0).getLandmark());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                getLandmark(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            getLandmark(imageBitmap);

        } if (requestCode == PLACE_PICKER_REQUEST) {

        }
    }


}
