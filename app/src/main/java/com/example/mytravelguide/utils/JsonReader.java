package com.example.mytravelguide.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
}
