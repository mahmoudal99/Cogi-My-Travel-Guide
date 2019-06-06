package com.example.mytravelguide.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.mytravelguide.TravelGuideActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GoogleSearchAsyncTask {

    private static final String TAG = "GoogleSearchAsyncTask";
    private Integer responseCode = null;
    private String responseMessage = "";
    private ArrayList<String> results = new ArrayList<>();

    // Google Search Async Task
    private class SearchAsyncTask extends AsyncTask<URL, Integer, String> {

        protected void onPreExecute() {
            Log.d(TAG, "AsyncTask - onPreExecute");
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            Log.d(TAG, "AsyncTask - doInBackground, url=" + url);

            // Http connection
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();

            } catch (IOException e) {
                Log.e(TAG, "Http connection ERROR " + e.toString());
            }

            try {
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
            } catch (IOException e) {
                Log.e(TAG, "Http getting response code ERROR " + e.toString());
            }

            try {
                // Variables
                String result = null;
                if (responseCode == 200) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                        if (line.contains("snippet")) {
                            results.add(line);
                        }
                    }
                    rd.close();
                    conn.disconnect();
                    result = sb.toString();

                    return result;
                } else {
                    String errorMsg = "Http ERROR response " + responseMessage + "\n" + "Make sure to replace in code your own Google API key and Search Engine ID";
                    Log.e(TAG, errorMsg);
                    result = errorMsg;
                    return result;
                }
            } catch (IOException e) {
                Log.e(TAG, "Http Response ERROR " + e.toString());
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            Log.d(TAG, "AsyncTask - onProgressUpdate, progress=" + progress);
        }

        protected void onPostExecute(String result) {

            Log.d(TAG, "AsyncTask - onPostExecute, result=" + result);

            for (int i = 0; i < results.size(); i++) {
                Log.d(TAG, results.get(i));
            }

//            StringBuffer sb = new StringBuffer(results.get(1).length());
//            sb.delete(0, 9);
//
//            String res = sb.toString();
//            res.replace(":", "");
//            res = results.get(2).substring(12, results.get(2).length() - 1);
//            imageURL = res;
//            new TravelGuideActivity.DownloadImageTask((ImageView) findViewById(R.id.attractionImage)).execute(res);
        }
    }

    // Download Image Async Task
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
//            Glide.with(TravelGuideActivity.this).load(result).into(bmImage);
        }
    }

}
