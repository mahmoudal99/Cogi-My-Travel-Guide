package com.example.mytravelguide.attractions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.TravelGuideActivity;
import com.example.mytravelguide.WikiData;
import com.example.mytravelguide.models.AttractionObject;
import com.example.mytravelguide.utils.GooglePlacesApi;
import com.example.mytravelguide.utils.ImageProcessing;
import com.example.mytravelguide.utils.JsonReader;
import com.example.mytravelguide.utils.Model;
import com.example.mytravelguide.utils.SearchAdapter;
import com.example.mytravelguide.utils.SwipeViewAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;
import com.kc.unsplash.Unsplash;
import com.kc.unsplash.models.SearchResults;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CitiesActivity extends AppCompatActivity implements OnMapReadyCallback, SearchAdapter.LandmarkAdapterListener {

    private static final String PALACE = "Q16560";
    private static final String TOWER = "Q12518";
    private static final String CASTLE = "Q23413";
    private static final String TOURISTATTRACTION = "Q570116";
    private static final String ARCHAELOGICALSITE = "Q570116";
    private static final String MOSQUE = "Q32815";
    private static final String TEMPLE = "Q44539";
    private static final String CHURCH = "Q16970";
    private static final String SYNAGOGUE = "Q34627";

    private static final String WIKIDATAREQUEST = "WIKIDATAREQUEST";
    private static final String LANDMARKREQUEST = "LANDMARKREQUEST";
    private static final String CITYLATLNGREQUEST = "CITYLATLNGREQUEST";
    private static final String LANDMARKIDREQUEST = "LANDMARKIDREQUEST";
    private static final String CITYPOPULATIONREQUEST = "CITYPOPULATIONREQUEST";

    // Widgets
    private ImageView backArrow, searchImageView, cityImage, closeSearchArrow, blackSearchButton, noCityImage;
    private CardView mapCardView, searchBarCardView;
    private RecyclerView listView;
    private TextView cityTextView, searchPlacesEditText, noLandmarkSelected;
    private EditText searchEditText;
    private TabLayout tabLayout;

    ViewPager viewPager;
    SwipeViewAdapter adapter;
    List<Model> models;

    private OkHttpClient okHttpClient;

    // Variables
    List<AttractionObject> landmarksArrayList = new ArrayList<>();

    // Classes
    GoogleMap mGoogleMap;
    WikiData wikiData;
    ImageProcessing imageProcessing;
    SearchAdapter mAdapter = new SearchAdapter(CitiesActivity.this, landmarksArrayList, this);

    //Shared Preference
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        wikiData = new WikiData();
        isStoragePermissionGranted();

        okHttpClient = new OkHttpClient();
        init();
        toggleCityImageVisibility();
        setUpTabs();
        setUpWidgets();
        imageProcessing.loadImageFromStorage(cityImage);
        handleIncomingIntent(cityTextView.getText().toString());
        supportMapFragment();
        if(getIntent().hasExtra("cityName")){
            String cityName = getIntent().getStringExtra("cityName");
            handleIncomingIntent(cityName);
        }
    }

    private void createModels(String[] imageStrings) {
        models = new ArrayList<>();
        models.add(new Model(imageStrings[0]));
        models.add(new Model(imageStrings[1]));
        models.add(new Model(imageStrings[2]));
        models.add(new Model(imageStrings[3]));
        callSwipeViewAdapter();
    }

    private void callSwipeViewAdapter() {
        adapter = new SwipeViewAdapter(models, this);
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

    private void init() {
        tabLayout = findViewById(R.id.tab_layout);
        noLandmarkSelected = findViewById(R.id.noLandmarkSelected);
        searchPlacesEditText = findViewById(R.id.searchPlacesEditText);
        searchBarCardView = findViewById(R.id.searchBarCardView);
        closeSearchArrow = findViewById(R.id.closeSearchArrow);
        blackSearchButton = findViewById(R.id.blackSearchButton);
        mapCardView = findViewById(R.id.mapCardView);
        listView = findViewById(R.id.landmarksInCity);
        backArrow = findViewById(R.id.backArrow);
        noCityImage = findViewById(R.id.noCityImage);
        cityTextView = findViewById(R.id.cityTextView);
        searchEditText = findViewById(R.id.searchTextView);
        searchEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchPlacesEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchImageView = findViewById(R.id.searchButton);
        imageProcessing = new ImageProcessing(CitiesActivity.this);
        cityImage = findViewById(R.id.cityImage);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.apply();
    }

    private void setUpWidgets() {
        backArrow.setOnClickListener(v -> {
            Intent backIntent = new Intent(CitiesActivity.this, HomePageActivity.class);
            startActivity(backIntent);
        });

        searchImageView.setOnClickListener(v -> toggleSearchWidgets(searchEditText.getVisibility()));

        closeSearchArrow.setOnClickListener(v -> {
            if (closeSearchArrow.getVisibility() == View.VISIBLE) {
                hideSearchWidgets();
            }
        });

        searchEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            String cityName = searchEditText.getText().toString();
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String cityLat = pref.getString("CityLat", null);
                if (cityLat == null) {
                    cityTextView.setText(getResources().getString(R.string.city));
                } else {
                    cityTextView.setText(cityName);
                }

                handleCitySearchResult(cityName);
                return true;
            }
            return false;
        });

        searchPlacesEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        searchPlacesEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                mAdapter.getFilter().filter(searchPlacesEditText.getText().toString());
                closeKeyboard();
            }
            return false;
        });

        cityTextView.setText(pref.getString("CityName", "City"));
    }

    private void toggleCityImageVisibility() {
        String cityLat = pref.getString("CityLat", null);
        if (cityLat == null) {
            cityImage.setVisibility(View.INVISIBLE);
            noCityImage.setVisibility(View.VISIBLE);
            mapCardView.setVisibility(View.INVISIBLE);
            cityTextView.setText(getResources().getString(R.string.city));
        } else {
            cityImage.setVisibility(View.VISIBLE);
            noCityImage.setVisibility(View.INVISIBLE);
            mapCardView.setVisibility(View.VISIBLE);
        }
    }

    // WikiData
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
                    case LANDMARKREQUEST:
                        JsonReader jsonReader = new JsonReader();
                        List<String> landmarks = jsonReader.getLandmarksInCityFromJson(requestReponse);
                        landmarksInCity(landmarks);
                        break;
                    case WIKIDATAREQUEST:
                        String result = wikiData.getCityWikiDataID(requestReponse);
                        getCityLandmarks(result);
                        break;
                    case CITYLATLNGREQUEST:
                        Request cityLatLngRequest = wikiData.getCityLatLng(requestReponse);
                        getCityLatLngFromJson(cityLatLngRequest);
                        break;
                    case LANDMARKIDREQUEST:
                        jsonReader = new JsonReader();
                        JSONObject placeIDObject = jsonReader.getLandmarkPlaceIDFromJson(requestReponse);
                        try {
                            openSelectedLandmark(placeIDObject.get("place_id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case CITYPOPULATIONREQUEST:
                        getCityPopulationFromJson(requestReponse);
                        break;
                }
            }
        });
    }

    public void getCityLandmarks(String cityWikiDataID) {
        String[] types = {PALACE, TOWER, CASTLE, TOURISTATTRACTION, ARCHAELOGICALSITE, MOSQUE, TEMPLE, CHURCH, SYNAGOGUE};
        for (int i = 0; i < 9; i++) {
            String getPlacesInCityURL = "https://query.wikidata.org/sparql?format=json&query=%0ASELECT+DISTINCT+%3Fitem+%3Fname+%3Fcoord+%3Flat+%3Flon%0AWHERE+%7B%0A+++hint%3AQuery+hint%3Aoptimizer+%22None%22+.%0A+++%3Fitem+wdt%3AP131%2A+wd%3A" + cityWikiDataID + "+.%0A+++%3Fitem+wdt%3AP31%2Fwdt%3AP279%2A+wd%3A" + types[i] + "+.%0A+++%3Fitem+wdt%3AP625+%3Fcoord+.%0A+++%3Fitem+p%3AP625+%3Fcoordinate+.%0A+++%3Fcoordinate+psv%3AP625+%3Fcoordinate_node+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLatitude+%3Flat+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLongitude+%3Flon+.%0A+++SERVICE+wikibase%3Alabel+%7B%0A++++bd%3AserviceParam+wikibase%3Alanguage+%22%5BAUTO_LANGUAGE%5D%2Cen%22+.%0A++++%3Fitem+rdfs%3Alabel+%3Fname%0A+++%7D%0A%7D%0AORDER+BY+ASC+%28%3Fname%29%0A";
            Request cityLandmarksRequest = new Request.Builder().url(getPlacesInCityURL).header("content-type", "application/html").build();
            httpClientCall(cityLandmarksRequest, LANDMARKREQUEST);
        }
    }

    private void landmarksInCity(List<String> landmarks) {

        for (String landmark : landmarks) {
            Log.d("LANDMARKS3", landmark);
            AttractionObject attractionObject = new AttractionObject();
            attractionObject.setPlaceName(landmark);
            if (Pattern.compile("[0-9]").matcher(landmark).find()) {
                Log.d("Invalid Landmark", "Not added");
            } else {
                Log.d("ISADDED ", "ADDED");
                landmarksArrayList.add(attractionObject);
            }
        }
        Log.d("ISADDED ", "Finished" +  landmarksArrayList.size());
        listView = findViewById(R.id.landmarksInCity);
        loadNearByLocations(mAdapter, landmarksArrayList);
    }

    private void loadNearByLocations(SearchAdapter adapter, List<AttractionObject> landmarksArrayList1) {
        runOnUiThread(() -> {
            if(landmarksArrayList1.size() == 0){
                Log.d("HEUIHIUDIU", "REPLAY" + landmarksArrayList1.size());
                handleIncomingIntent(cityTextView.getText().toString());
            }else {
                Log.d("HEUIHIUDIU", "ADAPTER" + landmarksArrayList1.size());
                SearchAdapter mAdapter = new SearchAdapter(CitiesActivity.this, landmarksArrayList1, CitiesActivity.this);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(CitiesActivity.this);
                listView.setLayoutManager(mLayoutManager);
                listView.setAdapter(adapter);
                mAdapter.notifyDataSetChanged();
            }

        });
    }

    // JSON Calls
    public void getCityLatLngFromJson(Request request) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String requestResponse = response.body().string();
                mapsLatLngResponse(requestResponse);
            }
        });
    }

    private void mapsLatLngResponse(String response) {
        JsonReader jsonReader = new JsonReader();
        double[] doubles = jsonReader.getMapsLatLngFromJson(response);
        setMapsLatLng(doubles[0], doubles[1]);
    }

    private void getCityPopulationFromJson(String response) {
        JsonReader jsonReader = new JsonReader();
        String[] landmarksArray;
        List<String> arrayList = new ArrayList<>();
        List<String> cityInformationList = jsonReader.getCityPopulationFromJson(response);

        if (cityInformationList.get(0).contains("null")) {
            // Load Landmarks
            Log.d("LOADING", "Landmarks");
            String landmarksString = cityInformationList.get(1);
            landmarksArray = landmarksString.split(", ");
            arrayList.addAll(Arrays.asList(landmarksArray));
            landmarksInCity(arrayList);
        } else {
            // Get Population
            Log.d("LOADING", "Population");
            if (cityInformationList.get(0).length() >= 7) {
                String population = "1000000";
                Request request = wikiData.getCityDataId(cityTextView.getText().toString(), population);
                httpClientCall(request, WIKIDATAREQUEST);
            } else {
                StringBuilder population = new StringBuilder(cityInformationList.get(0));
                for (int i = 1; i < cityInformationList.get(0).length(); i++) {
                    population.setCharAt(i, '0');
                }
                Request request = wikiData.getCityDataId(cityTextView.getText().toString(), population.toString());
                httpClientCall(request, WIKIDATAREQUEST);
            }
        }
    }

    // Map Fragment
    @Override
    public void onMapReady(GoogleMap googleMap) {
        double cityLat = Double.parseDouble(Objects.requireNonNull(pref.getString("CityLat", "0.0")));
        double cityLng = Double.parseDouble(Objects.requireNonNull(pref.getString("CityLng", "0.0")));
        LatLng cityLatLng = new LatLng(cityLat, cityLng);
        mGoogleMap = googleMap;
        mGoogleMap.addMarker(new MarkerOptions().position(cityLatLng).title(cityTextView.getText().toString()));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(cityLatLng));
        mGoogleMap.setMaxZoomPreference(11);
        mGoogleMap.setMinZoomPreference(11);
    }

    private void supportMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setMapsLatLng(double lat, double lng) {
        runOnUiThread(() -> {
            LatLng latLng = new LatLng(lat, lng);
            mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(cityTextView.getText().toString()));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        });

        editor.putString("CityLat", String.valueOf(lat));
        editor.putString("CityLng", String.valueOf(lng));
        editor.commit();
    }

    // Tabs
    private void setUpTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.map)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.landmarks)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        setUpViewager();
    }

    private void setUpViewager() {
        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    showMapTabComponents();
                } else if (tab.getPosition() == 1) {
                    showLandmarksTabComponents();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showMapTabComponents() {
        listView.setVisibility(View.GONE);
        searchPlacesEditText.setVisibility(View.GONE);
        blackSearchButton.setVisibility(View.GONE);
        searchBarCardView.setVisibility(View.GONE);
        String cityLat = pref.getString("CityLat", null);
        if (cityLat == null) {
            mapCardView.setVisibility(View.INVISIBLE);
        } else {
            mapCardView.setVisibility(View.VISIBLE);
        }
    }

    private void showLandmarksTabComponents() {
        mapCardView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        searchPlacesEditText.setVisibility(View.VISIBLE);
        blackSearchButton.setVisibility(View.VISIBLE);
        searchBarCardView.setVisibility(View.VISIBLE);
    }

    // Search
    private void toggleSearchWidgets(int visibilityValue) {
        if (visibilityValue == View.GONE) {
            cityTextView.setVisibility(View.GONE);
            searchEditText.setVisibility(View.VISIBLE);
            closeSearchArrow.setVisibility(View.VISIBLE);
            backArrow.setVisibility(View.GONE);
        } else {
            cityTextView.setVisibility(View.VISIBLE);
            searchEditText.setVisibility(View.GONE);
            closeSearchArrow.setVisibility(View.GONE);
            backArrow.setVisibility(View.VISIBLE);
        }
    }

    private void hideSearchWidgets() {
        cityTextView.setVisibility(View.VISIBLE);
        searchEditText.setVisibility(View.GONE);
        closeSearchArrow.setVisibility(View.GONE);
        backArrow.setVisibility(View.VISIBLE);
    }

    private void handleIncomingIntent(String cityName){
        Request request = wikiData.callCityDataApi(cityName);
        httpClientCall(request, CITYPOPULATIONREQUEST);
        GooglePlacesApi googlePlacesApi = new GooglePlacesApi("AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o", CitiesActivity.this);
        if(cityName.contains("Petra")){
            String url = googlePlacesApi.getPlacesByQuery(cityName + ", Jordan");
            Request latLngReqiest = wikiData.getCityLatLng(url);
            getCityLatLngFromJson(latLngReqiest);
        }
        editor.putString("CityName", cityName);
        setCityImage(cityName);
        cityTextView.setText(cityName);
        runOnUiThread(() -> {
            cityTextView.setVisibility(View.VISIBLE);
            searchEditText.setVisibility(View.GONE);
        });
        noLandmarkSelected.setVisibility(View.GONE);
        landmarksArrayList.clear();
    }

    private void handleCitySearchResult(String cityName) {
        closeSearchArrow.setVisibility(View.GONE);
        backArrow.setVisibility(View.VISIBLE);
        Request request = wikiData.callCityDataApi(cityName);
        httpClientCall(request, CITYPOPULATIONREQUEST);
        GooglePlacesApi googlePlacesApi = new GooglePlacesApi("AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o", CitiesActivity.this);
        String url = googlePlacesApi.getPlacesByQuery(cityName);
        if(cityName.contains("Petra")){
            setMapsLatLng(30.328, 35.444);
        }else {
            Request latLngReqiest = wikiData.getCityLatLng(url);
            getCityLatLngFromJson(latLngReqiest);
        }

        closeKeyboard();
        toggleCityImageVisibility();
        editor.putString("CityName", cityName);
        setCityImage(cityName);
        runOnUiThread(() -> {
            cityTextView.setVisibility(View.VISIBLE);
            searchEditText.setVisibility(View.GONE);
        });
        noLandmarkSelected.setVisibility(View.GONE);
        landmarksArrayList.clear();
    }

    private void setCityImage(String cityName) {
        Unsplash unsplash = new Unsplash("73a58cad473ac4376a1ed2c4f27cfeb08cfa77e8492f4cdfc2814085794d6100");
        unsplash.searchPhotos(cityName, new Unsplash.OnSearchCompleteListener() {
            @Override
            public void onComplete(SearchResults results) {
                String[] cityImageStrings = new String[4];
                cityImageStrings[0] = results.getResults().get(0).getUrls().getRegular();
                cityImageStrings[1] = results.getResults().get(1).getUrls().getRegular();
                cityImageStrings[2] = results.getResults().get(2).getUrls().getRegular();
                cityImageStrings[3] = results.getResults().get(3).getUrls().getRegular();
                createModels(cityImageStrings);
            }

            @Override
            public void onError(String error) {
                Log.d("Unsplash Error", error);
            }
        });
    }

    private void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    // Landmark Selected
    @Override
    public void onLandmarkSelected(AttractionObject place) {
        GooglePlacesApi googlePlacesApi = new GooglePlacesApi("AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o", CitiesActivity.this);
        Request request = wikiData.createLandmarkPlaceIdRequest(googlePlacesApi.getPlacesByQuery(place.getPlaceName()));
        httpClientCall(request, LANDMARKIDREQUEST);
    }

    public void openSelectedLandmark(String landmarkId) {
        Intent intent = new Intent(CitiesActivity.this, TravelGuideActivity.class);
        intent.putExtra("landmarkID", landmarkId);
        startActivity(intent);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission", "Permission is granted");
                return true;
            } else {
                Log.v("Permission", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v("Permission", "Permission is granted");
            return true;
        }
    }

}