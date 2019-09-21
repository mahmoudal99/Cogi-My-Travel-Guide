package com.example.mytravelguide.utils;
import android.util.Log;

import org.apache.jena.atlas.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JsonReader {

    public JsonReader() {
    }

    public double[] getMapsLatLngFromJson(String requestResponse) {
        JSONObject requestJsonObject = null;
        try {
            requestJsonObject = new JSONObject(requestResponse);
            JSONArray resultsJsonArrau = requestJsonObject.getJSONArray("results");
            requestJsonObject = new JSONObject(resultsJsonArrau.get(0).toString());
            requestJsonObject = requestJsonObject.getJSONObject("geometry");
            requestJsonObject = requestJsonObject.getJSONObject("location");
            return new double[]{(double) requestJsonObject.get("lat"), (double) requestJsonObject.get("lng")};
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new double[]{0, 0};
    }

    public List<String> getLandmarksInCityFromJson(String response) {
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

    public JSONObject getLandmarkPlaceIDFromJson(String response) {

        JSONObject placeIDObject = null;
        try {
            placeIDObject = new JSONObject(response);
            JSONArray jsonArray = placeIDObject.getJSONArray("results");
            placeIDObject = new JSONObject(jsonArray.get(0).toString());
            return placeIDObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getDirectionsInformation(String jsonData){
        JSONObject directionJsonData = null;

        try {
            directionJsonData = new JSONObject(jsonData);
            JSONArray jsonArray = directionJsonData.getJSONArray("routes");
            directionJsonData = new JSONObject(jsonArray.get(0).toString());
            jsonArray = directionJsonData.getJSONArray("legs");
            directionJsonData = new JSONObject(jsonArray.get(0).toString());
            String distance = directionJsonData.get("distance").toString();
            String duration = directionJsonData.get("duration").toString();
            JSONObject jsonDistance = new JSONObject(distance);
            JSONObject jsonDuration = new JSONObject(duration);
            List<String> tripInformation = new ArrayList<>();
            tripInformation.add(jsonDistance.get("text").toString());
            tripInformation.add(jsonDuration.get("text").toString());
            return tripInformation;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getCityPopulationFromJson(String response){
        JSONObject populationJsonData = null;
        List<String> cityInformation = new ArrayList<>();
        try {
            populationJsonData = new JSONObject(response);
            if(populationJsonData.toString().contains("landmarks")){
                cityInformation.add("null");
                cityInformation.add(populationJsonData.get("landmarks").toString());
                return cityInformation;
            }else {
                Log.d("MOMOMOM", "NULL");
                cityInformation.add(populationJsonData.get("population").toString());
                cityInformation.add("null");
                return cityInformation;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return null;
    }
}

































