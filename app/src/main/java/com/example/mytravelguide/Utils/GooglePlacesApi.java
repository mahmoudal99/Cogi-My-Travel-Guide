package com.example.mytravelguide.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelguide.BuildConfig;
import com.example.mytravelguide.Models.AttractionObject;
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

    private PlacesClient placesClient;
    private Context context;
    private Bitmap bitmap;

    public GooglePlacesApi(Context context) {
        this.context = context;
        Places.initialize(context, API_KEY);
        placesClient = Places.createClient(context);
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
                    Log.d("Gooogle Places Api", apiException.getMessage());
                }
            });
        } else {
            Toast.makeText(context, "Permission Not Granted", Toast.LENGTH_SHORT).show();
        }
        return attractionObjects;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setPhoto(PhotoMetadata photo, ImageView imageView) {

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
        // Opening Hours
        OpeningHours openingHours;
        openingHours = place.getOpeningHours();
        List<Period> periods = Objects.requireNonNull(openingHours).getPeriods();
        Period period = periods.get(0);
        TimeOfWeek timeOfWeekOpen = period.getOpen();
        TimeOfWeek timeOfWeekClose = period.getClose();
        LocalTime localTimeOpen = Objects.requireNonNull(timeOfWeekOpen).getTime();
        LocalTime localTimeClose = Objects.requireNonNull(timeOfWeekClose).getTime();

        return localTimeOpen.getHours() + ":00" + " - " + localTimeClose.getHours() + ":00";
    }

}









































