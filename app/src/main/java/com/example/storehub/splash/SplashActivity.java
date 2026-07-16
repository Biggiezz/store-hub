package com.example.storehub.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.storehub.R;
import com.example.storehub.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
      new Handler().postDelayed(() -> {
          Intent intent = new Intent(this, LoginActivity.class);
          startActivity(intent);
          finish();
      }, 3000);
    }
}