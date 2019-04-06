package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SignUpActivity extends AppCompatActivity {

    TextView alreadyHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        setUpWidget();
    }

    private void init() {
        alreadyHaveAccount = findViewById(R.id.AlreadyAccountText);
    }

    private void setUpWidget() {
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(signInIntent);
            }
        });
    }
}
