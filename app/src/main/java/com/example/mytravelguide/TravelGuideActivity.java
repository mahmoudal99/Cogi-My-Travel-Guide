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
import com.example.mytravelguide.utils.FirebaseMethods;
import com.example.mytravelguide.utils.GooglePlacesApi;
import com.example.mytravelguide.utils.ImageProcessing;
import com.example.mytravelguide.utils.Landmark;
import com.example.mytravelguide.utils.NearByLocationsAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
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
import java.util.ArrayList;
import java.util.Arrays;
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

public class TravelGuideActivity extends AppCompatActivity {

    private static final String TAG = "TravelGuideActivity";

    private final static int LOCATION = 3;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private static final int PICK_IMAGE = 1;

    // Widgets
    private ImageView backArrow, addLandmarkToTimeline, searchLandmarkButton;
    private TextView landmarkTextView, landmarkOpeningHours, landmarkAddress, landmarkRating, landmarkHistoryTextView, numberTextView, websiteTextView;
    private ImageView expandLandmarkHistory, mircophone, landmarkImage;
    private CardView informationCardView;

    private String landmarkNameString, placeID;

    private RelativeLayout landmarkRelativeLayout;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private Landmark landmark;
    private FirebaseMethods firebaseMethods;

    // Google
    private GooglePlacesApi googlePlacesApi;
    boolean expendHistory = true;

    private TextToSpeech textToSpeech;

    Context context;

    // Shared Preference
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private ImageProcessing imageProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_travel_guide);
        if(getIntent().getStringExtra("landmarkID") != null){
            getLandmarkFromIntent(getIntent().getStringExtra("landmarkID"));
        }
        requestPermission();
        setUpTextToSpeech();
        isWriteStoragePermissionGranted();
        init();
        setUpWidgets();
        loadPreviousLandmark();
        setUpFirebaseAuthentication();
    }

    private void init() {
        context = TravelGuideActivity.this;
        landmark = new Landmark(context);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.apply();

        imageProcessing = new ImageProcessing(TravelGuideActivity.this);

        backArrow = findViewById(R.id.backArrow);
        addLandmarkToTimeline = findViewById(R.id.addPlace);
        searchLandmarkButton = findViewById(R.id.search);

//        landmarkImage = findViewById(R.id.attractionImage);
        landmarkRelativeLayout = findViewById(R.id.landmarkImage);
        landmarkTextView = findViewById(R.id.attractionName);
        landmarkOpeningHours = findViewById(R.id.openingHours);
        landmarkRating = findViewById(R.id.rating);
        landmarkAddress = findViewById(R.id.address);
        numberTextView = findViewById(R.id.number);
        websiteTextView = findViewById(R.id.website);
        landmarkHistoryTextView = findViewById(R.id.landmarkHistoryTextView);
        String landmarkInformationResult = "";
        landmarkNameString = getIntent().getStringExtra("AttractionName");

        informationCardView = findViewById(R.id.infoCard);
        mircophone = findViewById(R.id.microphone);

        googlePlacesApi = new GooglePlacesApi(TravelGuideActivity.this);
        firebaseMethods = new FirebaseMethods(TravelGuideActivity.this);
        expandLandmarkHistory = findViewById(R.id.expandLandmarkHistory);
    }

    private void setUpWidgets() {
        mircophone.setEnabled(false);
        mircophone.setOnClickListener(v -> speak());

        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(TravelGuideActivity.this, HomePageActivity.class));
            textToSpeech.stop();
            textToSpeech.shutdown();
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

