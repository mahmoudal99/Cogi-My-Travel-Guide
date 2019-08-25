package com.example.mytravelguide.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JsonReader {

    public JsonReader(){}

    public double[] getMapsLatLngFromJson(String requestResponse){
        JSONObject requestJsonObject = null;
        try {
            requestJsonObject = new JSONObject(requestResponse);
            JSONArray resultsJsonArrau = requestJsonObject.getJSONArray("results");
            requestJsonObject = new JSONObject(resultsJsonArrau.get(0).toString());
            requestJsonObject = requestJsonObject.getJSONObject("geometry");
            requestJsonObject = requestJsonObject.getJSONObject("location");
            return new double[] {(double) requestJsonObject.get("lat"), (double) requestJsonObject.get("lng")};
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new double[] {0, 0};
    }

    public List<String> getLandmarksInCityFromJson(String response){
        ArrayList<String> landmarks = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            String value = jsonObject.getString("results");
            jsonObject = new JSONObject(value);
            JSONArray jsonArray = jsonObject.getJSONArray("bindings");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject finalObject = jsonArray.getJSONObject(i);
                jsonObject = new JSONObject(finalObject.getString("name"));
                landmarks.add(jsonObject.get("value").toString());
            }
            Set<String> landmarksSet = new LinkedHashSet<>(landmarks);
            List<String> landmarksList = new ArrayList<>(landmarksSet);
            return landmarksList;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
