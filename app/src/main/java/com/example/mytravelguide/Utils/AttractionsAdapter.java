package com.example.mytravelguide.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mytravelguide.Models.AttractionObject;
import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.R;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class AttractionsAdapter extends RecyclerView.Adapter<AttractionsAdapter.MyViewHolder>  {

    private ArrayList<AttractionObject> attractionObjects;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName, dateVisited;

        public MyViewHolder(View view) {
            super(view);
            placeName = (TextView) view.findViewById(R.id.place_name);
        }
    }


    public AttractionsAdapter(ArrayList<AttractionObject> places) {
        this.attractionObjects = places;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attraction_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AttractionObject placeModel = attractionObjects.get(position);
        holder.placeName.setText(placeModel.placeName);
    }

    @Override
    public int getItemCount() {
        return attractionObjects.size();
    }
}
