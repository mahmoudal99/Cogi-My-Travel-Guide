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
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.attractions.ExploreActivity;
import com.example.mytravelguide.models.AttractionObject;
import com.example.mytravelguide.utils.CloudFirestore;
import com.example.mytravelguide.utils.FetchURL;
import com.example.mytravelguide.utils.FirebaseMethods;
import com.example.mytravelguide.utils.GooglePlacesApi;
import com.example.mytravelguide.utils.ImageProcessing;
import com.example.mytravelguide.utils.JsonReader;
import com.example.mytravelguide.utils.Landmark;
import com.example.mytravelguide.utils.NearByLocationsAdapter;
import com.example.mytravelguide.utils.TaskLoadedCallback;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.L;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.api.client.util.IOUtils;
import com.google.api.services.customsearch.model.Search;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.kc.unsplash.Unsplash;
import com.kc.unsplash.models.SearchResults;

import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class TravelGuideActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private static final String TAG = "TravelGuideActivity";
    private final static int LOCATION = 3;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int PICK_IMAGE = 1;

    // Widgets
    private ImageView backArrow, addLandmarkToTimeline, searchLandmarkButton;
    private TextView landmarkTextView, landmarkOpeningHours, landmarkAddress, landmarkRating, numberTextView, websiteTextView, distanceTextView,
            durationTextView, open_closedTextView;
    private ImageView landmarkImage, mapImageView, informationImageView, carImage, cycleImageView, walkingImageView, journeyMode;
    private CardView informationCardView, mapOptionsCardView, mapCardView, landmarkImageCardView;
    private LinearLayout tripInformationLinLayout, tripInformationLinLayout2;

    // Variables
    private String landmarkNameString;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private Landmark landmark;

    // Google Maps
    private GoogleMap mGoogleMap;
    private MarkerOptions location1, location2;
    private Polyline currentPolyline;

    private Context context;

    // Shared Preference
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // Classes
    private ImageProcessing imageProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_travel_guide);

        getIncomingIntent();
        requestPermission();
        isWriteStoragePermissionGranted();
        init();
        setUpWidgets();
        supportMapFragment();
        loadPreviousLandmark();
        setUpFirebaseAuthentication();
    }

    private void getIncomingIntent() {
        if (getIntent().getStringExtra("landmarkID") != null) {
            getLandmarkFromIntent(getIntent().getStringExtra("landmarkID"));
        }
    }

    private void init() {
        context = TravelGuideActivity.this;

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.apply();

        imageProcessing = new ImageProcessing(TravelGuideActivity.this);

        backArrow = findViewById(R.id.backArrow);
        mapImageView = findViewById(R.id.mapImageView);
        carImage = findViewById(R.id.carImage);
        walkingImageView = findViewById(R.id.walkingImage);
        journeyMode = findViewById(R.id.journeyMode);
        cycleImageView = findViewById(R.id.cycleImage);
        informationImageView = findViewById(R.id.informationImageView);

        informationCardView = findViewById(R.id.infoCard);
        mapOptionsCardView = findViewById(R.id.mapOptionsCardView);
        mapCardView = findViewById(R.id.mapCardView);
        landmarkImageCardView = findViewById(R.id.landmarkImageCardView);
        tripInformationLinLayout = findViewById(R.id.tripInformationLinLayout);
        tripInformationLinLayout2 = findViewById(R.id.tripInformationLinLayout2);

        landmarkImage = findViewById(R.id.landmarkImage);
        landmark = new Landmark(context);
        addLandmarkToTimeline = findViewById(R.id.addPlace);
        searchLandmarkButton = findViewById(R.id.search);
        landmarkTextView = findViewById(R.id.attractionName);
        landmarkOpeningHours = findViewById(R.id.openingHours);
        landmarkRating = findViewById(R.id.rating);
        landmarkAddress = findViewById(R.id.address);
        numberTextView = findViewById(R.id.number);
        websiteTextView = findViewById(R.id.website);
        landmarkNameString = getIntent().getStringExtra("AttractionName");

        durationTextView = findViewById(R.id.durationText);
        distanceTextView = findViewById(R.id.distanceText);
        open_closedTextView = findViewById(R.id.open_closedTextView);
    }

    private void setUpWidgets() {

        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(TravelGuideActivity.this, HomePageActivity.class));
        });

        if (landmarkNameString != null) {
            landmarkTextView.setText(landmarkNameString);
        } else {
            landmarkTextView.setText(getString(R.string.landmark));
        }

        addLandmarkToTimeline.setOnClickListener(v -> {
            if (landmarkNameString != null) {
                landmark.checkLandmarkAlreadyAdded(landmarkNameString, pref.getString("LandmarkID", null), currentUser);
            } else {
                Toast.makeText(TravelGuideActivity.this, "No Landmark Selected", Toast.LENGTH_SHORT).show();
            }
        });

        searchLandmarkButton.setOnClickListener(v -> {
            Intent intent = landmark.landmarkPicker();
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });

        mapImageView.setOnClickListener(v -> {
            showMapWidgets();
        });

        informationImageView.setOnClickListener(v -> {
            showInformationWidgets();
        });

        carImage.setOnClickListener(v -> {
            setJourneyMode("driving");
            journeyMode.setImageDrawable(getDrawable(R.drawable.sports_car_blacl));
        });

        walkingImageView.setOnClickListener(v -> {
            setJourneyMode("walking");
            journeyMode.setImageDrawable(getDrawable(R.drawable.hiking_black));
        });

        cycleImageView.setOnClickListener(v -> {
            setJourneyMode("bicycling");
            journeyMode.setImageDrawable(getDrawable(R.drawable.man_cycling_black));
        });
    }

    private void clearTextViews() {
        landmarkOpeningHours.setText("");
        landmarkAddress.setText("");
        landmarkRating.setText("");
        landmarkTextView.setText("");
        websiteTextView.setText("");
    }

    // Google Map

    private void supportMapFragment() {
        initializeLocations();
        setUpMapFragment();
    }

    private void initializeLocations() {
        location1 = new MarkerOptions().position(new LatLng(48.8566, 2.3522)).title("Location 1");
        location2 = new MarkerOptions().position(new LatLng(pref.getFloat("LandmarkLat", (float) 27.667491), pref.getFloat("LandmarkLng", (float) 85.3208583))).title("Location 2");
    }

    private void setUpMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void showMapWidgets() {
        informationCardView.setVisibility(View.GONE);
        landmarkImageCardView.setVisibility(View.GONE);
        mapCardView.setVisibility(View.VISIBLE);
        mapOptionsCardView.setVisibility(View.VISIBLE);
        tripInformationLinLayout.setVisibility(View.VISIBLE);
        tripInformationLinLayout2.setVisibility(View.VISIBLE);
    }

    private void setJourneyMode(String mode) {
        LatLng latLng = new LatLng(pref.getFloat("LandmarkLat", (float) 27.667491), pref.getFloat("LandmarkLng", (float) 85.3208583));
        mGoogleMap.clear();
        location2 = new MarkerOptions().position(latLng);
        mGoogleMap.addMarker(location1);
        mGoogleMap.addMarker(location2);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location1.getPosition()));
        AsyncTask<String, Void, String> data = new FetchURL(TravelGuideActivity.this).execute(getUrl(location1.getPosition(), latLng, mode), mode);
        try {
            JsonReader jsonReader = new JsonReader();
            List<String> tripInformation = jsonReader.getDirectionsInformation(data.get());
            setDistanceDuration(tripInformation.get(0), tripInformation.get(1));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showInformationWidgets() {
        informationCardView.setVisibility(View.VISIBLE);
        landmarkImageCardView.setVisibility(View.VISIBLE);
        mapCardView.setVisibility(View.GONE);
        mapOptionsCardView.setVisibility(View.GONE);
        tripInformationLinLayout.setVisibility(View.GONE);
        tripInformationLinLayout2.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.clear();
        mGoogleMap.addMarker(location1);
        mGoogleMap.addMarker(location2);
        mGoogleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        mGoogleMap.setMaxZoomPreference(12);
        mGoogleMap.setMinZoomPreference(12);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location1.getPosition()));
        LatLng latLng = new LatLng(pref.getFloat("LandmarkLat", (float) 0.00), pref.getFloat("LandmarkLng", (float) 0.00));
        AsyncTask<String, Void, String> data = new FetchURL(TravelGuideActivity.this).execute(getUrl(location1.getPosition(), latLng, "driving"), "driving");
        try {
            JsonReader jsonReader = new JsonReader();
            List<String> tripInformation = jsonReader.getDirectionsInformation(data.get());
            setDistanceDuration(tripInformation.get(0), tripInformation.get(1));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setDistanceDuration(String distance, String duration) {
        durationTextView.setText(duration);
        distanceTextView.setText(distance);
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o";
    }

    private void updateMap(Place place) {
        LatLng latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        mGoogleMap.clear();
        location2 = new MarkerOptions().position(latLng).title(place.getName());
        mGoogleMap.addMarker(location1);
        mGoogleMap.addMarker(location2);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location1.getPosition()));
        AsyncTask<String, Void, String> data = new FetchURL(TravelGuideActivity.this).execute(getUrl(location1.getPosition(), latLng, "driving"), "driving");
        try {
            JsonReader jsonReader = new JsonReader();
            List<String> tripInformation = jsonReader.getDirectionsInformation(data.get());
            setDistanceDuration(tripInformation.get(0), tripInformation.get(1));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------- Locale ----------------------------------------------------------------------*/

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        saveLocale(lang);
    }

    private void saveLocale(String lang) {
        // save data
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("Language", "");
        setLocale(language);
    }

    /*---------------------------------------------------------------------- Landmark ----------------------------------------------------------------------*/

    private void loadLandmark(Place place) {
        if (place.getName().equals("The Blue Mosque")) {
            landmarkTextView.setText(context.getString(R.string.sultan_ahmed_mosque));
        } else {
            landmarkTextView.setText(place.getName());
        }

        if (place.getRating() != null) {
            landmarkRating.setText(place.getRating().toString());
        }
        if (place.getPhoneNumber() != null) {
            numberTextView.setText(place.getPhoneNumber());
        }
        if (place.getAddress() != null) {
            landmarkAddress.setText(place.getAddress());
        }

        if (place.getWebsiteUri() != null) {
            websiteTextView.setText(place.getWebsiteUri().toString());
            Linkify.addLinks(websiteTextView, Linkify.WEB_URLS);
        } else if (place.getWebsiteUri() == null) {

            websiteTextView.setText("No Information Available");
        }

        if (place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1) != null) {
            if (place.getOpeningHours().getWeekdayText().contains("Closed")) {
                open_closedTextView.setText(place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
            } else {
                open_closedTextView.setText("Opened");
            }

        }
        landmarkOpeningHours.setText(place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
        setLandmarkImage(place.getName());
        updateMap(place);
    }

    private void setLandmarkImage(String cityName) {
        Unsplash unsplash = new Unsplash("73a58cad473ac4376a1ed2c4f27cfeb08cfa77e8492f4cdfc2814085794d6100");
        unsplash.searchPhotos(cityName, new Unsplash.OnSearchCompleteListener() {
            @Override
            public void onComplete(SearchResults results) {
                imageProcessing.new SetLandmarkImage(landmarkImage).execute(results.getResults().get(0).getUrls().getRegular());
            }

            @Override
            public void onError(String error) {
                Log.d("Unsplash Error", error);
            }
        });
    }

    private void loadPreviousLandmark() {
        landmarkNameString = pref.getString("LandmarkName", "Landmark");
        landmarkTextView.setText(pref.getString("LandmarkName", "Landmark"));
        landmarkOpeningHours.setText(pref.getString("LandmarkOpeningHours", "0:00"));

        if (pref.getString("LandmarkOpenClosed", "Opened").contains("Closed")) {
            open_closedTextView.setText(pref.getString("LandmarkOpenClosed", "Opened"));
        } else {
            open_closedTextView.setText("Opened");
        }

        numberTextView.setText(pref.getString("LandmarkNumber", ""));
        websiteTextView.setText(pref.getString("LandmarkWebsite", ""));
        landmarkRating.setText(pref.getString("LandmarkRating", ""));
        landmarkAddress.setText(pref.getString("LandmarkAddress", ""));
        String placeID = pref.getString("LandmarkID", null);
        Linkify.addLinks(websiteTextView, Linkify.WEB_URLS);
        imageProcessing.loadLandmarkImageFromStorage(landmarkImage);
    }

    private void saveLandmarkInformation(Place place) {

        if (place.getRating() != null) {
            editor.putString("LandmarkRating", place.getRating().toString());
        }
        if (place.getPhoneNumber() != null) {
            editor.putString("LandmarkNumber", place.getPhoneNumber());
        }
        if (place.getWebsiteUri() != null) {
            editor.putString("LandmarkWebsite", place.getWebsiteUri().toString());
        }
        if (place.getId() != null) {
            editor.putString("LandmarkID", place.getId());
        }
        if (place.getAddress() != null) {
            editor.putString("LandmarkAddress", place.getAddress());
        }

        if (place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1) != null) {
            editor.putString("LandmarkOpeningHours", place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
            editor.putString("LandmarkOpenClosed", place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
        }

        editor.putString("LandmarkName", place.getName());
        editor.putFloat("LandmarkLat", (float) place.getLatLng().latitude);
        editor.putFloat("LandmarkLng", (float) place.getLatLng().longitude);
        editor.apply();
    }

    private void getLandmarkFromIntent(String landmarkID) {

        Places.initialize(getApplicationContext(), "AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o");

        PlacesClient placesClient = Places.createClient(TravelGuideActivity.this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(landmarkID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            loadLandmark(place);
            saveLandmarkInformation(place);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
            }
        });
    }

    private int getDayOfWeek() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return day.getValue();
    }

    /*---------------------------------------------------------------------- Activity Result -------------------------------------------------------------*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                clearTextViews();
                loadLandmark(place);
                saveLandmarkInformation(place);

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
                landmark.getLandmarkFromImage(bitmap, landmarkTextView);
                landmarkImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /*---------------------------------------------------------------------- Permission Requests -------------------------------------------------------- */

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOCATION);
            }
        }
    }

    public void isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission Granted");
            } else {
                Log.d(TAG, "Permission not given");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            }
        } else {
            Log.d(TAG, "Permission Granted");
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

    /*---------------------------------------------------------------------- Firebase ---------------------------------------------------------------------*/

    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "Success");
            } else {
                Log.d(TAG, "signed out");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mGoogleMap.addPolyline((PolylineOptions) values[0]);
    }
}