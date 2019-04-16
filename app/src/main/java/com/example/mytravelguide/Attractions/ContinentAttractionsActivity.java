package com.example.mytravelguide.Attractions;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mytravelguide.Models.AttractionObject;
import com.example.mytravelguide.R;
import com.example.mytravelguide.Utils.AttractionsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContinentAttractionsActivity extends AppCompatActivity {

    RecyclerView listView;
    AttractionObject attractionObject;
    private RecyclerView.Adapter mAdapter;

    // Firebase
    private FirebaseAuth authentication;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Widgets
    ImageView backArrow, attraction_img;
    RelativeLayout relativeLayout;
    TextView continentTextView;

    // Variables
    String continent;
    String databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continent_attractions);

        continent = getIntent().getStringExtra("Continent");

        init();
        setUpWidgets();
        setUpFirebaseAuthentication();
        loadAttractions();
    }

    private void init() {
        listView = findViewById(R.id.list);
        backArrow = findViewById(R.id.backArrow);
        continentTextView = findViewById(R.id.continentTextView);
        attraction_img = findViewById(R.id.attraction_img);
    }


    private void setUpWidgets() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(ContinentAttractionsActivity.this, AttractionsActivity.class);
                startActivity(backIntent);
            }
        });


        if (continent.equals("Africa")) {
            Drawable drawable1 = ContextCompat.getDrawable(ContinentAttractionsActivity.this, R.drawable.africa_bg_img);
            databaseRef = "TouristAttractionsAfrica";
            continentTextView.setText(getString(R.string.Africa));
            attraction_img.setImageDrawable(drawable1);

        } else if (continent.equals("America")) {
            Drawable drawable1 = ContextCompat.getDrawable(ContinentAttractionsActivity.this, R.drawable.america_bg_img);
            databaseRef = "TouristAttractionsAmerica";
            continentTextView.setText(getString(R.string.America));
            attraction_img.setImageDrawable(drawable1);

        } else if (continent.equals("Asia")) {
            Drawable drawable1 = ContextCompat.getDrawable(ContinentAttractionsActivity.this, R.drawable.asia_bg_img);
            databaseRef = "TouristAttractionsAsia";
            continentTextView.setText(getString(R.string.Asia));
            attraction_img.setImageDrawable(drawable1);

        }else if (continent.equals("Europe")) {
            Drawable drawable1 = ContextCompat.getDrawable(ContinentAttractionsActivity.this, R.drawable.europe_bg_img);
            databaseRef = "TouristAttractionsEurope";
            continentTextView.setText(getString(R.string.Europe));
            attraction_img.setImageDrawable(drawable1);

        }
    }

    public void loadAttractions() {

        final ArrayList<AttractionObject> attractionObjectArrayList = new ArrayList<>();

        listView = (RecyclerView) findViewById(R.id.list);

        mAdapter = new AttractionsAdapter(attractionObjectArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(mAdapter);


        authentication = FirebaseAuth.getInstance();
        FirebaseUser user = authentication.getCurrentUser();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = rootRef.child(databaseRef);

        uidRef.orderByChild("placeName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    attractionObject = new AttractionObject();
                    attractionObject.placeName = ds.child("placeName").getValue(String.class);
                    attractionObjectArrayList.add(attractionObject);
                    mAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //---------- Firebase ----------//
    private void setUpFirebaseAuthentication() {
        authentication = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    Log.d("Continent", "Success");
                } else {
                    Log.d("Continent", "signed out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        authentication.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            authentication.removeAuthStateListener(authStateListener);
        }
    }
}

