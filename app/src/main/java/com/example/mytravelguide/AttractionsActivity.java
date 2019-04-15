package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AttractionsActivity extends AppCompatActivity {

    ImageView backArrow;
    CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions);

        init();
        setUpWidgets();
    }

    private void init(){
        backArrow = findViewById(R.id.backArrow);
        cardView = findViewById(R.id.attractionsCardEurope);
    }

    private void setUpWidgets(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(AttractionsActivity.this, HomePageActivity.class);
                startActivity(backIntent);
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AttractionsActivity.this, ContinentAttractionsActivity.class));
            }
        });
    }
}
