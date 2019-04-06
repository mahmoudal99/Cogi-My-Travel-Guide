package com.example.mytravelguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SignInActivity extends AppCompatActivity {

    TextView createAccountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();
        setUpWidgets();

    }

    private void init(){
        createAccountText = findViewById(R.id.CreateAccountText);
    }

    private void setUpWidgets(){
        createAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccountIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(createAccountIntent);
            }
        });
    }
}
