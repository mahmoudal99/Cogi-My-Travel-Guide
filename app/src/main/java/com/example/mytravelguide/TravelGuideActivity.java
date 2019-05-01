package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mytravelguide.Attractions.ContinentAttractionsActivity;
import com.example.mytravelguide.Models.AttractionObject;
import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.Utils.CloudFirestore;
import com.example.mytravelguide.Utils.GooglePlacesApi;
import com.example.mytravelguide.Utils.GoogleSearch;
import com.example.mytravelguide.Utils.ImagePicker;
import com.example.mytravelguide.Utils.NearByLocationsAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TimeOfWeek;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class TravelGuideActivity extends AppCompatActivity {

    private static final String TAG = "TravelGuideActivity";
    private static final String API_KEY = "AIzaSyDVuZm4ZWwkzJdxeSOFEBWk37srFby2e4Q";
    private final static int FINE_LOCATION = 100;
    static final int AUTOCOMPLETE_REQUEST_CODE = 15;
    public static final int PICK_IMAGE = 1;
    private static final String encoding = "UTF-8";

    // Widgets
    private ImageView backArrow, addLandmark, landmarkImage, searchLandmarkButton;
    private TextView landmarkName, landmarkOpeningHours, landmarkPrice, landmarkRating, landmarkInformation;
    private ImageView nearByLocationButton, chooseImageButton, expandInformation, expandAboutImage, nearByImage;
    private CardView informationCard;
    private LinearLayout informationLayout;

    // Variables
    static String result = null;
    private Integer responseCode = null;
    private String responseMessage = "";
    private String searchString, landmarkNameString, landmarkInformationResult, placeID;

    private ArrayList<String> results = new ArrayList<>();
    private RecyclerView listView;
    private Map<String, String> placeMap;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private CloudFirestore cloudFirestore;

    // Google
    private GooglePlacesApi googlePlacesApi;
    private Place place;

    boolean expandInfo = true;
    boolean expandAbout = true;
    boolean expandNearBy = true;
    boolean landmarkAdded = false;

    Context context;

    // Shared Preference
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_guide);

        requestPermission();
        init();
        setUpWidgets();
        setUpLinearLayout();
        setUpFirebaseAuthentication();
    }

    private void init() {
        context = TravelGuideActivity.this;

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.apply();

        backArrow = findViewById(R.id.backArrow);
        nearByLocationButton = findViewById(R.id.location);
        addLandmark = findViewById(R.id.addPlace);
        searchLandmarkButton = findViewById(R.id.search);
        chooseImageButton = findViewById(R.id.gallery);

        landmarkImage = findViewById(R.id.attractionImage);
        landmarkName = findViewById(R.id.attractionName);
        landmarkOpeningHours = findViewById(R.id.openingHours);
        landmarkRating = findViewById(R.id.rating);
        landmarkPrice = findViewById(R.id.price);
        landmarkInformationResult = "";
        landmarkInformation = findViewById(R.id.landmarkInformation);

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);

        informationCard = findViewById(R.id.infoCard);

        informationLayout = findViewById(R.id.informationList);
        expandInformation = findViewById(R.id.expandInformation);
        expandAboutImage = findViewById(R.id.expandAbout);
        nearByImage = findViewById(R.id.expandNearByImage);

        placeMap = new HashMap<>();
    }

    private void setUpWidgets() {
        backArrow.setOnClickListener(v -> {
            Intent backIntent = new Intent(TravelGuideActivity.this, HomePageActivity.class);
            startActivity(backIntent);
        });

        landmarkNameString = "Landmark";
        landmarkNameString = getIntent().getStringExtra("AttractionName");
        landmarkInformation.setText(landmarkInformationResult);

        if (landmarkNameString != null) {
            landmarkName.setText(landmarkNameString);
        } else {
            landmarkName.setText(getString(R.string.Attraction));
        }

        addLandmark.setOnClickListener(v -> {

            if (landmarkNameString != null) {
                checkExistingPlaces(landmarkNameString);
            } else {
                Toast.makeText(TravelGuideActivity.this, "No Landmark Selected", Toast.LENGTH_SHORT).show();
            }

        });

        nearByLocationButton.setOnClickListener(v -> loadNearByLocations());

        chooseImageButton.setOnClickListener(v -> openGallery());

        searchLandmarkButton.setOnClickListener(v -> placePicker());

        expandInformation.setOnClickListener(v -> {

            if (expandInfo) {
                LinearLayout layout = findViewById(R.id.informationList);
                openList(layout);
                expandInfo = false;
            } else if (!expandInfo) {
                LinearLayout layout = findViewById(R.id.informationList);
                closeList(layout);
                expandInfo = true;
            }
        });

        expandAboutImage.setOnClickListener(v -> {

            if (expandAbout) {
                LinearLayout layout = findViewById(R.id.aboutList);
                openList(layout);
                expandAbout = false;
            } else if (!expandAbout) {
                LinearLayout layout = findViewById(R.id.aboutList);
                closeList(layout);
                expandAbout = true;
            }
        });

        nearByImage.setOnClickListener(v -> {

            if (expandNearBy) {
                LinearLayout layout = findViewById(R.id.nearByLocationsList);
                openList(layout);
                expandNearBy = false;
            } else if (!expandNearBy) {
                LinearLayout layout = findViewById(R.id.nearByLocationsList);
                closeList(layout);
                expandNearBy = true;
            }
        });

    }

    private void checkExistingPlaces(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("VisitedPlaces").document(currentUser.getUid()).collection("MyPlaces")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (document.get("Place Name").toString().equals(name)) {
                                setAddedTrue();
                                Toast.makeText(TravelGuideActivity.this, "Landmark Already Added To Timeline", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }

                    addVisitedPlace();
                });
    }

    public void setAddedTrue() {
        landmarkAdded = true;
    }

    private void setUpLinearLayout() {
        LinearLayout infoLayout = findViewById(R.id.informationList);
        closeList(infoLayout);

        LinearLayout aboutLayout = findViewById(R.id.aboutList);
        closeList(aboutLayout);

        LinearLayout nearByLayout = findViewById(R.id.nearByLocationsList);
        closeList(nearByLayout);
    }

    /*---------------------------------------------------------------------- Features ----------------------------------------------------------------------*/

    private void placePicker() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS,
                Place.Field.PRICE_LEVEL, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void loadNearByLocations() {
        ArrayList<AttractionObject> nearByLocationsArray = new ArrayList<>();

        listView = findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView.Adapter mAdapter = new NearByLocationsAdapter(nearByLocationsArray, TravelGuideActivity.this);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(mAdapter);

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);
        nearByLocationsArray = googlePlacesApi.getNearByLocations(nearByLocationsArray, mAdapter);
    }

    private void addVisitedPlace() {

        // Create a new user with a first and last name
        placeMap.put("Place Name", landmarkNameString);
        placeMap.put("Date Visited", "March 2018");
        placeMap.put("ID", placeID);

        cloudFirestore = new CloudFirestore(placeMap, currentUser);
        cloudFirestore.addPlace();
        setAddedTrue();
    }

    private void callSearchEngine(String placeName) {

        if (placeName != null) {
            searchString = placeName;
            TravelGuideActivity.WikipediaAsyncTask searchTask = new TravelGuideActivity.WikipediaAsyncTask();
            searchTask.execute(placeName);
        }
    }

    private void getLandmark(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionCloudLandmarks -> {
                    Log.d("VISION", firebaseVisionCloudLandmarks.get(0).getLandmark());
                    landmarkName.setText(firebaseVisionCloudLandmarks.get(0).getLandmark());
                    callSearchEngine(firebaseVisionCloudLandmarks.get(0).getLandmark());
                });
    }

    private void openGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void openList(LinearLayout linearLayout) {
        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        linearLayout.setLayoutParams(params);
    }

    private void closeList(LinearLayout linearLayout) {
        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        float height = getResources().getDimension(R.dimen.list_height);
        params.height = (int) height;
        linearLayout.setLayoutParams(params);
    }


    /*---------------------------------------------------------------------- Activity Result ----------------------------------------------------------------------*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                try {
                    landmarkName.setText(place.getName());
                    googlePlacesApi.setPhoto(Objects.requireNonNull(place.getPhotoMetadatas()).get(0), landmarkImage);
                    landmarkNameString = place.getName();
                    landmarkOpeningHours.setText(googlePlacesApi.placeOpeningHours(place));
                    landmarkRating.setText(String.valueOf(place.getRating()));
                    placeID = place.getId();

                    if (place.getPriceLevel() != null) {
                        landmarkPrice.setText(place.getPriceLevel());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                callSearchEngine(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Cancelled");
            }
        }
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                getLandmark(bitmap);
                landmarkImage.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------- Permission Requests ----------------------------------------------------------------------*/

    private void requestPermission() {
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

    /*---------------------------------------------------------------------- Async Task ----------------------------------------------------------------------*/

    // Wikipedia Async Task
    private class WikipediaAsyncTask extends AsyncTask<String, Integer, String> {
        protected void onPreExecute() {
            Log.d(TAG, "AsyncTask - onPreExecute");
        }

        @Override
        protected String doInBackground(String... strings) {

            String keyword = strings[0];
            String searchText = keyword + " wikipedia";

            try {
                Document google = Jsoup.connect("https://www.google.com/search?q=" + URLEncoder.encode(searchText, encoding)).userAgent("Mozilla/5.0").get();
                String wikipediaURL = google.getElementsByTag("cite").get(0).text();
                String wikipediaApiJSON = "https://www.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="
                        + URLEncoder.encode(wikipediaURL.substring(wikipediaURL.lastIndexOf("/") + 1), encoding);

                HttpURLConnection httpcon = (HttpURLConnection) new URL(wikipediaApiJSON).openConnection();
                httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
                String responseSB = in.readLine();
                in.close();

                return responseSB.split("extract\":\"")[1];
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.substring(0, result.length() - 5);
            String wikipediaResult = result.replaceAll("[-+.^:,;()]", "");
            landmarkInformation.setText(wikipediaResult);
        }
    }

    // Google Search Async Task
    private class GoogleSearchAsyncTask extends AsyncTask<URL, Integer, String> {

        protected void onPreExecute() {
            Log.d(TAG, "AsyncTask - onPreExecute");
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            Log.d(TAG, "AsyncTask - doInBackground, url=" + url);

            // Http connection
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();

            } catch (IOException e) {
                Log.e(TAG, "Http connection ERROR " + e.toString());
            }

            try {
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
            } catch (IOException e) {
                Log.e(TAG, "Http getting response code ERROR " + e.toString());
            }

            try {
                if (responseCode == 200) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                        if (line.contains("snippet")) {
                            results.add(line);
                        }
                    }
                    rd.close();
                    conn.disconnect();
                    result = sb.toString();

                    return result;
                } else {
                    String errorMsg = "Http ERROR response " + responseMessage + "\n" + "Make sure to replace in code your own Google API key and Search Engine ID";
                    Log.e(TAG, errorMsg);
                    result = errorMsg;
                    return result;
                }
            } catch (IOException e) {
                Log.e(TAG, "Http Response ERROR " + e.toString());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            Log.d(TAG, "AsyncTask - onProgressUpdate, progress=" + progress);
        }

        protected void onPostExecute(String result) {

            Log.d(TAG, "AsyncTask - onPostExecute, result=" + result);

            for (int i = 0; i < results.size(); i++) {
                Log.d(TAG, results.get(i));
            }

//            StringBuffer sb = new StringBuffer(results.get(1).length());
//            sb.delete(0, 9);
//
//            String res = sb.toString();
//            res.replace(":", "");
//            res = results.get(2).substring(12, results.get(2).length() - 1);
//            imageURL = res;
//            new TravelGuideActivity.DownloadImageTask((ImageView) findViewById(R.id.attractionImage)).execute(res);
        }
    }

    // Download Image Async Task
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
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


    /*---------------------------------------------------------------------- Firebase ----------------------------------------------------------------------*/

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





































