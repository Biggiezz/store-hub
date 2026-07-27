package com.example.storehub;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.storehub.utils.LocaleHelper;
import com.example.storehub.utils.SharedPreferencesManager;

public class BaseActivity extends AppCompatActivity {
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentLanguage = SharedPreferencesManager.getInstance(this).getLanguage();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String lang = SharedPreferencesManager.getInstance(this).getLanguage();
        if (!lang.equals(currentLanguage)) {
            recreate();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}

