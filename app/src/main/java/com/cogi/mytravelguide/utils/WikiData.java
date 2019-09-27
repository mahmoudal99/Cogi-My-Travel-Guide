package com.cogi.mytravelguide.utils;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WikiData {

    public WikiData() {
    }

    public Request getCityDataId(String cityName, String population) {
        String url = "https://wft-geo-db.p.mashape.com/v1/geo/cities?namePrefix=" + cityName + "&minPopulation=" + population;
        return new Request.Builder()
                .url(url)
                .header("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .header("X-RapidAPI-Key", "de22d3cbadmshf632b8fa723db10p12a5e2jsnecd78f4ef9d6")
                .build();
    }

    public Request callCityDataApi(String cityName) {
        String getPlacesInCityURL = "https://my-travel-guide-f9642.appspot.com/cities/" + cityName;
        return new Request.Builder().url(getPlacesInCityURL).header("content-type", "application/html").build();
    }

    public Request getCityLatLng(String cityJsonUrl) {
        return new Request.Builder().url(cityJsonUrl).header("content-type", "application/html").build();
    }

    public Request createCityPlaceIdRequest(String cityURL) {
        return new Request.Builder().url(cityURL).header("content-type", "application/html").build();
    }

    public Request createLandmarkPlaceIdRequest(String landmarkURL) {
        return new Request.Builder().url(landmarkURL).header("content-type", "application/html").build();
    }

    public String getCityWikiDataID(String respnse) {
        try {
            JSONObject jsonObject = new JSONObject(respnse);
            JSONArray data = jsonObject.getJSONArray("data");

            try {
                return data.getJSONObject(0).get("wikiDataId").toString();
            } catch (JSONException e) {
                Log.d("JSONException", e.getMessage());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "null";
    }
}

































