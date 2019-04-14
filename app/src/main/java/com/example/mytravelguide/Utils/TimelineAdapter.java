package com.example.mytravelguide.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mytravelguide.Models.VisitedPlaceObject;
import com.example.mytravelguide.R;

import java.util.ArrayList;

public class TimelineAdapter extends ArrayAdapter<VisitedPlaceObject> {

    String name, dateVisited;

    public TimelineAdapter(Context context, ArrayList<VisitedPlaceObject> arrayList, String name, String date) {
        super(context, 0, arrayList);
        this.name = name;
        this.dateVisited = date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final VisitedPlaceObject placeModel = getItem(position);

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);

        }

        TextView placeName = convertView.findViewById(R.id.placeName);
        TextView date = convertView.findViewById(R.id.date);
        ImageView placeImage = convertView.findViewById(R.id.placeImage);

        placeName.setText(placeModel.placeName);
        date.setText(placeModel.dateVisited);

        return convertView;

    }
}










































