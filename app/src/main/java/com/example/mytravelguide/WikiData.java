package com.example.mytravelguide;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;

public class WikiData {

    private static final String PALACE = "Q16560";
    private static final String TOWER = "Q12518";
    private static final String CASTLE = "Q23413";
    private static final String TOURISTATTRACTION = "Q570116";
    private static final String ARCHAELOGICALSITE = "Q570116";
    private static final String MOSQUE = "Q32815";
    private static final String TEMPLE = "Q44539";
    private static final String CHURCH = "Q16970";
    private static final String SYNAGOGUE = "Q34627";

    private static final String WIKIDATAREQUEST = "WIKIDATAREQUEST";
    private static final String LANDMARKREQUEST = "LANDMARKREQUEST";
    private static final String CITYLATLNGREQUEST = "CITYLATLNGREQUEST";


    public WikiData() { }

    public Request getCityDataId(String cityName) {
        String url = "https://wft-geo-db.p.mashape.com/v1/geo/cities?namePrefix=" + cityName + "&minPopulation=500000";
        Request cityDataIDRequest = new Request.Builder()
                .url(url)
                .header("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
                .header("X-RapidAPI-Key", "de22d3cbadmshf632b8fa723db10p12a5e2jsnecd78f4ef9d6")
                .build();
        return cityDataIDRequest;
    }


    public Request getCityLatLng(String cityJsonUrl) {
        Request cityLatLngRequest = new Request.Builder().url(cityJsonUrl).header("content-type", "application/html").build();
        return cityLatLngRequest;
    }

    public Request createCityPlaceIdRequest(String cityURL) {
        Request cityLatLngRequest = new Request.Builder().url(cityURL).header("content-type", "application/html").build();
        return cityLatLngRequest;
    }

    public Request createLandmarkPlaceIdRequest(String landmarkURL) {
        Request request = new Request.Builder().url(landmarkURL).header("content-type", "application/html").build();
        return request;
    }

    public String getCityWikiDataID(String respnse) {
        try {
            JSONObject jsonObject = new JSONObject(respnse);
            JSONArray data = jsonObject.getJSONArray("data");
            return data.getJSONObject(0).get("wikiDataId").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "null";
    }
}

































