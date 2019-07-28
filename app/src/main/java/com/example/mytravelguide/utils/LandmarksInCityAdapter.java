package com.example.mytravelguide.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelguide.R;
import com.example.mytravelguide.models.AttractionObject;

import java.util.ArrayList;
import java.util.List;

public class LandmarksInCityAdapter extends RecyclerView.Adapter<LandmarksInCityAdapter.MyViewHolder> {

    ArrayList<AttractionObject> landmarks;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;

        MyViewHolder(View view) {
            super(view);
            placeName = view.findViewById(R.id.place_name);
        }
    }

    public LandmarksInCityAdapter(ArrayList<AttractionObject> landmarks, Context context){
        this.landmarks = landmarks;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.near_by_location_item, parent, false);
        return new LandmarksInCityAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AttractionObject landmark = landmarks.get(position);
        Log.d("FFIIINEINADAP", landmark.placeName);
        holder.placeName.setText(landmark.placeName);
    }

    @Override
    public int getItemCount() {
        return landmarks.size();
    }
}


































