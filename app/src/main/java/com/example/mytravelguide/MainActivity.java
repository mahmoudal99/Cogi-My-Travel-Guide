package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.mytravelguide.authentication.SignInActivity;

public class MainActivity extends AppCompatActivity {

    ImageView companyLogo;
    Animation fromTopAnimation;
    SharedPreferences sharedPreferences;
    String path = "android.resource://" + "com.example.mytravelguide" + "/" + R.raw.easy_ease;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadActivity();
    }


    private void openSignInPage() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void loadActivity() {

        if (!sharedPreferences.getBoolean("animationLoaded", false)) {

            Handler handler = new Handler();
            handler.postDelayed(this::openSignInPage, 10000);

            VideoView mVideoView = findViewById(R.id.videoViewRelative);
            mVideoView.setVideoPath(path);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.requestFocus();
            mVideoView.start();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("animationLoaded", true);
            editor.apply();

        } else {
            openSignInPage();
        }
    }

}

























