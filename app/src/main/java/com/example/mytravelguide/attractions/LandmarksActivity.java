package com.example.mytravelguide.attractions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.example.mytravelguide.HomePageActivity;
import com.example.mytravelguide.R;
import com.example.mytravelguide.utils.GeocodingLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class LandmarksActivity extends AppCompatActivity implements OnMapReadyCallback {

    ImageView backArrow;
    CardView europeCardView, africaCardView, asiaCardView, americaCardView;

    private String cityName;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        Handler handler = new Handler();
        GeocodingLocation geocodingLocation = new GeocodingLocation();
        geocodingLocation.getAddressFromLocation("Wadi Musa", LandmarksActivity.this, handler);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
        setUpWidgets();
        getCityDataId();
    }

    // Include the OnCreate() method here too, as described above.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(48.8539241, 2.2913515);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        googleMap.setMaxZoomPreference(12);
        googleMap.setMinZoomPreference(12);
    }

    private void init() {
        okHttpClient = new OkHttpClient();
    }

    private void getCityDataId() {
        String url = "https://wft-geo-db.p.mashape.com/v1/geo/cities?namePrefix=" + cityName + "&minPopulation=1000";
        Request cityDataIDRequest = new Request.Builder()
                .url(url)
                .header("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .header("X-RapidAPI-Key", "de22d3cbadmshf632b8fa723db10p12a5e2jsnecd78f4ef9d6")
                .build();
        httpClientCall(cityDataIDRequest);
    }

    private void httpClientCall(Request request) {
        okHttpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("City WIKIDATA Id" + "Error", e.getMessage());
            }
            @Override
            public void onResponse(Response response) throws IOException {
                final String myResponse = response.body().string();
                Log.d("City WIKIDATA Id", myResponse);
            }
        });
    }

    private void setUpWidgets() {
//        backArrow.setOnClickListener(v -> {
//            Intent backIntent = new Intent(LandmarksActivity.this, HomePageActivity.class);
//            startActivity(backIntent);
//        });
    }
}






























