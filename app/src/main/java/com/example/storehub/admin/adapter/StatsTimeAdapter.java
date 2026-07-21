package com.example.storehub.admin.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class StatsTimeAdapter extends ArrayAdapter<String> {

    public StatsTimeAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, android.R.layout.simple_spinner_item, items);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}
