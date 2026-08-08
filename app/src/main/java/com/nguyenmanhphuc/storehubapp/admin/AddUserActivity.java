package com.nguyenmanhphuc.storehubapp.admin;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AddUserActivity extends AppCompatActivity {

    private TextView tvHeaderTitle, tvAvatarHint;
    private User userToEdit = null;
    private ImageButton btnBack;
    private RelativeLayout rlAvatarPicker;
    private ShapeableImageView ivAvatar;
    private EditText etFullName, etPhone, etEmail, etPassword, etAddress;
    private Spinner spRole;
    private ImageView ivTogglePassword;
    private MaterialButton btnCancel, btnSaveUser;
    private boolean isPasswordVisible = false;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAvatar.setImageTintList(null);
                    ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(this)
                            .load(selectedImageUri)
                            .centerCrop()
                            .into(ivAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_user_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();
        setUpAdapter();
        setUpListener();
        checkEditMode();
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("user_edit")) {
            String json = getIntent().getStringExtra("user_edit");
            userToEdit = new Gson().fromJson(json, User.class);
        }

        if (userToEdit != null) {
            if (tvHeaderTitle != null) {
                tvHeaderTitle.setText(getString(R.string.edit_user_title));
            }
            btnSaveUser.setText(getString(R.string.save_changes));

            etFullName.setText(userToEdit.getName());
            etPhone.setText(userToEdit.getPhone());
            etEmail.setText(userToEdit.getEmail());
            etEmail.setEnabled(false);
            etEmail.setFocusable(false);
            etEmail.setFocusableInTouchMode(false);

            rlAvatarPicker.setEnabled(false);
            rlAvatarPicker.setClickable(false);
            if (tvAvatarHint != null) {
                tvAvatarHint.setVisibility(View.GONE);
            }

            etAddress.setText(userToEdit.getAddress());
            etPassword.setText("********");
            etPassword.setEnabled(false);
            etPassword.setFocusable(false);
            etPassword.setFocusableInTouchMode(false);
            etPassword.setHint(getString(R.string.password_secured_hint));
            if (ivTogglePassword != null) {
                ivTogglePassword.setEnabled(false);
                ivTogglePassword.setVisibility(View.GONE);
            }

            // Tìm và chọn vai trò tương ứng trong Spinner
            for (int i = 0; i < spRole.getCount(); i++) {
                if (spRole.getItemAtPosition(i).toString().equalsIgnoreCase(userToEdit.getRole())) {
                    spRole.setSelection(i);
                    break;
                }
            }

            // Tải ảnh đại diện cũ bằng Glide
            if (userToEdit.getImage() != null && !userToEdit.getImage().isEmpty()) {
                ivAvatar.setImageTintList(null);
                ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(this)
                        .load(userToEdit.getImage())
                        .centerCrop()
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .into(ivAvatar);
            }
        }
    }

    private void initUi() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvAvatarHint = findViewById(R.id.tvAvatarHint);
        btnBack = findViewById(R.id.btnBack);
        rlAvatarPicker = findViewById(R.id.rlAvatarPicker);
        ivAvatar = findViewById(R.id.ivAvatar);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        spRole = findViewById(R.id.spRole);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        etAddress = findViewById(R.id.etAddress);
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveUser = findViewById(R.id.btnSaveUser);
    }

    private void setUpAdapter() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);
        User currentUser = prefManager.getUser();

        ArrayList<String> roleList = new ArrayList<>();
        roleList.add("Chọn vai trò");
        if (currentUser != null && currentUser.isSuperAdmin()) {
            roleList.add("superadmin");
        }
        roleList.add("admin");
        roleList.add("customer");

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roleList
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(roleAdapter);
    }

    private void setUpListener() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        rlAvatarPicker.setOnClickListener(v -> openGallery());

        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        btnSaveUser.setOnClickListener(v -> validateAndSaveUser());
    }

    private void openGallery() {
        imagePickerLauncher.launch("image/*");
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void validateAndSaveUser() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem() != null ? spRole.getSelectedItem().toString() : "";
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError(getString(R.string.register_fullname_required));
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError(getString(R.string.register_phone_required));
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.enter_email_required));
            etEmail.requestFocus();
            return;
        }

        if ("Chọn vai trò".equals(role) || TextUtils.isEmpty(role)) {
            Toast.makeText(this, this.getString(R.string.toast_vui_long_chon_vai_tro_cho_nguoi_dung), Toast.LENGTH_SHORT).show();
            return;
        }

        if (userToEdit == null && TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.register_password_required));
            etPassword.requestFocus();
            return;
        }

        User newUser = new User(
                userToEdit != null ? userToEdit.getId() : null,
                fullName,
                email,
                phone,
                role,
                selectedImageUri != null ? selectedImageUri.toString() : (userToEdit != null ? userToEdit.getRawImage() : ""),
                address,
                userToEdit != null ? userToEdit.getLastActive() : getString(R.string.just_now)
        );
        // Mật khẩu chỉ được gán khi thêm mới người dùng (không cho phép sửa)
        if (userToEdit == null && !TextUtils.isEmpty(password)) {
            newUser.setPassword(password);
        }

        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);
        String token = "Bearer " + prefManager.getToken();
        HttpResquest httpResquest = new HttpResquest();
        if (userToEdit != null) {
            httpResquest.callAPI().updateUser(token, userToEdit.getId(), newUser).enqueue(new retrofit2.Callback<Response<User>>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getCode() == 201) {
                        Toast.makeText(AddUserActivity.this, String.format(getString(R.string.add_user_success_format), fullName), Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(AddUserActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AddUserActivity.this, AddUserActivity.this.getString(R.string.toast_loi_khi_them_nguoi_dung), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<Response<User>> call, @NonNull Throwable t) {
                    Toast.makeText(AddUserActivity.this, String.format(getString(R.string.server_connection_error_prefix), t.getMessage()), Toast.LENGTH_LONG).show();
                }
            });

        } else {
            httpResquest.callAPI().addUser(token, newUser).enqueue(new retrofit2.Callback<Response<User>>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getCode() == 201) {
                        Toast.makeText(AddUserActivity.this, String.format(getString(R.string.add_user_success_format), fullName), Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        Toast.makeText(AddUserActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AddUserActivity.this, AddUserActivity.this.getString(R.string.toast_loi_khi_them_nguoi_dung), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<Response<User>> call, @NonNull Throwable t) {
                    Toast.makeText(AddUserActivity.this, String.format(getString(R.string.server_connection_error_prefix), t.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
