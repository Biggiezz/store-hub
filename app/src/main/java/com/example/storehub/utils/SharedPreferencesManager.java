package com.example.storehub.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.storehub.model.User;
import com.google.gson.Gson;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "store_hub_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER = "auth_user";
    private static final String KEY_LANGUAGE = "app_language";

    private static SharedPreferencesManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public SharedPreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserSession(String token, User user) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    // Lấy Token
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Lưu User
    public void updateUser(User user) {
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_USER, userJson).apply();
    }

    // Lấy User
    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson == null) {
            return null;
        }
        return gson.fromJson(userJson, User.class);
    }

    // Kiểm tra đăng nhập
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Quản lý ngôn ngữ
    public void setLanguage(String languageCode) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "vi"); // Mặc định là tiếng Việt
    }

    // Đăng xuất (Xóa hết dữ liệu)
    public void logout() {
        sharedPreferences.edit().remove(KEY_TOKEN).remove(KEY_USER).apply();
        // Không xóa ngôn ngữ khi logout để giữ nguyên lựa chọn của user
    }
}
