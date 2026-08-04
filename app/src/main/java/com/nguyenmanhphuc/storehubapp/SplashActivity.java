package com.nguyenmanhphuc.storehubapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;


import com.nguyenmanhphuc.storehubapp.admin.HomePageManagementActivity;
import com.nguyenmanhphuc.storehubapp.auth.LoginActivity;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;

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