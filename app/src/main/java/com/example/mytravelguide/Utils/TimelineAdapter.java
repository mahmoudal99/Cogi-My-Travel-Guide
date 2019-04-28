package com.example.mytravelguide.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.R;
import com.example.mytravelguide.TravelGuideActivity;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder>  {

    private ArrayList<VisitedPlaceObject> places;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName, dateVisited;
        public ImageView placeImage;

        public MyViewHolder(View view) {
            super(view);
            placeName = (TextView) view.findViewById(R.id.placeName);
            dateVisited = (TextView) view.findViewById(R.id.date);
            placeImage = (ImageView) view.findViewById(R.id.placeImage);
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

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        VisitedPlaceObject placeModel = places.get(position);
        holder.placeName.setText(placeModel.placeName);
        holder.dateVisited.setText(placeModel.dateVisited);
        if(!placeModel.URL.isEmpty()){
            Glide.with(context).load(placeModel.URL).into(holder.placeImage);
        }
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
