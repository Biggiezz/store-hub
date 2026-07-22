package com.example.storehub;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.storehub.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
