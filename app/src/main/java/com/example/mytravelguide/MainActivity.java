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
    Animation fromTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        setUpAnimation();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            // <---- run your one time code here
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                    Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(signInIntent);
                }
            }, 10000);

            // mark first time has ran.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }else {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }

    }

    private void setUpAnimation() {
        companyLogo = findViewById(R.id.company_logo);
        fromTop = AnimationUtils.loadAnimation(this, R.anim.fromtop);
        companyLogo.setAnimation(fromTop);
    }
}
