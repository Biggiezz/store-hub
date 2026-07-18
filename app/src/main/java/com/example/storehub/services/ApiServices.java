package com.example.storehub.services;

import com.example.storehub.model.News;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServices {
    @GET("api/productsRouter/get-all-product")
    Call<Response<ArrayList<Product>>> getListProduct();

    @POST("api/productsRouter/add-product")
    Call<Response<Product>> addProduct(@Body Product product);

    // Lấy danh sách toàn bộ tin tức đã xuất bản
    @GET("api/newsRouter/get-all-news")
    Call<Response<ArrayList<News>>> getListNews();

    // Lấy chi tiết một bài viết tin tức dựa vào ID
    @GET("api/newsRouter/get-news-by-id/{id}")
    Call<Response<News>> getNewsById(@Path("id") String id);
}
