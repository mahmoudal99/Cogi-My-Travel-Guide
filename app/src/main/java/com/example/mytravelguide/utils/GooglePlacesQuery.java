package com.example.mytravelguide.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.google.api.client.util.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GooglePlacesQuery {

    private static final String TAG = "searchApp";
    static String result = null;
    Integer responseCode = null;
    String responseMessage = "";

    public GooglePlacesQuery() {
    }

    // Google Search Async Task
    public class GooglePlacesQueryAsyncTask extends AsyncTask<URL, Integer, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String json = null;
            try {
//                String countryID = "Q1218";
//                String url1 = "https://query.wikidata.org/sparql?format=json&query=%0ASELECT+DISTINCT+%3Fitem+%3Fname+%3Fcoord+%3Flat+%3Flon%0AWHERE+%7B%0A+++hint%3AQuery+hint%3Aoptimizer+%22None%22+.%0A+++%3Fitem+wdt%3AP131%2A+wd%3AQ1218+.%0A+++%3Fitem+wdt%3AP31%2Fwdt%3AP279%2A+wd%3AQ839954+.%0A+++%3Fitem+wdt%3AP625+%3Fcoord+.%0A+++%3Fitem+p%3AP625+%3Fcoordinate+.%0A+++%3Fcoordinate+psv%3AP625+%3Fcoordinate_node+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLatitude+%3Flat+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLongitude+%3Flon+.%0A+++SERVICE+wikibase%3Alabel+%7B%0A++++bd%3AserviceParam+wikibase%3Alanguage+%22%5BAUTO_LANGUAGE%5D%2Cen%22+.%0A++++%3Fitem+rdfs%3Alabel+%3Fname%0A+++%7D%0A%7D%0AORDER+BY+ASC+%28%3Fname%29%0A";
//
//                Document doc = Jsoup.connect(url1).get();
////                String title = doc.getAllElements().text();
//                Log.d("FINALLITO", title);
//                URL url = new URL("https://query.wikidata.org/sparql?format=json&query=%0ASELECT+DISTINCT+%3Fitem+%3Fname+%3Fcoord+%3Flat+%3Flon%0AWHERE+%7B%0A+++hint%3AQuery+hint%3Aoptimizer+%22None%22+.%0A+++%3Fitem+wdt%3AP131%2A+wd%3AQ1218+.%0A+++%3Fitem+wdt%3AP31%2Fwdt%3AP279%2A+wd%3AQ839954+.%0A+++%3Fitem+wdt%3AP625+%3Fcoord+.%0A+++%3Fitem+p%3AP625+%3Fcoordinate+.%0A+++%3Fcoordinate+psv%3AP625+%3Fcoordinate_node+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLatitude+%3Flat+.%0A+++%3Fcoordinate_node+wikibase%3AgeoLongitude+%3Flon+.%0A+++SERVICE+wikibase%3Alabel+%7B%0A++++bd%3AserviceParam+wikibase%3Alanguage+%22%5BAUTO_LANGUAGE%5D%2Cen%22+.%0A++++%3Fitem+rdfs%3Alabel+%3Fname%0A+++%7D%0A%7D%0AORDER+BY+ASC+%28%3Fname%29%0A");
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setDoOutput(true);
//                connection.setInstanceFollowRedirects(false);
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestProperty("charset", "utf-8");
//                connection.connect();
//
//                InputStream inStream = connection.getInputStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
//                while(reader.ready()) {
//                    String line = reader.readLine();
//                    Log.d("ONGOD7", line);
//                }


//                String text = new Scanner(inStream, "UTF-8").useDelimiter("\\Z").next();
//                Log.d("ONGOD6", string);
//                JSONObject jsonObj = new JSONObject(text);
//                JSONArray contacts = jsonObj.getJSONArray("results");
//                for (int i = 0; i < contacts.length(); i++) {
//                    JSONObject c = contacts.getJSONObject(i);
//                    String id = c.getString("value");
//                    Log.d("ONGOD3", id);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }
    }
}
