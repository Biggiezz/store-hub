package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.example.storehub.admin.HomePageManagementActivity;
import com.example.storehub.auth.LoginActivity;
import com.example.storehub.model.User;
import com.example.storehub.utils.SharedPreferencesManager;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        
        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);

        new Handler().postDelayed(() -> {
            Intent intent;
            if (prefManager.isLoggedIn()) {
                User user = prefManager.getUser();
                String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";

                if (role.equals("admin") || role.equals("super admin") || role.equals("superadmin")) {
                    intent = new Intent(this, HomePageManagementActivity.class);
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