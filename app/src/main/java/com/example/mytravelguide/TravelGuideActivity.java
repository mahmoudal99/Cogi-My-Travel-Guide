package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mytravelguide.Attractions.ContinentAttractionsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelGuideActivity extends AppCompatActivity{

    private static final String TAG = "TravelGuideActivity";

    // Widgets
    ImageView backArrow, addPlace, image;
    TextView attractionName;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_guide);

        placeName = getIntent().getStringExtra("AttractionName");

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
        attractionName = findViewById(R.id.attractionName);
    }

    private void setUpWidgets(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(TravelGuideActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });

        if(!placeName.isEmpty()){
            attractionName.setText(placeName);
        }

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVisitedPlace("Paris");
            }
        });

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

        if(!placeName.isEmpty()){
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
}





