//        expandLandmarkInformation.setOnClickListener(v -> {
//            if (expandInfo) {
//                LinearLayout layout = findViewById(R.id.informationList);
//                expandLinearLayout(layout);
//                expandInfo = false;
//            } else if (!expandInfo) {
//                LinearLayout layout = findViewById(R.id.informationList);
//                collapseLinearLayout(layout);
//                expandInfo = true;
//            }
//        });

        expandLandmarkHistory.setOnClickListener(v -> {
            if (expendHistory) {
                expendHistory = false;
                landmarkHistoryTextView.setVisibility(View.VISIBLE);

            } else if (!expendHistory) {
                LinearLayout layout = findViewById(R.id.landmarkInformationLin);
                expandLinearLayout(layout);
                expendHistory = true;
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layout.setLayoutParams(params);
                landmarkHistoryTextView.setVisibility(View.GONE);
                informationCardView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void clearTextViews() {
        landmarkOpeningHours.setText("");
        landmarkAddress.setText("");
        landmarkRating.setText("");
        landmarkTextView.setText("");
        websiteTextView.setText("");
        landmarkHistoryTextView.setText("");
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

//        if(place.getPhotoMetadatas() != null){
//            googlePlacesApi.setLandmarkPhoto(Objects.requireNonNull(place.getPhotoMetadatas()).get(0), landmarkRelativeLayout);
//        }else {
//            Log.d("IMAGERROR", "unsplash");
//            setCityImage(place.getName());
//        }

        setCityImage(place.getName());

        landmarkOpeningHours.setText(googlePlacesApi.placeOpeningHours(place));

    }

    private void setCityImage(String cityName) {
        Unsplash unsplash = new Unsplash("73a58cad473ac4376a1ed2c4f27cfeb08cfa77e8492f4cdfc2814085794d6100");
        unsplash.searchPhotos(cityName, new Unsplash.OnSearchCompleteListener() {
            @Override
            public void onComplete(SearchResults results) {
                imageProcessing.new SetCityImageRelLayout(landmarkRelativeLayout).execute(results.getResults().get(0).getUrls().getRegular());
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
        numberTextView.setText(pref.getString("LandmarkNumber", ""));
        websiteTextView.setText(pref.getString("LandmarkWebsite", ""));
        landmarkRating.setText(pref.getString("LandmarkRating", ""));
        landmarkAddress.setText(pref.getString("LandmarkAddress", ""));
        placeID = pref.getString("LandmarkID", null);
        Linkify.addLinks(websiteTextView, Linkify.WEB_URLS);
        landmarkHistoryTextView.setText(pref.getString("LandmarkHistory", ""));
        imageProcessing.loadImageFromStorage(landmarkRelativeLayout);
        firebaseMethods.getLandmarkInformation(landmarkNameString, landmarkHistoryTextView);
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

        editor.putString("LandmarkName", place.getName());
        editor.putString("LandmarkOpeningHours", googlePlacesApi.placeOpeningHours(place));
//        editor.putString("LandmarkInformation", landmarkHistoryTextView.getText().toString());
        editor.putString("LandmarkHistory", landmarkHistoryTextView.getText().toString());
        editor.apply();
    }

    private void getLandmarkFromIntent(String landmarkID){

        Places.initialize(getApplicationContext(), "AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o");

        PlacesClient placesClient = Places.createClient(TravelGuideActivity.this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(landmarkID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.d("GETTINGPLACEID1", "Place found: " + place.getOpeningHours());
            loadLandmark(place);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e("GETTINGPLACEID1", "Place not found: " + exception.getMessage());
            }
        });
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

//    private void setUpLinearLayout() {
//        LinearLayout infoLayout = findViewById(R.id.informationList);
//        collapseLinearLayout(infoLayout);
//    }

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

    private void setUpTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.d("TextToSpeech", "Language not supported");
                } else {
                    mircophone.setEnabled(true);
                }
            } else {
                Log.d("TextToSpeech", "TextToSpeech initializing failed");
            }
        });
    }

    private void speak() {
        textToSpeech.speak(landmarkHistoryTextView.getText(), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(0));
    }

    /*---------------------------------------------------------------------- Activity Result ----------------------------------------------------------------------*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

//                try {
                FirebaseMethods firebaseMethods = new FirebaseMethods(TravelGuideActivity.this);
                firebaseMethods.getLandmarkInformation(place.getName(), landmarkHistoryTextView);
                clearTextViews();
                loadLandmark(place);
                saveLandmarkInformation(place);

//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.d("BURJ", e.getMessage() + "none");
//                }

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

    /*---------------------------------------------------------------------- Permission Requests ----------------------------------------------------------------------*/

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

    /*---------------------------------------------------------------------- Wikipedia Api ----------------------------------------------------------------------*/

//    private class WikiApi extends AsyncTask<String, Integer, String> {
//        @Override
//        protected String doInBackground(String... strings) {
//            String keyword = strings[0];
//            try {
//
//                keyword = keyword.replaceAll(" ", "+");
//                String searchText = keyword + "+wikipedia";
//                Document document = Jsoup.connect("https://www.google.com/search?source=hp&ei=uc8gXdSGFLOD8gK8r5qACA&q=" + searchText).get();
//                Element link = document
//                        .select("article[itemprop=articleBody] p.interstitial-link i a")
//                        .first();
//
//                String linkText = link.ownText();
//
//                String linkURL = link.absUrl("href");
//
//                Log.d("LINKIT", linkURL);
////                if (keyword.equals("The Blue Mosque")) {
////                    keyword = "Sultan Ahmed Mosque";
////                }
////
////                String wikipediaURL = keyword;
////                String wikipediaApiJSON = "https://www.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="
////                        + URLEncoder.encode(wikipediaURL.substring(wikipediaURL.lastIndexOf("/") + 1, wikipediaURL.length()), encoding);
////
////                //"extract":" the summary of the article
////                HttpURLConnection httpcon = (HttpURLConnection) new URL(wikipediaApiJSON).openConnection();
////                httpcon.addRequestProperty("User-Agent", "Mozilla/5.0");
////                BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
////
////                String responseSB = in.readLine();
////
////                in.close();
////                if (responseSB.split("extract\":\"").length > 1) {
////
////                    String result = responseSB.split("extract\":\"")[1];
////                    result = result.replaceAll("[-+.^:,;(){}\']", "");
////                    result = result.replaceAll("[0-9]", "");
////                    result = result.replaceAll("\\\\[.*?\\\\]", "");
////                    result = result.replaceAll("\\\\", "");
////                    result = result.replaceAll("tuuufubfubl", "");
////                    return result;
////                }
//
//                return "Nothing found";
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            landmarkHistoryTextView.setText(wikipediaResult);
//        }
//    }

    /*---------------------------------------------------------------------- Firebase ----------------------------------------------------------------------*/

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

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}





































