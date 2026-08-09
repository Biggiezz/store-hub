package com.nguyenmanhphuc.storehubapp.services;

import android.content.Context;

import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpResquest {

    public static final String BASE_URL = "http://10.0.2.2:3000/"; 
//    public static final String BASE_URL = "http://192.168.1.235:3000/";
//    public static final String BASE_URL = "https://store-hub-server.onrender.com/";
 //    public static final String BASE_URL = "https://storehub-server.vercel.app/";

    public ApiServices apiServices;

    public HttpResquest() {
        apiServices = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiServices.class);
    }

    public ApiServices callAPI() {
        return apiServices;
    }

    public static String authorizationHeader(Context context) {
        String token = new SharedPreferencesManager(context).getToken();
        return token == null || token.trim().isEmpty() ? null : "Bearer " + token;
    }
}
