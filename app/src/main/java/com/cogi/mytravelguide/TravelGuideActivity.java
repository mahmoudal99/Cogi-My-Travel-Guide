package com.cogi.mytravelguide;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cogi.mytravelguide.utils.FetchURL;
import com.cogi.mytravelguide.utils.GooglePlacesApi;
import com.cogi.mytravelguide.utils.ImageProcessing;
import com.cogi.mytravelguide.utils.JsonReader;
import com.cogi.mytravelguide.utils.Landmark;
import com.cogi.mytravelguide.models.LandmarkSwipeViewModel;
import com.cogi.mytravelguide.adapters.LandmarkSwipeViewAdapter;
import com.cogi.mytravelguide.utils.TaskLoadedCallback;
import com.cogi.mytravelguide.utils.WikiData;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class TravelGuideActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, View.OnClickListener, LocationListener {

    private static final String TAG = "TravelGuideActivity";
    private static final String STARTINGPOINTREQUEST = "STARTINGPOINTREQUEST";
    private final static int LOCATION = 3;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int PICK_IMAGE = 1;

    // Widgets
    private ImageView backArrow, addLandmarkToTimeline, searchLandmarkButton;
    private TextView landmarkTextView, landmarkOpeningHours, landmarkAddress, landmarkRating, numberTextView, websiteTextView, distanceTextView,
            durationTextView, open_closedTextView, noLandmarkSelected;
    private ImageView landmarkImage, mapImageView, informationImageView, carImage, cycleImageView, walkingImageView, journeyMode, noLandmarkImage;
    private CardView informationCardView, mapOptionsCardView, mapCardView, landmarkImageCardView;
    private LinearLayout tripInformationLinLayout, tripInformationLinLayout2;
    private EditText searchStartingPointEditText;

    ViewPager viewPager;
    LandmarkSwipeViewAdapter adapter;
    List<LandmarkSwipeViewModel> models;

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
    private WikiData wikiData;
    private OkHttpClient okHttpClient;

    // Dialog
    DatePickerDialog datePickerDialog;
    private int mYear, mMonth, mDay;

    LinearLayout linearLayoutMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_travel_guide);


        requestPermission();
        init();
        setUpWidgets();
        supportMapFragment();
        if (getIntent().hasExtra("landmarkID")) {
            getIncomingIntent();
        } else {
            loadPreviousLandmark();
        }
        setUpFirebaseAuthentication();
    }

    private void createModels(PhotoMetadata[] imageStrings) {
        models = new ArrayList<>();
        models.add(new LandmarkSwipeViewModel(imageStrings[0]));
        models.add(new LandmarkSwipeViewModel(imageStrings[1]));
        models.add(new LandmarkSwipeViewModel(imageStrings[2]));
        models.add(new LandmarkSwipeViewModel(imageStrings[3]));
        callSwipeViewAdapter();
    }

    private void callSwipeViewAdapter() {
        adapter = new LandmarkSwipeViewAdapter(models, this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(130, 0, 130, 0);
        setUpViewPager();
    }

    private void setUpViewPager() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("SwipeViewPager", String.valueOf(position));
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("SwipeViewPager", String.valueOf(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void getIncomingIntent() {
        if (getIntent().getStringExtra("landmarkID") != null) {
            getLandmarkFromIntent(getIntent().getStringExtra("landmarkID"), getIntent().getStringExtra("city"));
        }
    }

    private void init() {
        datePickerDialog = new DatePickerDialog(TravelGuideActivity.this);
        wikiData = new WikiData();
        linearLayoutMode = findViewById(R.id.linearLayoutMode);
        viewPager = findViewById(R.id.viewPager);
        okHttpClient = new OkHttpClient();
        context = TravelGuideActivity.this;
        noLandmarkSelected = findViewById(R.id.noLandmarkSelected);
        noLandmarkImage = findViewById(R.id.noLandmarkImage);

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

        searchStartingPointEditText = findViewById(R.id.searchStartingPoint);
    }

    private void setUpWidgets() {

        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(TravelGuideActivity.this, HomePageActivity.class));
        });
        linearLayoutMode.setVisibility(View.GONE);
        if (landmarkNameString != null) {
            landmarkTextView.setText(landmarkNameString);
        } else {
            landmarkTextView.setText(getString(R.string.landmark));
        }

        addLandmarkToTimeline.setOnClickListener(v -> {
            if (landmarkNameString != null) {
                datePickerDialog.setTitle("Date Visited");
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        (view, year, monthOfYear, dayOfMonth) -> landmark.checkLandmarkAlreadyAdded(pref.getString("LandmarkName", "Landmark"),
                                pref.getString("LandmarkID", null), currentUser,
                                dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), mYear, mMonth, mDay);
                datePickerDialog.show();
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
            viewPager.setVisibility(View.GONE);
        });

        informationImageView.setOnClickListener(v -> {
            showInformationWidgets();
            viewPager.setVisibility(View.VISIBLE);
        });


        carImage.setOnClickListener(v -> {
            setJourneyMode("driving");
            journeyMode.setImageDrawable(getResources().getDrawable(R.drawable.sports_car_black));
        });


        walkingImageView.setOnClickListener(v -> {
            setJourneyMode("walking");
            journeyMode.setImageDrawable(getResources().getDrawable(R.drawable.hiking_black));
        });

        cycleImageView.setOnClickListener(v -> {
            setJourneyMode("bicycling");
            journeyMode.setImageDrawable(getResources().getDrawable(R.drawable.man_cycling_black));
        });


        searchStartingPointEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        searchStartingPointEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                GooglePlacesApi googlePlacesApi = new GooglePlacesApi("AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o", TravelGuideActivity.this);
                linearLayoutMode.setVisibility(View.VISIBLE);
                Request request = wikiData.createLandmarkPlaceIdRequest(googlePlacesApi.getPlacesByQuery(searchStartingPointEditText.getText().toString() + pref.getString("city", "")));
                httpClientCall(request, STARTINGPOINTREQUEST);
                closeKeyboard();
            }
            return false;
        });
    }

    private void httpClientCall(Request request, String requestType) {
        okHttpClient.setProtocols(Collections.singletonList(Protocol.HTTP_1_1));
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("City WIKIDATA Id" + "Error", e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String requestReponse = response.body().string();
                switch (requestType) {
                    case STARTINGPOINTREQUEST:
                        JsonReader jsonReader = new JsonReader();
                        JSONObject placeIDObject = jsonReader.getLandmarkPlaceIDFromJson(requestReponse);
                        try {
                            getStartingPointLatLng(placeIDObject.get("place_id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            }
        });
    }

    private void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
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
        location2 = new MarkerOptions().position(new LatLng(pref.getFloat("LandmarkLat", (float) 0.0), pref.getFloat("LandmarkLng", (float) 0.0))).title("Location 2");
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
            if (tripInformation == null) {
                runOnUiThread(() -> {
                    Toast.makeText(TravelGuideActivity.this, "Cycling not available for this journey", Toast.LENGTH_SHORT).show();
                });
            } else {
                setDistanceDuration(tripInformation.get(0), tripInformation.get(1));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showInformationWidgets() {
        informationCardView.setVisibility(View.VISIBLE);
        mapCardView.setVisibility(View.GONE);
        mapOptionsCardView.setVisibility(View.GONE);
        tripInformationLinLayout.setVisibility(View.GONE);
        tripInformationLinLayout2.setVisibility(View.GONE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.clear();
        mGoogleMap.addMarker(location2);
        mGoogleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        mGoogleMap.setMaxZoomPreference(20);
        mGoogleMap.setMinZoomPreference(10);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location2.getPosition()));
        durationTextView.setText("");
        distanceTextView.setText("");
        journeyMode.setVisibility(View.INVISIBLE);
    }

    private void setDistanceDuration(String distance, String duration) {
        durationTextView.setText(duration);
        distanceTextView.setText(distance);
        journeyMode.setVisibility(View.VISIBLE);
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
        linearLayoutMode.setVisibility(View.GONE);
        location2 = new MarkerOptions().position(latLng).title(place.getName());
        mGoogleMap.addMarker(location2);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location2.getPosition()));
    }

    private void newStartingPoint(Place place) {
        LatLng latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
        mGoogleMap.clear();
        location1 = new MarkerOptions().position(latLng).title("Starting Point");
        LatLng landmarkLatLng = new LatLng(pref.getFloat("LandmarkLat", (float) 0.00), pref.getFloat("LandmarkLng", (float) 0.00));
        location2 = new MarkerOptions().position(landmarkLatLng).title(pref.getString("LandmarkName", "Landmark"));
        mGoogleMap.addMarker(location1);
        mGoogleMap.addMarker(location2);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(location1.getPosition()));
        durationTextView.setText("");
        distanceTextView.setText("");
        journeyMode.setVisibility(View.INVISIBLE);
        AsyncTask<String, Void, String> data = new FetchURL(TravelGuideActivity.this).execute(getUrl(location1.getPosition(), location2.getPosition(), "driving"), "driving");
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

            websiteTextView.setText(getResources().getString(R.string.no_infotmation_available));
        }


        if (place.getOpeningHours().getWeekdayText().contains(getResources().getString(R.string.close))) {
            open_closedTextView.setText(place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
            landmarkOpeningHours.setText(place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
        } else {
            open_closedTextView.setVisibility(View.VISIBLE);
            open_closedTextView.setText(getResources().getString(R.string.open));
        }


//        if (place.getName().contains("Great Sphinx of Giza")) {
//            if (place.getPhotoMetadatas() != null) {
//                GooglePlacesApi googlePlacesApi = new GooglePlacesApi(BuildConfig.APIKEY, TravelGuideActivity.this);
//                googlePlacesApi.setLandmarkImageWithBitmap(place.getPhotoMetadatas().get(0), landmarkImage);
//                landmarkImageCardView.setVisibility(View.VISIBLE);
//                viewPager.setVisibility(View.GONE);
//            }
//        } else {
//            setLandmarkImage(place.getPhotoMetadatas());
//            Log.d("METADATA1", place.getPhotoMetadatas().get(0).toString() +
//                    place.getPhotoMetadatas().get(1).toString() + place.getPhotoMetadatas().get(2));
//            landmarkImageCardView.setVisibility(View.GONE);
//            viewPager.setVisibility(View.VISIBLE);
//        }

        if (place.getPhotoMetadatas() != null) {
            setLandmarkImage(place.getPhotoMetadatas());
            landmarkImageCardView.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        }

        updateMap(place);
    }

    private void setLandmarkImage(List<PhotoMetadata> photoMetadataList) {
        PhotoMetadata[] landmarkImageStrings = new PhotoMetadata[4];
        landmarkImageStrings[0] = photoMetadataList.get(0);
        landmarkImageStrings[1] = photoMetadataList.get(1);
        landmarkImageStrings[2] = photoMetadataList.get(2);
        landmarkImageStrings[3] = photoMetadataList.get(3);
        createModels(landmarkImageStrings);
    }

    private void loadPreviousLandmark() {
        if (pref.getString("LandmarkName", "Landmark").contains("Landmark")) {
            Log.d("No Previous Landmark", "Landmark");
        } else {
            noLandmarkSelected.setVisibility(View.GONE);
            noLandmarkImage.setVisibility(View.GONE);
            landmarkNameString = pref.getString("LandmarkName", "Landmark");
            landmarkTextView.setText(pref.getString("LandmarkName", "Landmark"));
            landmarkOpeningHours.setText(pref.getString("LandmarkOpeningHours", "0:00"));

            if (pref.getString("LandmarkOpenClosed", "Opened").contains("Closed")) {
                open_closedTextView.setVisibility(View.VISIBLE);
                open_closedTextView.setText(getResources().getString(R.string.close));
            } else {
                open_closedTextView.setVisibility(View.VISIBLE);
                open_closedTextView.setText(getResources().getString(R.string.open));
            }

            numberTextView.setText(pref.getString("LandmarkNumber", ""));
            websiteTextView.setText(pref.getString("LandmarkWebsite", ""));
            landmarkRating.setText(pref.getString("LandmarkRating", ""));
            landmarkAddress.setText(pref.getString("LandmarkAddress", ""));
            String placeID = pref.getString("LandmarkID", null);
            Linkify.addLinks(websiteTextView, Linkify.WEB_URLS);
            loadPreviousLandmarkPhotos(placeID);
        }
    }

    private void loadPreviousLandmarkPhotos(String id) {
        Places.initialize(context, BuildConfig.APIKEY);
        PlacesClient placesClient = Places.createClient(context);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(id, placeFields);
        placesClient.fetchPlace(request).addOnSuccessListener(fetchPlaceResponse -> {
            Place place = fetchPlaceResponse.getPlace();
            setLandmarkImage(place.getPhotoMetadatas());
        });
    }

    private void saveLandmarkInformation(Place place, String city) {

        if (place.getRating() != null) {
            editor.putString("LandmarkRating", place.getRating().toString());
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        if (place.getPhoneNumber() != null) {
            editor.putString("LandmarkNumber", place.getPhoneNumber());
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        if (place.getWebsiteUri() != null) {
            editor.putString("LandmarkWebsite", place.getWebsiteUri().toString());
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        if (place.getId() != null) {
            editor.putString("LandmarkID", place.getId());
        }
        if (place.getAddress() != null) {
            editor.putString("LandmarkAddress", place.getAddress());
        }

        if (place.getOpeningHours() != null) {
            editor.putString("LandmarkOpeningHours", place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
            editor.putString("LandmarkOpenClosed", place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        editor.putString("LandmarkCity", city);
        editor.putString("LandmarkName", place.getName());
        editor.putFloat("LandmarkLat", (float) place.getLatLng().latitude);
        editor.putFloat("LandmarkLng", (float) place.getLatLng().longitude);
        editor.apply();
    }

    private void saveLandmarkInformation(Place place) {

        if (place.getRating() != null) {
            editor.putString("LandmarkRating", place.getRating().toString());
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        if (place.getPhoneNumber() != null) {
            editor.putString("LandmarkNumber", place.getPhoneNumber());
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        if (place.getWebsiteUri() != null) {
            editor.putString("LandmarkWebsite", place.getWebsiteUri().toString());
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        if (place.getId() != null) {
            editor.putString("LandmarkID", place.getId());
        }
        if (place.getAddress() != null) {
            editor.putString("LandmarkAddress", place.getAddress());
        }

        if (place.getOpeningHours() != null) {
            editor.putString("LandmarkOpeningHours", place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
            editor.putString("LandmarkOpenClosed", place.getOpeningHours().getWeekdayText().get(getDayOfWeek() - 1));
        } else {
            editor.putString("LandmarkWebsite", "Information Not Available");
        }

        editor.putString("LandmarkName", place.getName());
        editor.putFloat("LandmarkLat", (float) place.getLatLng().latitude);
        editor.putFloat("LandmarkLng", (float) place.getLatLng().longitude);
        editor.apply();
    }

    private void getLandmarkFromIntent(String landmarkID, String city) {

        Places.initialize(getApplicationContext(), "AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o");

        PlacesClient placesClient = Places.createClient(TravelGuideActivity.this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(landmarkID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            loadLandmark(place);
            saveLandmarkInformation(place, city);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
            }
        });
    }

    private void getStartingPointLatLng(String id) {
        Places.initialize(getApplicationContext(), "AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o");

        PlacesClient placesClient = Places.createClient(TravelGuideActivity.this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(id, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            newStartingPoint(response.getPlace());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
            }
        });
    }

    private int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
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

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Your Location"));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}











































