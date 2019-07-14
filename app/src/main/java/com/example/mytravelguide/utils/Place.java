package com.example.mytravelguide.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.mytravelguide.utils.GooglePlaces.*;

public class Place {

    private final List<String> types = new ArrayList<>();
    private GooglePlaces client;
    private String id;
    private double lat = -1, lng = -1;
    private JSONObject json;
    private String iconUrl;
    private InputStream icon;
    private String name;
    private String addr;
    private String vicinity;
    private double rating = -1;
    private String referenceId;
    private String phone, internationalPhone;
    private String googleUrl, website;
    private int utcOffset;
    private int accuracy;
    private String lang;

    protected Place() {
    }

    /**
     * Creates a JSON object for POSTing new places to Google Places API.
     *
     * @return JSON object to represent place
     */
    public static JSONObject buildInput(double lat, double lng, int accuracy, String name, Collection<String> types,
                                        String lang) throws JSONException {
        return new JSONObject().put(OBJECT_LOCATION, new JSONObject().put("lat", lat).put("lng", lng))
                .put(INTEGER_ACCURACY, accuracy).put(STRING_NAME, name).put(ARRAY_TYPES, new JSONArray(types))
                .put(STRING_LANGUAGE, lang);
    }

    /**
     * Parses a detailed Place object.
     *
     * @param client  api client
     * @param rawJson json to parse
     * @return a detailed place
     */
    public static Place parseDetails(GooglePlaces client, String rawJson) throws JSONException {
        JSONObject json = new JSONObject(rawJson);

        JSONObject result = json.getJSONObject(OBJECT_RESULT);

        // easy stuff
        String id = result.getString(STRING_ID);
        String name = result.getString(STRING_NAME);
        String address = result.optString(STRING_ADDRESS, null);
        String phone = result.optString(STRING_PHONE_NUMBER, null);
        String iconUrl = result.optString(STRING_ICON, null);
        String internationalPhone = result.optString(STRING_INTERNATIONAL_PHONE_NUMBER, null);
        double rating = result.optDouble(DOUBLE_RATING, -1);
        String reference = result.optString(STRING_REFERENCE, null);
        String url = result.optString(STRING_URL, null);
        String vicinity = result.optString(STRING_VICINITY, null);
        String website = result.optString(STRING_WEBSITE, null);
        int utcOffset = result.optInt(INTEGER_UTC_OFFSET, -1);
        return null;
    }

    /**
     * Returns the client associated with this Place object.
     *
     * @return client
     */
    public GooglePlaces getClient() {
        return client;
    }

    /**
     *
     * @param client to set
     * @return this
     */
    protected Place setClient(GooglePlaces client) {
        this.client = client;
        return this;
    }

    /**
     * Returns the id associated with this place.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id associated with this place.
     *
     * @param id id
     * @return this
     */
    protected Place setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the latitude of the place.
     *
     * @return place latitude
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Sets the latitude of the place.
     *
     * @param lat latitude
     * @return this
     */
    protected Place setLatitude(double lat) {
        this.lat = lat;
        return this;
    }

    /**
     * Returns the longitude of this place.
     *
     * @return longitude
     */
    public double getLongitude() {
        return lng;
    }

    /**
     * Sets the longitude of this place.
     *
     * @param lon longitude
     * @return this
     */
    protected Place setLongitude(double lon) {
        this.lng = lon;
        return this;
    }

    /**
     * Returns this Place's phone number.
     *
     * @return number
     */
    public String getPhoneNumber() {
        return phone;
    }

    /**
     * Sets this Place's phone number.
     *
     * @param phone number
     * @return this
     */
    protected Place setPhoneNumber(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Returns the place's phone number with a country code.
     *
     * @return phone number
     */
    public String getInternationalPhoneNumber() {
        return internationalPhone;
    }

    /**
     * Sets the phone number with an international country code.
     *
     * @param internationalPhone phone number
     * @return this
     */
    protected Place setInternationalPhoneNumber(String internationalPhone) {
        this.internationalPhone = internationalPhone;
        return this;
    }

    /**
     * Returns the Google PLus page for this place.
     *
     * @return plus page
     */
    public String getGoogleUrl() {
        return googleUrl;
    }

    /**
     * Sets the Google Plus page for this place.
     *
     * @param googleUrl google plus page
     * @return this
     */
    protected Place setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
        return this;
    }

    /**
     * Returns the name of this place.
     *
     * @return name of place
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this place.
     *
     * @param name of place
     * @return this
     */
    protected Place setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the reference id, used for getting more details about this place.
     *
     * @return reference id
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Returns the reference id to find more details about this place.
     *
     * @param referenceId to get details from
     * @return this
     */
    protected Place setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    /**
     * Returns the JSON representation of this place. This does not build a JSON object, it only returns the JSON
     * that was given in the initial response from the server.
     *
     * @return the json representation
     */
    public JSONObject getJson() {
        return json;
    }

    /**
     * Sets the JSON representation of this Place.
     *
     * @param json representation
     * @return this
     */
    protected Place setJson(JSONObject json) {
        this.json = json;
        return this;
    }

    /**
     * Returns the accuracy of the location, expressed in meters.
     *
     * @return accuracy of location
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the accuracy of the location, expressed in meters.
     *
     * @param accuracy of location
     * @return this
     */
    protected Place setAccuracy(int accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    /**
     * Returns the language of the place.
     *
     * @return language
     */
    public String getLanguage() {
        return lang;
    }

    /**
     * Sets the language of the location.
     *
     * @param lang place language
     * @return this
     */
    protected Place setLanguage(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Returns an updated Place object with more details than the Place object returned in an initial query.
     *
     * @param params extra params to include in the request url
     * @return a new place with more details
     */
    public Place getDetails(Param... params) {
        return client.getPlace(referenceId, params);
    }

    @Override
    public String toString() {
        return String.format("Place{id=%s,loc=%f,%f,name=%s,addr=%s,ref=%s", id, lat, lng, name, addr, referenceId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Place && ((Place) obj).id.equals(id);
    }
}













































