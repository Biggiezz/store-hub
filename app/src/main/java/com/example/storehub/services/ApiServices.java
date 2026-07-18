package com.example.storehub.services;

import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.model.LoginRequest;
import com.example.storehub.model.LoginResponse;
import com.example.storehub.model.RegisterRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiServices {
    @GET("api/productsRouter/get-all-product")
    Call<Response<ArrayList<Product>>> getListProduct();

    @POST("api/productsRouter/add-product")
    Call<Response<Product>> addProduct(@Body Product product);

    @POST("users/register")
    Call<Response<User>> register(@Body RegisterRequest request);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
