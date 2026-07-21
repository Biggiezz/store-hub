package com.example.storehub.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.storehub.MainActivity;
import com.example.storehub.R;
import com.example.storehub.admin.HomePageManagement;
import com.example.storehub.model.LoginRequest;
import com.example.storehub.model.LoginResponse;
import com.example.storehub.model.News;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private TextView tvRegisterNow;
    private SharedPreferencesManager prefManager;

    private ArrayList<Product> preloadedProducts = null;
    private ArrayList<News> preloadedNews = null;
    private boolean isProductsCallDone = false;
    private boolean isNewsCallDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        prefManager = new SharedPreferencesManager(this);

        initUi();
        setUpListener();
    }

    private void initUi() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
    }

    private void setUpListener() {
        if (tvRegisterNow != null) {
            tvRegisterNow.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không đúng định dạng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Khởi tạo Login Request
        LoginRequest request = new LoginRequest(email, password);

        // Gọi API Đăng nhập
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        // Lưu Token & thông tin User
                        prefManager.saveToken(apiResponse.getToken());
                        prefManager.saveUser(apiResponse.getData());

                        // Cập nhật thông báo tải dữ liệu
                        progressDialog.setMessage("Đang tải dữ liệu sản phẩm...");

                        // Tải trước dữ liệu
                        preloadData(progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Sai tài khoản hoặc mật khẩu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preloadData(ProgressDialog progressDialog) {
        HttpResquest httpResquest = new HttpResquest();

        isProductsCallDone = false;
        isNewsCallDone = false;
        preloadedProducts = null;
        preloadedNews = null;

        // Tải danh sách sản phẩm (50 sản phẩm để phục vụ lọc danh mục)
        httpResquest.callAPI().getListProduct(1, 50).enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                isProductsCallDone = true;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<Product>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        preloadedProducts = apiResponse.getData();
                    }
                }
                checkPreloadComplete(progressDialog);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                isProductsCallDone = true;
                checkPreloadComplete(progressDialog);
            }
        });

        // Tải danh sách tin tức
        httpResquest.callAPI().getListNews(1, 5).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                isNewsCallDone = true;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<News>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        preloadedNews = apiResponse.getData();
                    }
                }
                checkPreloadComplete(progressDialog);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                isNewsCallDone = true;
                checkPreloadComplete(progressDialog);
            }
        });
    }

    private void checkPreloadComplete(ProgressDialog progressDialog) {
        if (isProductsCallDone && isNewsCallDone) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            // Gán dữ liệu preload vào cache static của MainActivity
            MainActivity.preloadedProducts = preloadedProducts;
            MainActivity.preloadedNews = preloadedNews;

            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            /// Kiểm tra role để chuyển đến màn hình Quản trị (HomePageManagement) hoặc Trang người dùng (MainActivity)
            User user = prefManager.getUser();
            String role = user != null && user.getRole() != null ? user.getRole().trim().toLowerCase() : "";

            Intent intent;
            if (role.equals("admin") || role.equals("super admin") || role.equals("superadmin")) {
                intent = new Intent(LoginActivity.this, HomePageManagement.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }
    }
}
