package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

        setUpAnimation();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(signInIntent);
            }
        }, 10000);   //5 seconds


    }

    private void setUpAnimation() {
        companyLogo = findViewById(R.id.company_logo);
        fromTop = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        companyLogo.setAnimation(fromTop);
    }
}
