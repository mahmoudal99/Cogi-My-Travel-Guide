package com.example.mytravelguide.attractions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.mytravelguide.BuildConfig;
import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.TravelGuideActivity;
import com.example.mytravelguide.models.AttractionObject;
import com.example.mytravelguide.utils.GeocodingLocation;
import com.example.mytravelguide.utils.GooglePlacesApi;
import com.example.mytravelguide.utils.ImageProcessing;
import com.example.mytravelguide.utils.LandmarksInCityAdapter;
import com.example.mytravelguide.utils.NearByLocationsAdapter;
import com.example.mytravelguide.utils.NewAdapter;
import com.example.mytravelguide.utils.TabsAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.L;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.tabs.TabLayout;
import com.kc.unsplash.Unsplash;
import com.kc.unsplash.models.Photo;
import com.kc.unsplash.models.SearchResults;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.regex.Pattern;

public class LandmarksActivity extends AppCompatActivity implements OnMapReadyCallback, NewAdapter.LandmarkAdapterListener {

    private ImageView backArrow, search, cityImage;
    private CardView mapCardView;
    private RecyclerView listView;
    private TextView cityTextView;
    private EditText searchTextView;

    private double lat, lng;
    private OkHttpClient okHttpClient;
    List<AttractionObject> landmarksArrayList = new ArrayList<>();
    GoogleMap mGoogleMap;

    ImageProcessing imageProcessing;

    NewAdapter mAdapter = new NewAdapter(LandmarksActivity.this, landmarksArrayList, this);;

    SharedPreferences pref;
    SharedPreferences.Editor editor;


    EditText editText;
    ImageView imageView;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("Landmarks"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    mapCardView.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                } else if (tab.getPosition() == 1) {
                    mapCardView.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Handler handler = new Handler();
        GeocodingLocation geocodingLocation = new GeocodingLocation();
        geocodingLocation.getAddressFromLocation("Wadi Musa", LandmarksActivity.this, handler);
        init();
        setUpWidgets();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imageProcessing.loadImageFromStorage(cityImage);
        getCityDataId(cityTextView.getText().toString());
        searchListner();
    }

