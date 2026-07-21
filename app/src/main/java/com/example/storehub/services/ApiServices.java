package com.example.storehub.services;

import com.example.storehub.model.AddToCartRequest;
import com.example.storehub.model.ApiMessageResponse;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.LoginRequest;
import com.example.storehub.model.LoginResponse;
import com.example.storehub.model.News;
import com.example.storehub.model.Post;
import com.example.storehub.model.Product;
import com.example.storehub.model.ProductDetailResponse;
import com.example.storehub.model.RegisterRequest;
import com.example.storehub.model.Response;
import com.example.storehub.model.UpdateCartQuantityRequest;
import com.example.storehub.model.User;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiServices {
    @GET("api/productsRouter/get-all-product")
    Call<Response<ArrayList<Product>>> getListProduct(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("api/productsRouter/search-product")
    Call<Response<ArrayList<Product>>> searchProduct(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("keyword") String keyword
    );

    @GET("api/productsRouter/get-latest-product")
    Call<Response<ArrayList<Product>>> getLatestProduct();

    @GET("api/productsRouter/get-product-by-id/{id}")
    Call<Response<ProductDetailResponse>> getProductDetail(@Path("id") String id);

    @POST("api/productsRouter/add-product")
    Call<Response<Product>> addProduct(@Body Product product);

    @GET("api/productsRouter/get-cart")
    Call<Response<ArrayList<CartItem>>> getCart();

    @POST("api/productsRouter/add-to-cart")
    Call<ApiMessageResponse> addToCart(@Body AddToCartRequest request);

    @POST("api/productsRouter/update-cart-quantity")
    Call<Response<ArrayList<CartItem>>> updateCartQuantity(@Body UpdateCartQuantityRequest request);

    @DELETE("api/productsRouter/delete-cart-item/{id}")
    Call<Response<ArrayList<CartItem>>> deleteCartItem(@Path("id") String id);

    @POST("api/productsRouter/add-review")
    Call<Response<ProductDetailResponse>> addReview(@Body com.example.storehub.model.AddReviewRequest request);

    // Lấy danh sách toàn bộ tin tức đã xuất bản
    @GET("api/newsRouter/get-all-news")
    Call<Response<ArrayList<News>>> getListNews(@Query("page") int page,
                                                @Query("limit") int limit);

    // Lấy chi tiết một bài viết tin tức dựa vào ID
    @GET("api/newsRouter/get-news-by-id/{id}")
    Call<Response<News>> getNewsById(@Path("id") String id);

    @POST("users/register")
    Call<Response<User>> register(@Body RegisterRequest request);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

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
