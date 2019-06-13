package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
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
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.Models.AttractionObject;
import com.example.mytravelguide.Utils.CloudFirestore;
import com.example.mytravelguide.Utils.GooglePlacesApi;
import com.example.mytravelguide.Utils.NearByLocationsAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class TravelGuideActivity extends AppCompatActivity {

    private static final String TAG = "TravelGuideActivity";
    private static final String API_KEY = BuildConfig.APIKEY;
    private final static int LOCATION = 3;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int PICK_IMAGE = 1;
    private static final String encoding = "UTF-8";

    // Widgets
    private ImageView backArrow, addLandmarkToTimeline, landmarkImage, searchLandmarkButton;
    private TextView landmarkTextView, landmarkOpeningHours, landmarkPrice, landmarkRating;
    private ImageView nearByLocationsButton, chooseImageButton, expandLandmarkInformation, expandNearByLocationsArrow;

    private String landmarkNameString;
    private String placeID;

    private Map<String, String> placeMap;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Google
    private GooglePlacesApi googlePlacesApi;

    boolean expandInfo = true;
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
        nearByLocationsButton = findViewById(R.id.location);
        addLandmarkToTimeline = findViewById(R.id.addPlace);
        searchLandmarkButton = findViewById(R.id.search);
        chooseImageButton = findViewById(R.id.gallery);

        landmarkImage = findViewById(R.id.attractionImage);
        landmarkTextView = findViewById(R.id.attractionName);
        landmarkOpeningHours = findViewById(R.id.openingHours);
        landmarkRating = findViewById(R.id.rating);
        landmarkPrice = findViewById(R.id.price);
        String landmarkInformationResult = "";
        landmarkNameString = "Landmark";
        landmarkNameString = getIntent().getStringExtra("AttractionName");

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);

        expandLandmarkInformation = findViewById(R.id.expandInformation);
        expandNearByLocationsArrow = findViewById(R.id.expandNearLocationsByArrow);

        placeMap = new HashMap<>();
    }

    private void setUpWidgets() {
        backArrow.setOnClickListener(v -> startActivity(new Intent(TravelGuideActivity.this, HomePageActivity.class)));

        if (landmarkNameString != null) {
            landmarkTextView.setText(landmarkNameString);
        } else {
            landmarkTextView.setText(getString(R.string.landmark));
        }

        addLandmarkToTimeline.setOnClickListener(v -> {
            if (landmarkNameString != null) {
                checkLandmarkAlreadyAdded(landmarkNameString);
            } else {
                Toast.makeText(TravelGuideActivity.this, "No Landmark Selected", Toast.LENGTH_SHORT).show();
            }
        });

        nearByLocationsButton.setOnClickListener(v -> loadNearByLocations());
        chooseImageButton.setOnClickListener(v -> openGallery());

        searchLandmarkButton.setOnClickListener(v -> landmarkPicker());

        expandLandmarkInformation.setOnClickListener(v -> {
            if (expandInfo) {
                LinearLayout layout = findViewById(R.id.informationList);
                expandLinearLayout(layout);
                expandInfo = false;
            } else if (!expandInfo) {
                LinearLayout layout = findViewById(R.id.informationList);
                collapseLinearLayout(layout);
                expandInfo = true;
            }
        });


        expandNearByLocationsArrow.setOnClickListener(v -> {

            if (expandNearBy) {
                LinearLayout layout = findViewById(R.id.nearByLocationsList);
                expandLinearLayout(layout);
                expandNearBy = false;

            } else if (!expandNearBy) {
                LinearLayout layout = findViewById(R.id.nearByLocationsList);
                collapseLinearLayout(layout);
                expandNearBy = true;
            }
        });

    }

    private void checkLandmarkAlreadyAdded(String landmarkName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getString(R.string.VisitedPlaces)).document(currentUser.getUid()).collection(getString(R.string.MyPlaces))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (Objects.requireNonNull(document.get("Place Name")).toString().equals(landmarkName)) {
                                setLandmarkAddedTrue();
                                Toast.makeText(TravelGuideActivity.this, "Landmark Already Added To Timeline", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                    addVisitedLandmark();
                });
    }

    public void setLandmarkAddedTrue() {
        landmarkAdded = true;
    }

    private void setUpLinearLayout() {
        LinearLayout infoLayout = findViewById(R.id.informationList);
        collapseLinearLayout(infoLayout);

        LinearLayout nearByLayout = findViewById(R.id.nearByLocationsList);
        collapseLinearLayout(nearByLayout);
    }


    /*---------------------------------------------------------------------- Features ----------------------------------------------------------------------*/
    private void landmarkPicker() {
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

        RecyclerView listView = findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView.Adapter mAdapter = new NearByLocationsAdapter(nearByLocationsArray, TravelGuideActivity.this);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(mAdapter);

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);
        nearByLocationsArray = googlePlacesApi.getNearByLocations(nearByLocationsArray, mAdapter);
    }

    private void addVisitedLandmark() {
        placeMap.put("Place Name", landmarkNameString);
        placeMap.put("Date Visited", "March 2018");
        placeMap.put("ID", placeID);

        CloudFirestore cloudFirestore = new CloudFirestore(placeMap, currentUser);
        cloudFirestore.addPlace();
        setLandmarkAddedTrue();
    }

//    private void callSearchEngine(String placeName) {
//        if (placeName != null) {
//            Log.d("Place Come On", placeName);
//            TravelGuideActivity.WikipediaAsyncTask searchTask = new TravelGuideActivity.WikipediaAsyncTask();
//            searchTask.execute(placeName);
//        }
//    }

    private void getLandmark(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionCloudLandmarks -> {
                    landmarkTextView.setText(firebaseVisionCloudLandmarks.get(0).getLandmark());
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

    private void collapseLinearLayout(LinearLayout linearLayout) {
        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        float height = getResources().getDimension(R.dimen.list_height);
        params.height = (int) height;
        linearLayout.setLayoutParams(params);
    }

    private void expandLinearLayout(LinearLayout linearLayout) {
        ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        linearLayout.setLayoutParams(params);
    }


    /*---------------------------------------------------------------------- Activity Result ----------------------------------------------------------------------*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                try {
                    landmarkTextView.setText(place.getName());
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
//                callSearchEngine(place.getName());
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
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TravelGuideActivity.this, "This app requires location permissions to detect your location!", Toast.LENGTH_LONG).show();
                finish();
            }
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

                String wikipediaURL = google.getElementsByTag("a").get(12).text();

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





































