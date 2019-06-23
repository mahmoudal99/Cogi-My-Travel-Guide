package com.example.mytravelguide.utils;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class GoogleSearch {

    public GoogleSearch() {
    }

    public URL search(String searchString) {

        String searchStringNoSpaces = searchString.replace(" ", "+");

        String apiKey = "AIzaSyCyeExKDw-uElEQ8PlvE3G97_9o9djUrDM";
        String searchEngineID = "000804371853375564055:-lvbacgxzgs";

        String urlString = "https://www.googleapis.com/customsearch/v1?q=" + searchStringNoSpaces + "&key=" + apiKey + "&cx=" + searchEngineID + "&alt=json";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("Google Search", "ERROR converting String to URL " + e.toString());
        }
        Log.d("Google Search", "Url = " + urlString);

        return url;
    }
}
