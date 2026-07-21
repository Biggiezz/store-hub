package com.example.storehub.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storehub.R;

public class HomePageManagement extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_management);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostManagementFragment())
                    .commit();
        }
    }
}
