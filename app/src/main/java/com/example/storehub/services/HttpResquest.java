package com.example.storehub.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpResquest {
    /// thêm đường dẫn chuẩn trên máy win

    public static final String BASE_URL = "http://10.0.2.2:3000/"; // không xóa -> comment lại

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
}
