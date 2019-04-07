package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomePageActivity extends AppCompatActivity {

    CardView attractionsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        init();
        setUpWidgets();
    }

    private void init(){
        attractionsCard = findViewById(R.id.attractionsCard);
    }

    private void setUpWidgets(){
        attractionsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent attractionsIntent = new Intent(HomePageActivity.this, AttractionsActivity.class);
                startActivity(attractionsIntent);
            }
        });
    }
}
