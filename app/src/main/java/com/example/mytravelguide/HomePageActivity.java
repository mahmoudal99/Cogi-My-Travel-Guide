package com.example.mytravelguide;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.example.mytravelguide.attractions.AttractionsActivity;
import com.example.mytravelguide.settings.SettingsActivity;
import com.google.android.gms.vision.L;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = "HomePageActivity";

    private CardView attractionsCard, travelGuideCard, timelineCard;
    private ImageView settings;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_home_page);
        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
    }

    private void init() {
        attractionsCard = findViewById(R.id.attractionsCard);
        travelGuideCard = findViewById(R.id.travelGuideCard);
        timelineCard = findViewById(R.id.timelineCard);
        settings = findViewById(R.id.settings);
        authentication = FirebaseAuth.getInstance();
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

        settings.setOnClickListener(v -> startActivity(new Intent(HomePageActivity.this, SettingsActivity.class)));
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

//    private class WikiApi extends AsyncTask<String, Integer, String> {
//        @Override
//        protected String doInBackground(String... strings) {
//
//            String keyword = "Eiffel Tower";
//            keyword = keyword.replaceAll(" ", "+");
//            String searchText = keyword + "+wikipedia";
//            Document document = null;
//            try {
//                document = Jsoup.connect("https://www.google.com/search?source=hp&ei=uc8gXdSGFLOD8gK8r5qACA&q=" + searchText).get();
//                ArrayList<Element> elements = document.getAllElements();
//
//                List<String> containedUrls = new ArrayList<String>();
//                String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
//                Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
//                Matcher urlMatcher = pattern.matcher(document.getAllElements().get(0).text());
//
//                while (urlMatcher.find())
//                {
//                    containedUrls.add(document.getAllElements().get(0).text().substring(urlMatcher.start(0),
//                            urlMatcher.end(0)));
//                }
//
//                Log.d("LINKIT", containedUrls.get(0));
//
////                Document google = Jsoup.connect(containedUrls.get(0)).get();
//                Document google = Jsoup.connect(containedUrls.get(0)).get();
//                Log.d("RESULTWIKI13", google.getElementsByTag("div").text());
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return "Nothing found";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            String yes = result;
//        }
//    }

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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            authentication.removeAuthStateListener(authStateListener);
        }
    }
}







































