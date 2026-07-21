package com.example.storehub.services;

import com.example.storehub.model.LoginResponse;
import com.example.storehub.model.Post;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiServices {
    @GET("api/productsRouter/get-all-product")
    Call<Response<ArrayList<Product>>> getListProduct();

    @POST("api/productsRouter/add-product")
    Call<Response<Product>> addProduct(@Body Product product);

    @GET("api/posts/get-all")
    Call<Response<ArrayList<Post>>> getListPost();

    @POST("api/posts/add")
    Call<Response<Post>> addPost(@Body Post post);

    @PUT("api/posts/update/{id}")
    Call<Response<Post>> updatePost(@Path("id") String id, @Body Post post);

    @DELETE("api/posts/delete/{id}")
    Call<Response<Void>> deletePost(@Path("id") String id);

    @POST("users/register")
    Call<Response<User>> register(@Body Map<String, String> body);

    @POST("users/login")
    Call<LoginResponse> login(@Body Map<String, String> body);

    @PUT("users/update-profile")
    Call<Response<User>> updateProfile(@Header("Authorization") String authHeader, @Body Map<String, String> body);

    @PUT("users/change-password")
    Call<Response<Void>> changePassword(@Header("Authorization") String authHeader, @Body Map<String, String> body);

    @POST("users/logout")
    Call<Response<Void>> logout(@Header("Authorization") String authHeader);
}
