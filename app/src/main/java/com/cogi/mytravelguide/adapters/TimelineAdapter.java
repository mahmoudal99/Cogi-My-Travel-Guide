package com.cogi.mytravelguide.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cogi.mytravelguide.models.VisitedPlaceObject;
import com.cogi.mytravelguide.R;
import com.cogi.mytravelguide.utils.GooglePlacesApi;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder> {

    private ArrayList<VisitedPlaceObject> places;
    private Context context;
    private ImageView tourist;
    int index = 0;
    int[] drawables = {R.drawable.tourist1, R.drawable.tourist2, R.drawable.tourist3, R.drawable.tourist4, R.drawable.tourist6, R.drawable.tourist7, R.drawable.tourist8};

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, dateVisited;
        ImageView placeImage;

        MyViewHolder(View view) {
            super(view);
            placeName = view.findViewById(R.id.placeName);
            placeImage = view.findViewById(R.id.placeImage);
            tourist = view.findViewById(R.id.touristImg);
            dateVisited = view.findViewById(R.id.dateVisited);
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
        holder.dateVisited.setText(placeModel.dateVisited);
        GooglePlacesApi googlePlacesApi = new GooglePlacesApi(context);

        if (index == 7) {
            index = 0;
        } else {
            tourist.setImageDrawable(context.getResources().getDrawable(drawables[index]));
            index += 1;
        }
        googlePlacesApi.setLandmarkImageWithBitmap(placeModel.photoMetadata, holder.placeImage);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
