package com.example.mytravelguide.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelguide.BuildConfig;
import com.example.mytravelguide.models.AttractionObject;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.TimeOfWeek;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.errors.ApiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class GooglePlacesApi {

    private static final String API_KEY = BuildConfig.APIKEY;
    public static final String API_URL = "https://maps.googleapis.com/maps/api/place/";
    public static final String METHOD_TEXT_SEARCH = "textsearch";
    public static final String METHOD_NEARBY_SEARCH = "nearbysearch";


    private PlacesClient placesClient;
    private Context context;
    private Bitmap bitmap;
    private String apiKey;

    public GooglePlacesApi(Context context) {
        this.context = context;
        Places.initialize(context, API_KEY);
        placesClient = Places.createClient(context);
    }

    public GooglePlacesApi(String apiKey) {
        this.apiKey = apiKey;
    }

    public ArrayList<AttractionObject> getNearByLocations(ArrayList<AttractionObject> attractionObjects, RecyclerView.Adapter mAdapter) {

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.PHOTO_METADATAS);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            placesClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {

                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    AttractionObject attractionObject = new AttractionObject();
                    attractionObject.placeName = placeLikelihood.getPlace().getName();
                    attractionObjects.add(attractionObject);
                    mAdapter.notifyDataSetChanged();
                }

            })).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.d("Gooogle Place Api", apiException.getMessage());
                }
            });
        } else {
            Toast.makeText(context, "Permission Not Granted", Toast.LENGTH_SHORT).show();
        }
        return attractionObjects;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setLandmarkPhoto(PhotoMetadata photo, RelativeLayout relativeLayout) {

        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo).build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            bitmap = fetchPhotoResponse.getBitmap();

            ImageProcessing imageProcessing = new ImageProcessing(context);
            imageProcessing.saveImageBitmap(bitmap);

            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            relativeLayout.setBackground(drawable);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Error", "Place not found: " + apiException.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setLandmarkPhoto(PhotoMetadata photo, ImageView imageView) {

        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo).build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            bitmap = fetchPhotoResponse.getBitmap();

            ImageProcessing imageProcessing = new ImageProcessing(context);
            imageProcessing.saveImageBitmap(bitmap);

            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            imageView.setBackground(drawable);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Error", "Place not found: " + apiException.getMessage());
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setLandmarkImageWithBitmap(PhotoMetadata photo, ImageView imageView) {

        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo).build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            bitmap = fetchPhotoResponse.getBitmap();
            imageView.setImageBitmap(bitmap);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Error", "Place not found: " + apiException.getMessage());
            }
        });
    }

    public String placeOpeningHours(Place place) {
        if (place.getOpeningHours() != null) {
            OpeningHours openingHours;
            openingHours = place.getOpeningHours();
            List<Period> periods = Objects.requireNonNull(openingHours).getPeriods();
            Period period = periods.get(0);
            TimeOfWeek timeOfWeekOpen = period.getOpen();
            TimeOfWeek timeOfWeekClose = period.getClose();
            LocalTime localTimeOpen = Objects.requireNonNull(timeOfWeekOpen).getTime();
            if (timeOfWeekClose != null) {
                LocalTime localTimeClose = timeOfWeekClose.getTime();
                return localTimeOpen.getHours() + ":00" + " - " + localTimeClose.getHours() + ":00";
            }
            return localTimeOpen.getHours() + ":00";
        }
        return "No Information Available";
    }

    public Place getPlaceById(String id) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        FetchPlaceRequest request = FetchPlaceRequest.builder(id, placeFields).build();
        FetchPlaceResponse fetchPlaceResponse = placesClient.fetchPlace(request).getResult();
        if (fetchPlaceResponse != null) {
            Place place = fetchPlaceResponse.getPlace();
            return place;
        }

        return null;
    }

    public String getPlacesByQuery(String query, Param... extraParams) {
        try {
            String uri = buildUrl(METHOD_TEXT_SEARCH, String.format("query=%s&key=%s", query, apiKey), extraParams);
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNearbyPlaces(double lat, double lng, double radius) {
        try {
            String uri = buildUrl(METHOD_NEARBY_SEARCH, String.format("key=%s&location=%f,%f&radius=%f", apiKey, lat, lng, radius));
            Log.d("LONELY1", uri);
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String buildUrl(String method, String params, Param... extraParams) {
        String url = String.format("%s%s/json?%s", API_URL, method, params);
        url = addExtraParams(url, extraParams);
        url = url.replace(' ', '+');
        return url;
    }

    private static String addExtraParams(String base, Param... extraParams) {
        for (Param param : extraParams) {
            base += "&" + param.name + (param.value != null ? "=" + param.value : "");
        }
        return base;
    }

    /**
     * Represents an extra, optional parameter that can be specified.
     */
    public static class Param {
        private final String name;
        protected String value;

        private Param(String name) {
            this.name = name;
        }

        /**
         * Returns a new param with the specified name.
         *
         * @param name to create Param from
         * @return new param
         */
        public static Param name(String name) {
            return new Param(name);
        }

        /**
         * Sets the value of the Param.
         *
         * @param value of param
         * @return this param
         */
        public Param value(Object value) {
            this.value = value.toString();
            return this;
        }
    }

    /**
     * Represents an extra, optional type parameter that restricts the results to places matching at least one of the specified types.
     */
    public static class TypeParam extends Param {

        private TypeParam(String name) {
            super(name);
        }

        /**
         * Returns a new type param with the specified name.
         *
         * @param name to create TypeParam from
         * @return new param
         */
        public static TypeParam name(String name) {
            return new TypeParam(name);
        }

        /**
         * Sets the values of the Param.
         *
         * @param values of params
         * @return this params
         */
        public Param value(List<String> values) {
            StringBuilder valuesSb = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                valuesSb.append(values.get(i));
                if (i != (values.size() - 1)) {
                    valuesSb.append("%7C"); // it represents a pipeline character |
                }
            }
            this.value = valuesSb.toString();
            return this;
        }


    }
}









































