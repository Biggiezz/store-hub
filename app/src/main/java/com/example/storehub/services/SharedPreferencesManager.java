package com.example.storehub.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.storehub.model.User;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "StoreHubPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ID = "user_id";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_IMAGE = "user_image";
    private static final String KEY_ADDRESS = "user_address";
    private static final String KEY_PASS_DATE = "user_pass_date";

    private static SharedPreferencesManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserSession(String token, User user) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ID, user.getId());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_IMAGE, user.getImage());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putString(KEY_PASS_DATE, user.getChangePasswordDate());
        editor.apply();
    }

    public void updateUser(User user) {
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putString(KEY_IMAGE, user.getImage());
        editor.putString(KEY_PASS_DATE, user.getChangePasswordDate());
        editor.apply();
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public User getUser() {
        String token = getToken();
        if (token == null) return null;

        return new User(
                pref.getString(KEY_ID, ""),
                pref.getString(KEY_NAME, ""),
                pref.getString(KEY_EMAIL, ""),
                pref.getString(KEY_PHONE, ""),
                pref.getString(KEY_ROLE, ""),
                pref.getString(KEY_IMAGE, ""),
                pref.getString(KEY_ADDRESS, ""),
                pref.getString(KEY_PASS_DATE, "")
        );
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