    // Include the OnCreate() method here too, as described above.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        double landmarkLat = Double.parseDouble(pref.getString("LandmarkLat", "0.0"));
        double landmarkLng = Double.parseDouble(pref.getString("LandmarkLng", "0.0"));
        LatLng sydney = new LatLng(landmarkLat, landmarkLng);
        mGoogleMap = googleMap;
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mGoogleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        mGoogleMap.setMaxZoomPreference(12);
        mGoogleMap.setMinZoomPreference(12);
    }

    private void init() {
        imageView = findViewById(R.id.landmarkSearch);
        editText = findViewById(R.id.edit_query);
        okHttpClient = new OkHttpClient();
        mapCardView = findViewById(R.id.mapCardView);
        listView = findViewById(R.id.landmarksInCity);
        backArrow = findViewById(R.id.backArrow);
        cityTextView = findViewById(R.id.cityTextView);
        searchTextView = findViewById(R.id.searchTextView);
        searchTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        search = findViewById(R.id.search);
        imageProcessing = new ImageProcessing(LandmarksActivity.this);
        cityImage = findViewById(R.id.cityImage);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.apply();
    }

    private void getCityDataId(String cityName) {
        String url = "https://wft-geo-db.p.mashape.com/v1/geo/cities?namePrefix=" + cityName + "&minPopulation=800000";
        Request cityDataIDRequest = new Request.Builder()
                .url(url)
                .header("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .header("X-RapidAPI-Key", "de22d3cbadmshf632b8fa723db10p12a5e2jsnecd78f4ef9d6")
                .build();
        httpClientCall(cityDataIDRequest, "WIKIDATA");
        landmarksArrayList.clear();
    }

    private void getCityLandmarks(String cityWikiDataID) {
        String cityDataId = cityWikiDataID;
        String[] types = {"Q16560", "Q12518", "Q23413", "Q570116", "Q839954", "Q32815", "Q44539", "Q16970", "Q34627"};
        String typePalace = "Q16560";
        String typeTower = "Q12518";
        String typeCastle = "Q23413";
        String typeTouristAttraction = "Q570116";
        String typeArchaeologicalSite = "Q839954";

        for (int i = 0; i < 9; i++) {
            String url1 = "https://query.wikidata.org/sparql?format=json&query=%0ASELECT+DISTINCT+%3Fitem+%3Fname+%3Fcoord+%3Flat+%3Flon%0AWHERE+%7B%0A+++hint%3AQuery+hint%3Aoptimizer+%22None%22+.%0A+++%3Fitem+wdt%3AP131%2A+wd%3A" + cityDataId + "+.%0A+++%3Fitem+wdt%3AP31%2Fwdt%3AP279%2A+wd%3A" + types[i] + "+.%0A+++%3Fitem+wdt%3AP625+%3Fcoord+.%0A+++%3Fitem+p%3AP625+%3Fcoordinate+.%0A+++%3Fcoordinate+psv%3AP625+%3Fcoordinate_node+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLatitude+%3Flat+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLongitude+%3Flon+.%0A+++SERVICE+wikibase%3Alabel+%7B%0A++++bd%3AserviceParam+wikibase%3Alanguage+%22%5BAUTO_LANGUAGE%5D%2Cen%22+.%0A++++%3Fitem+rdfs%3Alabel+%3Fname%0A+++%7D%0A%7D%0AORDER+BY+ASC+%28%3Fname%29%0A";
            Request cityLandmarksRequest = new Request.Builder().url(url1).header("content-type", "application/html").build();

            httpClientCall(cityLandmarksRequest, "LANDMARKSREQEST");
        }

    }

    private void getCityLatLng(String cityJsonUrl) {
        Request cityLatLngRequest = new Request.Builder().url(cityJsonUrl).header("content-type", "application/html").build();
        getCityLatLngFromJson(cityLatLngRequest);
    }

    private void getCityID(String cityURL) {
        Request cityLatLngRequest = new Request.Builder().url(cityURL).header("content-type", "application/html").build();
        getCityIDFromJson(cityLatLngRequest);
    }

    private void httpClientCall(Request request, String requestType) {

        okHttpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("City WIKIDATA Id" + "Error", e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String myResponse = response.body().string();
                if (requestType.equals("LANDMARKSREQEST")) {
                    landmarksInCityFromJson(myResponse);
                } else if (requestType.equals("WIKIDATA")) {
                    getCityIDFromJson(myResponse);
                } else if (requestType.equals("CITYREQUEST")) {
                    getCityLatLng(myResponse);
                }
            }
        });
    }

    private void landmarksInCityFromJson(String response) {
        ArrayList<String> landmarks = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            String value = jsonObject.getString("results");
            jsonObject = new JSONObject(value);
            JSONArray jsonArray = jsonObject.getJSONArray("bindings");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject finalObject = jsonArray.getJSONObject(i);
                jsonObject = new JSONObject(finalObject.getString("name"));

                landmarks.add(jsonObject.get("value").toString());

            }
            Set<String> hs1 = new LinkedHashSet<>(landmarks);
            List<String> al2 = new ArrayList<>(hs1);
            landmarksInCity(al2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCityIDFromJson(String respnse) {
        try {
            JSONObject jsonObject = new JSONObject(respnse);
            JSONArray data = jsonObject.getJSONArray("data");
            Log.d("getCityIDFromJson", data.getJSONObject(0).get("wikiDataId").toString());
            getCityLandmarks(data.getJSONObject(0).get("wikiDataId").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCityLatLngFromJson(Request request) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String myResponse = response.body().string();
                JSONObject jsonObject = null;
                JSONObject placeIDObject = null;
                try {
                    jsonObject = new JSONObject(myResponse);
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    jsonObject = new JSONObject(jsonArray.get(0).toString());
                    jsonObject = jsonObject.getJSONObject("geometry");
                    jsonObject = jsonObject.getJSONObject("location");
                    Log.d("LATLNG", jsonObject.get("lat") + " " + jsonObject.get("lng") + "f");

                    setMapsLatLng((double) jsonObject.get("lat"), (double) jsonObject.get("lng"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getCityIDFromJson(Request request) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String myResponse = response.body().string();
                JSONObject placeIDObject = null;
                try {
                    placeIDObject = new JSONObject(myResponse);
                    JSONArray jsonArray = placeIDObject.getJSONArray("results");
                    placeIDObject = new JSONObject(jsonArray.get(0).toString());
//                    setCitImage("ChIJD7fiBh9u5kcRYJSMaMOCCwQ");
                    Log.d("LATLNG3", placeIDObject.get("place_id").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
//
//    private void setCitImage(String placeID) {
//        runOnUiThread(() -> {
//            GooglePlacesApi googlePlacesApi = new GooglePlacesApi(BuildConfig.APIKEY);
//            Place place = googlePlacesApi.getPlaceById(placeID);
//            ImageView cityImage = findViewById(R.id.cityImage);
//            googlePlacesApi.setPhoto(place.getPhotoMetadatas().get(0), cityImage);
//        });
//    }

    private void setMapsLatLng(double lat, double lng) {
        runOnUiThread(() -> {
            LatLng sydney = new LatLng(lat, lng);
            mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        });

        editor.putString("LandmarkLat", String.valueOf(lat));
        editor.putString("LandmarkLng", String.valueOf(lng));
        editor.commit();
    }


    private void landmarksInCity(List<String> landmarks) {

        for (String landmark : landmarks) {
            Log.d("IOJOOEFIF", landmark);
            AttractionObject attractionObject = new AttractionObject();
            attractionObject.setPlaceName(landmark);
            if (Pattern.compile("[0-9]").matcher(landmark).find()) {
                Log.d("Landmark", "Not added");
            } else {
                landmarksArrayList.add(attractionObject);
            }
        }
        loadNearByLocations(landmarksArrayList);
    }

    private void loadNearByLocations(List<AttractionObject> landmarksArrayList) {

        runOnUiThread(() -> {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            listView.setLayoutManager(mLayoutManager);
            listView.setItemAnimator(new DefaultItemAnimator());
            listView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        });
    }

    private void setUpWidgets() {

        backArrow.setOnClickListener(v -> {
            Intent backIntent = new Intent(LandmarksActivity.this, HomePageActivity.class);
            startActivity(backIntent);
        });

        search.setOnClickListener(v -> {

            if (searchTextView.getVisibility() == View.GONE) {
                cityTextView.setVisibility(View.GONE);
                searchTextView.setVisibility(View.VISIBLE);
            } else {
                cityTextView.setVisibility(View.VISIBLE);
                searchTextView.setVisibility(View.GONE);
            }

        });


        searchTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchTextView.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {

                Toast.makeText(LandmarksActivity.this, searchTextView.getText(), Toast.LENGTH_SHORT).show();
                cityTextView.setText(searchTextView.getText());
                getCityDataId(searchTextView.getText().toString());
                GooglePlacesApi googlePlacesApi = new GooglePlacesApi("AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o");
                String url = googlePlacesApi.getPlacesByQuery(searchTextView.getText().toString());
                Log.d("COMOMOMO", url);
                getCityLatLng(url);
                getCityID(url);
                closeKeyboard();
                editor.putString("CityName", searchTextView.getText().toString());
                Unsplash unsplash = new Unsplash("73a58cad473ac4376a1ed2c4f27cfeb08cfa77e8492f4cdfc2814085794d6100");
                unsplash.searchPhotos(searchTextView.getText().toString(), new Unsplash.OnSearchCompleteListener() {
                    @Override
                    public void onComplete(SearchResults results) {
                        imageProcessing.new SetCityImage(cityImage).execute(results.getResults().get(0).getUrls().getFull());
                    }

                    @Override
                    public void onError(String error) {

                    }
                });

                runOnUiThread(() -> {
                    cityTextView.setVisibility(View.VISIBLE);
                    searchTextView.setVisibility(View.GONE);
                });

                return true;
            }
            return false;
        });

        cityTextView.setText(pref.getString("CityName", "Berlin"));
    }

    private void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void searchListner(){

        imageView.setOnClickListener(v -> {
            mAdapter.getFilter().filter(editText.getText().toString());
            closeKeyboard();
        });
    }

    @Override
    public void onContactSelected(AttractionObject contact) {
        Toast.makeText(getApplicationContext(), "Selected: " + contact.getPlaceName(), Toast.LENGTH_LONG).show();
    }

}






























