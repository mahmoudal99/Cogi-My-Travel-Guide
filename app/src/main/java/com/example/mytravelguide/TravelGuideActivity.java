package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mytravelguide.Attractions.ContinentAttractionsActivity;
import com.example.mytravelguide.Models.AttractionObject;
import com.example.mytravelguide.Utils.GooglePlacesApi;
import com.example.mytravelguide.Utils.NearByLocationsAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class TravelGuideActivity extends AppCompatActivity{

    private static final String TAG = "TravelGuideActivity";
    private static final String API_KEY = "AIzaSyDVuZm4ZWwkzJdxeSOFEBWk37srFby2e4Q";
    private final static int FINE_LOCATION = 100;
    static final int AUTOCOMPLETE_REQUEST_CODE = 15;

    RecyclerView listView;
    private RecyclerView.Adapter mAdapter;

    // Widgets
    ImageView backArrow, addPlace, image;
    TextView attractionName;
    ImageView location;

    VisitedActivity visitedActivity;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Variables
    static String result = null;
    Integer responseCode = null;
    String responseMessage = "";

    ArrayList<String> results = new ArrayList<>();

    String searchString, placeName, URL;

    GooglePlacesApi googlePlacesApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_guide);

        requestPermission();
        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
        callSearcEngine();
    }

    private void init(){
        backArrow = findViewById(R.id.backArrow);
        addPlace = findViewById(R.id.addPlace);
        visitedActivity = new VisitedActivity();
        image = findViewById(R.id.attractionImage);
        location = findViewById(R.id.location);
        attractionName = findViewById(R.id.attractionName);
        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);
    }

    private void setUpWidgets(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(TravelGuideActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });

        placeName = "Attraction";
        placeName = getIntent().getStringExtra("AttractionName");

        if(placeName != null){
            attractionName.setText(placeName);
        }else {
            attractionName.setText("Attraction");
        }

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(placeName!=null){
                    addVisitedPlace(placeName);
                }else {
                    Toast.makeText(TravelGuideActivity.this, "No Attraction Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadNearByLocations();
                placePicker();
            }
        });

    }

    private void placePicker(){
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void loadNearByLocations(){
        ArrayList<AttractionObject> nearByLocationsArray = new ArrayList<>();

        listView = (RecyclerView) findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mAdapter = new NearByLocationsAdapter(nearByLocationsArray, TravelGuideActivity.this);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(mAdapter);

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);
        nearByLocationsArray = googlePlacesApi.getNearByLocations(nearByLocationsArray, mAdapter);
    }

    private void addVisitedPlace(String name){
        // Create a new user with a first and last name
        Map<String, String> place = new HashMap<>();
        place.put("Place Name", placeName);
        place.put("Date Visited", "March 2018");
        place.put("URL", URL);

        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new document with a generated ID
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces").add(place)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Log.d(TAG, "DocumentSnapshot added with ID: ");
                    }
                });

    }

    private void callSearcEngine(){

        if(placeName != null){
            searchString = placeName;

            // looking for
            String searchStringNoSpaces = searchString.replace(" ", "+");

            // Your API key
            // TODO replace with your value
            String key = "AIzaSyCyeExKDw-uElEQ8PlvE3G97_9o9djUrDM";

            // Your Search Engine ID
            // TODO replace with your value
            String cx = "000804371853375564055:-lvbacgxzgs";

            String urlString = "https://www.googleapis.com/customsearch/v1?q=" + searchStringNoSpaces + "&key=" + key + "&cx=" + cx + "&searchType=image" + "&alt=json";
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                Log.e("GOOGLESEARCH", "ERROR converting String to URL " + e.toString());
            }
            Log.d("GOOGLESEARCH", "Url = " + urlString);


            // start AsyncTask
            TravelGuideActivity.GoogleSearchAsyncTask searchTask = new TravelGuideActivity.GoogleSearchAsyncTask();
            searchTask.execute(url);
            Log.d("URLC1", url.getPath());
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            Log.d("MYURLC3", urldisplay);
            try {

                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Glide.with(TravelGuideActivity.this).load(result).into(bmImage);

        }
    }

    private class GoogleSearchAsyncTask extends AsyncTask<URL, Integer, String> {

        protected void onPreExecute() {
            Log.d("GOOGLESEARCH", "AsyncTask - onPreExecute");
        }


        @Override
        protected String doInBackground(URL... urls) {

            URL url = urls[0];
            Log.d("GOOGLESEARCH", "AsyncTask - doInBackground, url=" + url);

            // Http connection
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Log.e("GOOGLESEARCH", "Http connection ERROR " + e.toString());
            }


            try {
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
            } catch (IOException e) {
                Log.e("GOOGLESEARCH", "Http getting response code ERROR " + e.toString());
            }

            try {

                if (responseCode == 200) {

                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                        if (line.contains("link")) {
                            results.add(line);
                        }
                    }
                    rd.close();
                    conn.disconnect();
                    result = sb.toString();

                    return result;

                } else {

                    String errorMsg = "Http ERROR response " + responseMessage + "\n" + "Make sure to replace in code your own Google API key and Search Engine ID";
                    Log.e("GOOGLESEARCH", errorMsg);
                    result = errorMsg;
                    return result;

                }
            } catch (IOException e) {
                Log.e("GOOGLESEARCH", "Http Response ERROR " + e.toString());
            }


            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            Log.d("GOOGLESEARCH", "AsyncTask - onProgressUpdate, progress=" + progress);

        }

        protected void onPostExecute(String result) {

            Log.d("GOOGLESEARCHRESULT", "AsyncTask - onPostExecute, result=" + result);

            StringBuffer sb = new StringBuffer(results.get(1).length());
            sb.delete(0, 9);

            String res = sb.toString();
            res.replace(":", "");
            res = results.get(2).substring(12, results.get(2).length() - 1);
            URL = res;
            new TravelGuideActivity.DownloadImageTask((ImageView) findViewById(R.id.attractionImage)).execute(res);
        }
    }


    //---------- Firebase ----------//
    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    Log.d(TAG, "Success");
                } else {
                    Log.d(TAG, "signed out");
                }
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
                    Toast.makeText(TravelGuideActivity.this, "This app requires location permissions to detect your location!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("vision", "Place: " + place.getName() + ", " + place.getId());
                attractionName.setText(place.getName());

                googlePlacesApi.setPhoto(place.getPhotoMetadatas().get(0), image);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}





































