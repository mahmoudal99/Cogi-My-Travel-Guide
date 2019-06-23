package com.example.mytravelguide.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelguide.models.AttractionObject;
import com.example.mytravelguide.R;
import com.example.mytravelguide.TravelGuideActivity;

import java.util.ArrayList;

public class NearByLocationsAdapter extends RecyclerView.Adapter<NearByLocationsAdapter.MyViewHolder> {

    private ArrayList<AttractionObject> attractionObjects;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;

        MyViewHolder(View view) {
            super(view);
            placeName = view.findViewById(R.id.place_name);
        }
    }

    public NearByLocationsAdapter(ArrayList<AttractionObject> places, Context context) {
        this.attractionObjects = places;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.near_by_location_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        AttractionObject placeModel = attractionObjects.get(position);
        holder.placeName.setText(placeModel.placeName);

        holder.placeName.setOnClickListener(v -> {
            Intent intent = new Intent(context, TravelGuideActivity.class);
            intent.putExtra("AttractionName", holder.placeName.getText());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return attractionObjects.size();
    }
}
