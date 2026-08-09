package com.nguyenmanhphuc.storehubapp.services;

import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Category;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.request.AddToCartRequest;
import com.nguyenmanhphuc.storehubapp.model.request.AddReviewRequest;
import com.nguyenmanhphuc.storehubapp.model.request.CancelOrderRequest;
import com.nguyenmanhphuc.storehubapp.model.request.LoginRequest;
import com.nguyenmanhphuc.storehubapp.model.request.RegisterRequest;
import com.nguyenmanhphuc.storehubapp.model.request.ReplyReviewRequest;
import com.nguyenmanhphuc.storehubapp.model.request.UpdateQuantityRequest;
import com.nguyenmanhphuc.storehubapp.model.request.UpdateStatusRequest;
import com.nguyenmanhphuc.storehubapp.model.response.DashboardData;
import com.nguyenmanhphuc.storehubapp.model.response.LoginResponse;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.response.RevenueData;

import com.nguyenmanhphuc.storehubapp.model.response.RecentActivity;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiServices {
    @GET("api/productsRouter/get-categories")
    Call<Response<ArrayList<Category>>> getCategories();

    @GET("api/productsRouter/get-all-product")
    Call<Response<ArrayList<Product>>> getListProduct(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("showInactive") boolean showInactive,
            @Query("sort") String sort
    );

    @GET("api/productsRouter/search-product")
    Call<Response<ArrayList<Product>>> searchProduct(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("keyword") String keyword,
            @Query("category") String category,
            @Query("showInactive") boolean showInactive,
            @Query("sort") String sort
    );

    @GET("api/productsRouter/get-product-by-id/{id}")
    Call<Response<Product>> getProductDetail(@Path("id") String id);

    @Multipart
    @POST("api/productsRouter/add-product")
    Call<Response<Product>> addProduct(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("stock") RequestBody stock,
            @Part("soldQuantity") RequestBody soldQuantity,
            @Part("isActive") RequestBody isActive,
            @Part("colors") RequestBody colors,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("api/productsRouter/update-product/{id}")
    Call<Response<Product>> updateProduct(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("stock") RequestBody stock,
            @Part("soldQuantity") RequestBody soldQuantity,
            @Part("isActive") RequestBody isActive,
            @Part("colors") RequestBody colors,
            @Part MultipartBody.Part image
    );

    @GET("api/productsRouter/get-cart")
    Call<Response<ArrayList<CartItem>>> getCart(@Header("Authorization") String token);

    @POST("api/productsRouter/add-to-cart")
    Call<Response<Object>> addToCart(@Header("Authorization") String token, @Body AddToCartRequest request);

    @POST("api/productsRouter/update-cart-quantity")
    Call<Response<ArrayList<CartItem>>> updateCartQuantity(@Header("Authorization") String token, @Body UpdateQuantityRequest request);

    @DELETE("api/productsRouter/delete-cart-item/{id}")
    Call<Response<ArrayList<CartItem>>> deleteCartItem(@Header("Authorization") String token, @Path("id") String id);

    @Multipart
    @POST("api/productsRouter/add-review")
    Call<Response<Product>> addReview(
            @Part("productId") okhttp3.RequestBody productId,
            @Part("customerName") okhttp3.RequestBody customerName,
            @Part("customerImage") okhttp3.RequestBody customerImage,
            @Part("rating") okhttp3.RequestBody rating,
            @Part("content") okhttp3.RequestBody content,
            @Part("orderId") okhttp3.RequestBody orderId,
            @Part java.util.List<okhttp3.MultipartBody.Part> mediaFiles
    );

    @POST("api/productsRouter/reply-review")
    Call<Response<Product>> replyReview(@Body ReplyReviewRequest request);

    @POST("api/oderRouter/create-order")
    Call<Response<Order>> createOrder(
            @Header("Authorization") String token,
            @Query("paymentMethod") String paymentMethod,
            @Query("appTransId") String appTransId
    );

    @GET("api/oderRouter/get-orders")
    Call<Response<ArrayList<Order>>> getOrders(@Header("Authorization") String token);

    @GET("api/oderRouter/admin/orders")
    Call<Response<ArrayList<Order>>> getAdminOrders(
            @Header("Authorization") String token
    );

    @GET("api/oderRouter/admin/orders/{id}")
    Call<Response<Order>> getAdminOrderDetail(
            @Header("Authorization") String token,
            @Path("id") String orderId
    );

    @PUT("api/oderRouter/admin/orders/{id}/status")
    Call<Response<Order>> updateAdminOrderStatus(
            @Header("Authorization") String token,
            @Path("id") String orderId,
            @Body UpdateStatusRequest request
    );

    @POST("api/oderRouter/cancel-order")
    Call<Response<Order>> cancelOrder(@Body CancelOrderRequest request);

    @POST("api/oderRouter/update-status")
    Call<Response<Order>> updateOrderStatus(@Body UpdateStatusRequest request);

    @POST("api/oderRouter/clear-cart")
    Call<Response<Object>> clearCart(@Header("Authorization") String token);

    // Lấy danh sách toàn bộ tin tức đã xuất bản
    @GET("api/newsRouter/get-all-news")
    Call<Response<ArrayList<News>>> getListNews(@Query("page") int page,
                                                @Query("limit") int limit,
                                                @Query("status") String status);

    // Lấy chi tiết một bài viết tin tức dựa vào ID
    @GET("api/newsRouter/get-news-by-id/{id}")
    Call<Response<News>> getNewsById(@Path("id") String id);

    @POST("api/newsRouter/add-news")
    Call<Response<News>> addNews(@Body News news);

    @DELETE("api/newsRouter/delete-news/{id}")
    Call<Response<Void>> deleteNews(@Path("id") String id);

    @GET("api/usersRouter/admin/recent-activities")
    Call<Response<ArrayList<RecentActivity>>> getRecentActivities(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("type") String type
    );

    @DELETE("api/productsRouter/delete-product/{id}")
    Call<Response<Void>> deleteProduct(@Header("Authorization") String token, @Path("id") String productId);

    @DELETE("users/delete-user/{id}")
    Call<Response<Void>> deleteUser(@Header("Authorization") String token, @Path("id") String userId);

    @POST("users/register")
    Call<Response<User>> register(@Body RegisterRequest request);

    @POST("users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("users/get-all-users")
    Call<Response<ArrayList<User>>> getListUsers(@Header("Authorization") String token);

    @GET("users/get-user-by-id/{id}")
    Call<Response<User>> getUserById(@Path("id") String id);

    @POST("users/add-user")
    Call<Response<User>> addUser(@Header("Authorization") String token, @Body User user);

    @PUT("users/update-user/{id}")
    Call<Response<User>> updateUser(
            @Header("Authorization") String token,
            @Path("id") String userId,
            @Body User user
    );

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

    @Multipart
    @PUT("api/newsRouter/admin/update-news/{id}")
    Call<Response<News>> updateAdminNews(@Header("Authorization") String token,
                                         @Path("id") String id,
                                         @Part("title") RequestBody title,
                                         @Part("content") RequestBody content,
                                         @Part("status") RequestBody status,
                                         @Part MultipartBody.Part image);

    @DELETE("api/newsRouter/admin/delete-news/{id}")
    Call<Response<Void>> deleteAdminNews(@Header("Authorization") String token, @Path("id") String id);

    @PUT("users/update-profile")
    Call<Response<User>> updateProfile(@Header("Authorization") String authHeader, @Body Map<String, String> body);

    @Multipart
    @PUT("users/update-profile")
    Call<Response<User>> updateProfileMultipart(
            @Header("Authorization") String authHeader,
            @Part("name") RequestBody name,
            @Part("phone") RequestBody phone,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part image
    );

    @PUT("users/change-password")
    Call<Response<Void>> changePassword(@Header("Authorization") String authHeader, @Body Map<String, String> body);

    @POST("users/logout")
    Call<Response<Void>> logout(@Header("Authorization") String authHeader);

    @POST("users/set-offline")
    Call<Response<Void>> setOffline(@Header("Authorization") String authHeader);

    @GET("users/admin/dashboard")
    Call<Response<DashboardData>> getAdminDashboardStats();

    @GET("users/admin/revenue-stats")
    Call<Response<RevenueData>> getRevenueStats(@Query("period") int period);

    @GET("users/admin/revenue-stats")
    Call<Response<RevenueData>> getRevenueStatsWithLimit(
            @Query("period") int period,
            @Query("activityLimit") int activityLimit
    );
}
