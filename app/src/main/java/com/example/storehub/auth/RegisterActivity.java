package com.example.storehub.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storehub.BaseActivity;
import com.example.storehub.R;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText edtFullName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private MaterialButton btnRegister, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        setUpListener();
    }

    private void initUi() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> handleRegister());
        }
    }

    private void handleRegister() {
        String name = edtFullName != null && edtFullName.getText() != null ? edtFullName.getText().toString().trim() : "";
        String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String phone = edtPhone != null && edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
        String password = edtPassword != null && edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword != null && edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Request gửi đi
        User.RegisterRequest request = new User.RegisterRequest(name, email, phone, password);

        // Gọi API
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().register(request).enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(@NonNull Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<User> apiResponse = response.body();
                    if (apiResponse.getCode() == 201) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        // Quay lại màn hình Login
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại hoặc email đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<User>> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
