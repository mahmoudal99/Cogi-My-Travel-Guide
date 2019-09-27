package com.cogi.mytravelguide.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cogi.mytravelguide.BuildConfig;
import com.cogi.mytravelguide.models.LandmarkModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.TimeOfWeek;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.errors.ApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class GooglePlacesApi {

    private static final String API_KEY = BuildConfig.APIKEY;
    private static final String API_URL = "https://maps.googleapis.com/maps/api/place/";
    private static final String METHOD_TEXT_SEARCH = "textsearch";
    private static final String METHOD_NEARBY_SEARCH = "nearbysearch";

    private PlacesClient placesClient;
    private Context context;
    private Bitmap bitmap;
    private String apiKey;
    private List<Place.Field> placeFields;
    private FindCurrentPlaceRequest findCurrentPlaceRequest;
    private FetchPhotoRequest fetchPhotoRequest;

    public GooglePlacesApi(Context context) {
        this.context = context;
        Places.initialize(context, API_KEY);
        placesClient = Places.createClient(context);
    }

    public GooglePlacesApi(String apiKey, Context context) {
        this.apiKey = apiKey;
        this.context = context;
        Places.initialize(context, API_KEY);
        placesClient = Places.createClient(context);
    }

    private void initializePlaceFields() {
        placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
    }

    private void findCurrentPlaceRequest() {
        findCurrentPlaceRequest = FindCurrentPlaceRequest.builder(placeFields).build();
    }

    public ArrayList<LandmarkModel> getNearByLocations(ArrayList<LandmarkModel> attractionObjects, RecyclerView.Adapter mAdapter) {
        initializePlaceFields();
        findCurrentPlaceRequest();

        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.findCurrentPlace(findCurrentPlaceRequest).addOnSuccessListener(((response) -> {
                loadPlaceLikelihoods(response.getPlaceLikelihoods(), attractionObjects, mAdapter);
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

    private void loadPlaceLikelihoods(List<PlaceLikelihood> placeLikelihoods, ArrayList<LandmarkModel> attractionObjects, RecyclerView.Adapter adapter) {
        for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
            LandmarkModel landmarkModel = new LandmarkModel();
            landmarkModel.placeName = placeLikelihood.getPlace().getName();
            attractionObjects.add(landmarkModel);
            adapter.notifyDataSetChanged();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setLandmarkPhoto(PhotoMetadata photo, RelativeLayout relativeLayout) {
        placesClient.fetchPhoto(fetchPhotoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
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
        placesClient.fetchPhoto(fetchPhotoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
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

    public void setLandmarkImageWithBitmap(PhotoMetadata photo, ImageView imageView) {
        fetchPhotoRequest = FetchPhotoRequest.builder(photo).build();
        placesClient.fetchPhoto(fetchPhotoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            bitmap = fetchPhotoResponse.getBitmap();
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Error", "Place not found: " + apiException.getMessage());
            }
        });
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

    public static class Param {
        private final String name;
        protected String value;

        private Param(String name) {
            this.name = name;
        }

        public static Param name(String name) {
            return new Param(name);
        }

    }
}









































