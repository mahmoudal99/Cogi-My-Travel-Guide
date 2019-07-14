package com.example.mytravelguide.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GooglePlacesQuery {

    public GooglePlacesQuery() {
    }

    // Google Search Async Task
    public class GooglePlacesQueryAsyncTask extends AsyncTask<URL, Integer, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String json = null;
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/place/textsearch/json?query=Empire+State+Building&key=AIzaSyDUBqf6gebSlU8W7TmX5Y2AsQlQL1ure5o");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");
                connection.connect();
                InputStream inStream = connection.getInputStream();
                String text = new Scanner(inStream, "UTF-8").useDelimiter("\\Z").next();
                Log.d("ONGOD2", text);
                JSONObject jsonObj = new JSONObject(text);
                JSONArray contacts = jsonObj.getJSONArray("results");
                for (int i = 0; i < contacts.length(); i++) {
                    JSONObject c = contacts.getJSONObject(i);
                    String id = c.getString("place_id");
                    Log.d("ONGOD3", id);
                }

            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
            }
            return json;
        }
    }
}
