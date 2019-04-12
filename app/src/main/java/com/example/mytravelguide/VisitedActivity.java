package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.mytravelguide.Models.PlaceModel;

import java.util.ArrayList;

public class VisitedActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity";

    ImageView backArrow;

    // Variables
    ArrayList<PlaceModel> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited);

        init();
        setUpWidgets();
    }

    private void init(){
        backArrow = findViewById(R.id.backArrow);
    }

    private void setUpWidgets(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(VisitedActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });
    }

    private void addPlaceToTimeline(){
        places = new ArrayList<>();


    }
}




























