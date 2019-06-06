package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.mytravelguide.Authentication.SignInActivity;

public class MainActivity extends AppCompatActivity {

    ImageView companyLogo;
    Animation fromTopAnimation;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        setUpAnimation();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        startAnimation();
    }

    private void setUpAnimation() {
        companyLogo = findViewById(R.id.company_logo);
        fromTopAnimation = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        companyLogo.setAnimation(fromTopAnimation);
    }

    private void openSignInPage() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startAnimation() {

        if (!sharedPreferences.getBoolean("animationLoaded", false)) {

            Handler handler = new Handler();
            handler.postDelayed(this::openSignInPage, 10000);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("animationLoaded", true);
            editor.commit();

        } else {
            openSignInPage();
        }
    }
}

























