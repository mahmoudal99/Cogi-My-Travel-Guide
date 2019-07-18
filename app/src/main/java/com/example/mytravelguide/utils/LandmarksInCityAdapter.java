package com.example.mytravelguide.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelguide.R;

import java.util.ArrayList;

public class LandmarksInCityAdapter extends RecyclerView.Adapter<LandmarksInCityAdapter.MyViewHolder> {

    ArrayList<String> landmarks;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;

        MyViewHolder(View view) {
            super(view);
            placeName = view.findViewById(R.id.place_name);
        }
    }

    public LandmarksInCityAdapter(ArrayList<String> landmarks, Context context){
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
        String placeName = landmarks.get(position);
        holder.placeName.setText(placeName);
    }

    @Override
    public int getItemCount() {
        return landmarks.size();
    }
}


































