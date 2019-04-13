package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.example.mytravelguide.Models.PlaceModel;
import com.example.mytravelguide.Utils.TimelineAdapter;

import java.util.ArrayList;

public class VisitedActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity";

    ImageView backArrow;
    ListView listView;
    ImageView addPlace;

    // Variables
    ArrayList<PlaceModel> places;
    String placeName, date;
    Context context;

    TimelineAdapter timelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited);

        init();
        setUpWidgets();
    }

    private void init(){
        backArrow = findViewById(R.id.backArrow);
        context = VisitedActivity.this;
        addPlace = findViewById(R.id.addPlace);
        listView = findViewById(R.id.list);
    }

    private void setUpWidgets(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(VisitedActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceToTimeline("Paris", "10/4/2019");
                timelineAdapter.notifyDataSetChanged();
            }
        });
    }

    public void addPlaceToTimeline(String placeName, String date){
        places = new ArrayList<PlaceModel>();
        PlaceModel placeModel = new PlaceModel(placeName, date);
        places.add(placeModel);
        timelineAdapter = new TimelineAdapter(VisitedActivity.this, places, placeName, date);
        listView = findViewById(R.id.list);
        listView.setAdapter(timelineAdapter);
        timelineAdapter.notifyDataSetChanged();

    }
}




























