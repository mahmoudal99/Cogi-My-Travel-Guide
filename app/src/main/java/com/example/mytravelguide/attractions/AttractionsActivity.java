package com.example.mytravelguide.attractions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.vision.L;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.json.Json;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AttractionsActivity extends AppCompatActivity implements OnMapReadyCallback {

    ImageView backArrow;
    CardView europeCardView,africaCardView,asiaCardView, americaCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        Handler handler = new Handler();
        GeocodingLocation geocodingLocation = new GeocodingLocation();
        geocodingLocation.getAddressFromLocation("Wadi Musa", AttractionsActivity.this, handler);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//
//        init();
//        setUpWidgets();
    }

    // Include the OnCreate() method here too, as described above.
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(48.8539241, 2.2913515);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));
        googleMap.setMaxZoomPreference(12);
        googleMap.setMinZoomPreference(12);
    }

//    private void init(){
//        backArrow = findViewById(R.id.backArrow);
//        europeCardView = findViewById(R.id.attractionsCardEurope);
//        africaCardView = findViewById(R.id.attractionsCardAfrica);
//        asiaCardView = findViewById(R.id.attractionsCardAsia);
//        americaCardView = findViewById(R.id.attractionsCardAmerica);
//    }

//    private void setUpWidgets(){
//        backArrow.setOnClickListener(v -> {
//            Intent backIntent = new Intent(AttractionsActivity.this, HomePageActivity.class);
//            startActivity(backIntent);
//        });
//
//        europeCardView.setOnClickListener(v -> startActivity(new Intent(AttractionsActivity.this, ContinentAttractionsActivity.class).putExtra("Continent", "Europe")));
//
//        asiaCardView.setOnClickListener(v -> startActivity(new Intent(AttractionsActivity.this, ContinentAttractionsActivity.class).putExtra("Continent", "Asia")));
//
//        americaCardView.setOnClickListener(v -> startActivity(new Intent(AttractionsActivity.this, ContinentAttractionsActivity.class).putExtra("Continent", "America")));
//
//        africaCardView.setOnClickListener(v -> startActivity(new Intent(AttractionsActivity.this, ContinentAttractionsActivity.class).putExtra("Continent", "Africa")));
//    }
}






























