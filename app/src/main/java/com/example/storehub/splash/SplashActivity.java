package com.example.storehub.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.storehub.MainActivity;
import com.example.storehub.R;
import com.example.storehub.auth.LoginActivity;
import com.example.storehub.utils.SharedPreferencesManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        
        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);

        new Handler().postDelayed(() -> {
            Intent intent;
            if (prefManager.isLoggedIn()) {
                com.example.storehub.model.User user = prefManager.getUser();
                String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";

                if (role.equals("admin") || role.equals("super admin") || role.equals("superadmin")) {
                    // intent = new Intent(this, com.example.storehub.admin.HomePageManagement.class);
                    intent = new Intent(this, MainActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}