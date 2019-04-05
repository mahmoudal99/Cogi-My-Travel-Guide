package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView companyLogo;
    Animation fromTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        companyLogo = findViewById(R.id.company_logo);

        fromTop = AnimationUtils.loadAnimation(this, R.anim.fromtop);

        companyLogo.setAnimation(fromTop);
    }
}
