package com.example.mytravelguide;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.example.mytravelguide.attractions.AttractionsActivity;
import com.example.mytravelguide.models.ImageModel;
import com.example.mytravelguide.utils.GooglePlacesQuery;
import com.example.mytravelguide.utils.SlidingImageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = "HomePageActivity";

    private CardView attractionsCard, travelGuideCard, timelineCard;
    private ImageView settings;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    Handler handler;
    Runnable Update;
    Timer swipeTimer;

    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private ArrayList<ImageModel> imageModelArrayList;

    private int[] myImageList = new int[]{R.drawable.sphinx, R.drawable.taj_mahal, R.drawable.petra, R.drawable.alhambra};
    private String[] imageNames = new String[]{"Sphinx of Giza", "Taj Mahal", "Petra", "Alhambra"};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GooglePlacesQuery googlePlacesQuery = new GooglePlacesQuery();
        googlePlacesQuery.new GooglePlacesQueryAsyncTask().execute();
        imageModelArrayList = new ArrayList<>();
        imageModelArrayList = populateList();
        loadLocale();
        setContentView(R.layout.activity_home_page);
        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
        initViewPager();
    }

    private ArrayList<ImageModel> populateList(){

        ArrayList<ImageModel> list = new ArrayList<>();

        for(int i = 0; i < 4; i++){
            ImageModel imageModel = new ImageModel();
            imageModel.setImage_drawable(myImageList[i]);
            imageModel.setImage_text(imageNames[i]);
            list.add(imageModel);
        }

        return list;
    }

    private void init() {
        attractionsCard = findViewById(R.id.attractionsCard);
        travelGuideCard = findViewById(R.id.travelGuideCard);
        timelineCard = findViewById(R.id.timelineCard);
        settings = findViewById(R.id.settings);
        authentication = FirebaseAuth.getInstance();
    }

    private void initViewPager() {

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new SlidingImageAdapter(HomePageActivity.this,imageModelArrayList));

        NUM_PAGES =imageModelArrayList.size();
        currentPage = 0;

        // Auto start of viewpager
        handler = new Handler();
        Update = () -> {
            if (currentPage == NUM_PAGES) {
                currentPage = 0;
            }
            mPager.setCurrentItem(currentPage++, true);
        };
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 4000, 3000);

    }

    private void setUpWidgets() {
        attractionsCard.setOnClickListener(v -> {
            Intent attractionsIntent = new Intent(HomePageActivity.this, AttractionsActivity.class);
            startActivity(attractionsIntent);
        });

        travelGuideCard.setOnClickListener(v -> {
            Intent travelGuideIntent = new Intent(HomePageActivity.this, TravelGuideActivity.class);
            startActivity(travelGuideIntent);
        });

        timelineCard.setOnClickListener(v -> {
            Intent visitedIntent = new Intent(HomePageActivity.this, TimelineActivity.class);
            startActivity(visitedIntent);
        });

    }

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

    //---------- Firebase ----------//
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
        handler.removeCallbacks(Update);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            authentication.removeAuthStateListener(authStateListener);
        }
        swipeTimer.cancel();
        handler.removeCallbacks(Update);
    }

}







































