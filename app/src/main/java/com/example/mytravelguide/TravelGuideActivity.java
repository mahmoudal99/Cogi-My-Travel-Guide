package com.example.mytravelguide;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.models.AttractionObject;
import com.example.mytravelguide.utils.CloudFirestore;
import com.example.mytravelguide.utils.GooglePlacesApi;
import com.example.mytravelguide.utils.NearByLocationsAdapter;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private TextView landmarkTextView, landmarkOpeningHours, landmarkPrice, landmarkRating, landmarkHistoryTextView, numberTextView, websiteTextView;
    private ImageView nearByLocationsButton, chooseImageButton, expandLandmarkInformation, expandNearByLocationsArrow, expandLandmarkHistory;
    private CardView informationCardView, nearbyLocationsCardView;

    private String landmarkNameString;
    private String placeID;

    private RelativeLayout landmarkRelativeLayout;

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
    boolean expendHistory = true;

    Context context;

    // Shared Preference
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_travel_guide);

        requestPermission();
        isWriteStoragePermissionGranted();
        init();
        setUpWidgets();
        loadPreviousLandmark();
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

//        landmarkImage = findViewById(R.id.attractionImage);
        landmarkRelativeLayout = findViewById(R.id.landmarkImage);
        landmarkTextView = findViewById(R.id.attractionName);
        landmarkOpeningHours = findViewById(R.id.openingHours);
        landmarkRating = findViewById(R.id.rating);
        landmarkPrice = findViewById(R.id.price);
        numberTextView = findViewById(R.id.number);
        websiteTextView = findViewById(R.id.website);
        landmarkHistoryTextView = findViewById(R.id.landmarkHistoryTextView);
        String landmarkInformationResult = "";
        landmarkNameString = "Landmark";
        landmarkNameString = getIntent().getStringExtra("AttractionName");

        informationCardView = findViewById(R.id.infoCard);
        nearbyLocationsCardView = findViewById(R.id.nearbyLocationsCardView);

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);

        expandLandmarkInformation = findViewById(R.id.expandInformation);
        expandNearByLocationsArrow = findViewById(R.id.expandNearLocationsByArrow);
        expandLandmarkHistory = findViewById(R.id.expandLandmarkHistory);

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

        expandLandmarkHistory.setOnClickListener(v -> {
            if (expendHistory) {
                LinearLayout layout = findViewById(R.id.landmarkHistoryList);
                expandLinearLayout(layout);
                expendHistory = false;
                landmarkHistoryTextView.setVisibility(View.VISIBLE);
                nearbyLocationsCardView.setVisibility(View.GONE);
                informationCardView.setVisibility(View.GONE);

            } else if (!expendHistory) {
                LinearLayout layout = findViewById(R.id.landmarkHistoryList);
                collapseLinearLayout(layout);
                expendHistory = true;
                nearbyLocationsCardView.setVisibility(View.VISIBLE);
                informationCardView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadPreviousLandmark(){
        landmarkTextView.setText(pref.getString("LandmarkName", "Landmark"));
        landmarkOpeningHours.setText(pref.getString("LandmarkOpeningHours", "0:00"));
        numberTextView.setText(pref.getString("LandmarkNumber", ""));
        websiteTextView.setText(pref.getString("LandmarkWebsite", ""));
        landmarkRating.setText(pref.getString("LandmarkRating", ""));
        Linkify.addLinks(websiteTextView,  Linkify.WEB_URLS);
        googlePlacesApi.loadImageFromStorage(landmarkRelativeLayout);
        new WikiApi().execute(landmarkTextView.getText().toString());
    }


    /*---------------------------------------------------------------------- Locale ----------------------------------------------------------------------*/

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        // save data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", lang);
        editor.commit();
    }

    public void loadLocale() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("Language", "");
        Log.d("MAHMOUD", language);
        setLocale(language);
    }

    /*---------------------------------------------------------------------- Landmark ----------------------------------------------------------------------*/

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
                    addLandmarkToTimeline();
                });
    }

    public void setLandmarkAddedTrue() {
        landmarkAdded = true;
    }

    private void addLandmarkToTimeline() {

        if (placeID == null) {
            Toast.makeText(this, "Landmark not added: No Id available", Toast.LENGTH_SHORT).show();
        } else {
            placeMap.put("Place Name", landmarkNameString);
            placeMap.put("ID", placeID);

            CloudFirestore cloudFirestore = new CloudFirestore(placeMap, currentUser);
            cloudFirestore.addPlace();
            setLandmarkAddedTrue();
            Toast.makeText(this, "Landmark added to timeline", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLandmark(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(firebaseVisionCloudLandmarks -> {
                    landmarkTextView.setText(firebaseVisionCloudLandmarks.get(0).getLandmark());
                });
    }

    private void landmarkPicker() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS,
                Place.Field.PRICE_LEVEL, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL, Place.Field.PHONE_NUMBER, Place.Field.VIEWPORT, Place.Field.WEBSITE_URI);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    /*---------------------------------------------------------------------- Features ----------------------------------------------------------------------*/

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

    private void openGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void setUpLinearLayout() {
        LinearLayout infoLayout = findViewById(R.id.informationList);
        collapseLinearLayout(infoLayout);

        LinearLayout nearByLayout = findViewById(R.id.nearByLocationsList);
        collapseLinearLayout(nearByLayout);
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
                    new WikiApi().execute(place.getName());
                    if(place.getName().equals("The Blue Mosque")){
                        landmarkTextView.setText(context.getString(R.string.sultan_ahmed_mosque));
                    }else {
                        landmarkTextView.setText(place.getName());
                    }

                    googlePlacesApi.setPhoto(Objects.requireNonNull(place.getPhotoMetadatas()).get(0), landmarkRelativeLayout);
                    landmarkNameString = place.getName();
                    landmarkOpeningHours.setText(googlePlacesApi.placeOpeningHours(place));
                    landmarkRating.setText(String.valueOf(place.getRating()));
                    numberTextView.setText(place.getPhoneNumber());
                    websiteTextView.setText(place.getWebsiteUri().toString());
                    Linkify.addLinks(websiteTextView,  Linkify.WEB_URLS);
                    placeID = place.getId();

                    // Save Landmark Information in Shared Preferences
                    editor.putString("LandmarkName", place.getName());
                    editor.putString("LandmarkOpeningHours", landmarkOpeningHours.getText().toString());
                    editor.putString("LandmarkInformation", landmarkHistoryTextView.getText().toString());
                    editor.putString("LandmarkRating", landmarkRating.getText().toString());
                    editor.putString("LandmarkWebsite", place.getWebsiteUri().toString());
                    editor.putString("LandmarkNumber", place.getPhoneNumber());
                    editor.apply();

                    Log.d("PLACEWIKI", place.getAddress() + " " + place.getPhoneNumber() + " " + place.getWebsiteUri().toString());

                    if (place.getPriceLevel() != null) {
                        landmarkPrice.setText(place.getPriceLevel());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

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

    /*---------------------------------------------------------------------- Wikipedia Api ----------------------------------------------------------------------*/

    private class WikiApi extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String keyword = strings[0];
            try {
//                Document google = Jsoup.connect("https://www.google.com/search?q=" + URLEncoder.encode(searchText, encoding)).userAgent("Mozilla/5.0").get();

                if (keyword.equals("The Blue Mosque")){
                    keyword = "Sultan Ahmed Mosque";
                }

                String wikipediaURL = keyword;
                String wikipediaApiJSON = "https://www.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="
                        + URLEncoder.encode(wikipediaURL.substring(wikipediaURL.lastIndexOf("/") + 1, wikipediaURL.length()), encoding);

                //"extract":" the summary of the article
                HttpURLConnection httpcon = (HttpURLConnection) new URL(wikipediaApiJSON).openConnection();
                httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

                String responseSB = in.readLine();

                in.close();
                Log.d("WIKI", responseSB);
                if (responseSB.split("extract\":\"").length > 1) {
                    String result = responseSB.split("extract\":\"")[1];
                    return result;
                }

                return "No information found";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            String wikipediaResult = result.replaceAll("[-+.^:,;(){}\']", "");
            wikipediaResult = wikipediaResult.replaceAll("[0-9]", "");
            wikipediaResult = wikipediaResult.replaceAll("\\\\", "");
            landmarkHistoryTextView.setText(wikipediaResult);
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

    public boolean isWriteStoragePermissionGranted(){
        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Permission Granted");
                return true;
            }else {
                Log.d(TAG, "Permission not given");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }else {
            Log.d(TAG, "Permission Granted");
            return true;
        }
    }
}





































