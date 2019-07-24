package com.example.mytravelguide.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mytravelguide.models.VisitedPlaceObject;
import com.example.mytravelguide.R;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder>  {

    private ArrayList<VisitedPlaceObject> places;
    private Context context;
    private View divider;
    private String[] colours = {"#D50000", "#00C853", "#2979FF", "#FFFF00", "#FF3D00", "#6200EA"};


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, dateVisited;
        ImageView placeImage;
        MyViewHolder(View view) {
            super(view);
            placeName = view.findViewById(R.id.placeName);
            placeImage = view.findViewById(R.id.placeImage);
            divider = view.findViewById(R.id.divider);
        }
    }


    public TimelineAdapter(ArrayList<VisitedPlaceObject> places, Context context) {
        this.places = places;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        VisitedPlaceObject placeModel = places.get(position);
        holder.placeName.setText(placeModel.placeName);
        Random random = new Random();
        int index = random.nextInt(6);
        divider.setBackgroundColor(Color.parseColor(colours[index]));
        GooglePlacesApi googlePlacesApi = new GooglePlacesApi(context);
        googlePlacesApi.setLandmarkImageWithBitmap(placeModel.photoMetadata, holder.placeImage);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
