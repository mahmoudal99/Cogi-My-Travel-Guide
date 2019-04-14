package com.example.mytravelguide.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.R;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder>  {

    private ArrayList<VisitedPlaceObject> places;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName, dateVisited;

        public MyViewHolder(View view) {
            super(view);
            placeName = (TextView) view.findViewById(R.id.placeName);
            dateVisited = (TextView) view.findViewById(R.id.date);
        }
    }


    public TimelineAdapter(ArrayList<VisitedPlaceObject> places) {
        this.places = places;
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
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
