package com.example.storehub.services;

import com.example.storehub.model.AdminStats;
import com.example.storehub.model.ApiMessageResponse;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.News;
import com.example.storehub.model.Product;
import com.example.storehub.model.ProductReview;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.model.Order;
import com.example.storehub.model.CancelOrderRequest;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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
    Call<Response<Product>> getProductDetail(@Path("id") String id);

    @POST("api/productsRouter/add-product")
    Call<Response<Product>> addProduct(@Body Product product);

    @GET("api/productsRouter/get-cart")
    Call<Response<ArrayList<CartItem>>> getCart();

    @POST("api/productsRouter/add-to-cart")
    Call<ApiMessageResponse> addToCart(@Body CartItem.AddToCartRequest request);

    @POST("api/productsRouter/update-cart-quantity")
    Call<Response<ArrayList<CartItem>>> updateCartQuantity(@Body CartItem.UpdateQuantityRequest request);

    @DELETE("api/productsRouter/delete-cart-item/{id}")
    Call<Response<ArrayList<CartItem>>> deleteCartItem(@Path("id") String id);

    @POST("api/productsRouter/add-review")
    Call<Response<Product>> addReview(@Body ProductReview.AddRequest request);

    @POST("api/oderRouter/create-order")
    Call<Response<Order>> createOrder();

    @GET("api/oderRouter/get-orders")
    Call<Response<ArrayList<Order>>> getOrders();

    @POST("api/oderRouter/cancel-order")
    Call<Response<Order>> cancelOrder(@Body CancelOrderRequest request);

    @POST("api/oderRouter/clear-cart")
    Call<Response<Object>> clearCart();

    // Lấy danh sách toàn bộ tin tức đã xuất bản
    @GET("api/newsRouter/get-all-news")
    Call<Response<ArrayList<News>>> getListNews(@Query("page") int page,
                                                @Query("limit") int limit);

    // Lấy chi tiết một bài viết tin tức dựa vào ID
    @GET("api/newsRouter/get-news-by-id/{id}")
    Call<Response<News>> getNewsById(@Path("id") String id);

    @POST("users/register")
    Call<Response<User>> register(@Body User.RegisterRequest request);

    @POST("users/login")
    Call<User.LoginResponse> login(@Body User.LoginRequest request);

    @GET("api/newsRouter/admin/get-all-news")
    Call<Response<ArrayList<News>>> getAdminListNews(@Header("Authorization") String token,
                                                     @Query("page") int page,
                                                     @Query("limit") int limit);

    @Multipart
    @POST("api/newsRouter/admin/add-news")
    Call<Response<News>> addAdminNews(@Header("Authorization") String token,
                                      @Part("title") RequestBody title,
                                      @Part("content") RequestBody content,
                                      @Part("status") RequestBody status,
                                      @Part MultipartBody.Part image);

    @PUT("api/newsRouter/admin/update-news/{id}")
    Call<Response<News>> updateAdminNews(@Header("Authorization") String token, @Path("id") String id, @Body News news);

    @DELETE("api/newsRouter/admin/delete-news/{id}")
    Call<Response<Void>> deleteAdminNews(@Header("Authorization") String token, @Path("id") String id);

    @PUT("users/update-profile")
    Call<Response<User>> updateProfile(@Header("Authorization") String authHeader, @Body Map<String, String> body);

    @PUT("users/change-password")
    Call<Response<Void>> changePassword(@Header("Authorization") String authHeader, @Body Map<String, String> body);

    @POST("users/logout")
    Call<Response<Void>> logout(@Header("Authorization") String authHeader);

    @GET("users/admin/dashboard")
    Call<Response<AdminStats.DashboardData>> getAdminDashboardStats();

    @POST("api/productsRouter/checkout")
    Call<Response<Object>> checkout();

    @GET("users/admin/revenue-stats")
    Call<Response<AdminStats.RevenueData>> getRevenueStats(@Query("period") int period);
}
