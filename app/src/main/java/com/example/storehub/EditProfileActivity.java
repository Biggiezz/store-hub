package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.services.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtProfileName;
    private EditText edtProfileEmail;
    private EditText edtProfilePhone;
    private EditText edtProfileAddress;
    
    private ImageView imgSmallAvatar;
    private ImageView imgLargeAvatar;
    private MaterialButton btnSaveChanges;
    
    private SessionManager sessionManager;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        currentUser = sessionManager.getUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        bindUserData();
        setupClickListeners();
    }

    private void initViews() {
        edtProfileName = findViewById(R.id.edtProfileName);
        edtProfileEmail = findViewById(R.id.edtProfileEmail);
        edtProfilePhone = findViewById(R.id.edtProfilePhone);
        edtProfileAddress = findViewById(R.id.edtProfileAddress);
        
        imgSmallAvatar = findViewById(R.id.imgSmallAvatar);
        imgLargeAvatar = findViewById(R.id.imgLargeAvatar);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
    }

    private void bindUserData() {
        edtProfileName.setText(currentUser.getName());
        edtProfileEmail.setText(currentUser.getEmail());
        edtProfilePhone.setText(currentUser.getPhone());
        edtProfileAddress.setText(currentUser.getAddress());

        // Load avatar if exists
        if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
            Glide.with(this).load(currentUser.getImage()).placeholder(R.drawable.ic_avatar).into(imgSmallAvatar);
            Glide.with(this).load(currentUser.getImage()).placeholder(R.drawable.ic_avatar).into(imgLargeAvatar);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnChangeAvatarPhoto).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thay đổi ảnh đại diện (Đang phát triển)", Toast.LENGTH_SHORT).show();
        });

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void saveProfileChanges() {
        String name = edtProfileName.getText().toString().trim();
        String phone = edtProfilePhone.getText().toString().trim();
        String address = edtProfileAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("phone", phone);
        body.put("address", address);

        String tokenHeader = "Bearer " + sessionManager.getToken();

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().updateProfile(tokenHeader, body).enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<User> res = response.body();
                    if (res.getCode() == 200 && res.getData() != null) {
                        sessionManager.updateUser(res.getData());
                        Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không thể cập nhật thông tin. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<User>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
