package com.example.mytravelguide.Utils;

import android.util.Log;

import com.example.mytravelguide.TravelGuideActivity;

import java.net.MalformedURLException;
import java.net.URL;

public class GoogleSearch {

    public GoogleSearch() {
    }

    public URL search(String searchString){
        // looking for
        String searchStringNoSpaces = searchString.replace(" ", "+");

        // Your API key
        // TODO replace with your value
        String key = "AIzaSyCyeExKDw-uElEQ8PlvE3G97_9o9djUrDM";

        // Your Search Engine ID
        // TODO replace with your value
        String cx = "000804371853375564055:-lvbacgxzgs";

        String urlString = "https://www.googleapis.com/customsearch/v1?q=" + searchStringNoSpaces + "&key=" + key + "&cx=" + cx + "&searchType=image" + "&alt=json";
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("GOOGLESEARCH", "ERROR converting String to URL " + e.toString());
        }
        Log.d("GOOGLESEARCH", "Url = " + urlString);

        return url;

    }
}
