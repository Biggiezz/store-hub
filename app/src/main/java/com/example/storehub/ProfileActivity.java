package com.example.storehub;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.storehub.auth.LoginActivity;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtProfileName, txtEmailValue, txtPhoneValue, txtAddressValue, txtPasswordChangedSub, btnLangVI, btnLangEN;
    private ImageView imgProfileAvatar;

    private SharedPreferencesManager sharedPreferencesManager;
    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    bindUserData();
                }
            }
    );

    private final ActivityResultLauncher<Intent> changePasswordLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    bindUserData();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferencesManager = new SharedPreferencesManager(this);

        initUi();
        bindUserData();
        setupClickListeners();
    }

    private void initUi() {
        txtProfileName = findViewById(R.id.txtProfileName);
        txtEmailValue = findViewById(R.id.txtEmailValue);
        txtPhoneValue = findViewById(R.id.txtPhoneValue);
        txtAddressValue = findViewById(R.id.txtAddressValue);
        txtPasswordChangedSub = findViewById(R.id.txtPasswordChangedSub);

        btnLangVI = findViewById(R.id.btnLangVI);
        btnLangEN = findViewById(R.id.btnLangEN);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
    }

    private void bindUserData() {
        User user = sharedPreferencesManager.getUser();
        if (user == null) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtProfileName.setText(user.getName() + " * " + (user.getRole().equalsIgnoreCase("admin") ? "Người quản lý" : "Khách hàng"));
        txtEmailValue.setText(user.getEmail());
        txtPhoneValue.setText(user.getPhone());
        txtAddressValue.setText(user.getAddress() == null || user.getAddress().isEmpty() ? "Chưa cập nhật địa chỉ" : user.getAddress());

        updatePasswordChangeSubtext(user.getChangePasswordDate());

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            Glide.with(this)
                    .load(user.getImage())
                    .placeholder(R.drawable.ic_avatar)
                    .into(imgProfileAvatar);
        }
    }

    private void updatePasswordChangeSubtext(String changeDateStr) {
        if (changeDateStr == null || changeDateStr.isEmpty()) {
            TextView txtSub = findViewById(R.id.profile_activity).findViewWithTag("password_sub");
            if (txtSub != null) txtSub.setText("Chưa đổi mật khẩu");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date changeDate = sdf.parse(changeDateStr);
            Date currentDate = new Date();

            if (changeDate != null) {
                long diffMs = currentDate.getTime() - changeDate.getTime();
                long diffDays = diffMs / (1000 * 60 * 60 * 24);

                String relativeText;
                if (diffDays < 1) {
                    relativeText = "Đã thay đổi hôm nay";
                } else if (diffDays < 30) {
                    relativeText = "Đã thay đổi " + diffDays + " ngày trước";
                } else {
                    long diffMonths = diffDays / 30;
                    relativeText = "Đã thay đổi " + diffMonths + " tháng trước";
                }

                if (txtPasswordChangedSub != null) {
                    txtPasswordChangedSub.setText(relativeText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (txtPasswordChangedSub != null) {
                txtPasswordChangedSub.setText("Đã thay đổi 3 tháng trước");
            }
        }
    }

    private void setupClickListeners() {
        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEditAvatar).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        findViewById(R.id.btnEditPersonalInfo).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            changePasswordLauncher.launch(intent);
        });

        btnLangVI.setOnClickListener(v -> selectLanguage(true));

        btnLangEN.setOnClickListener(v -> selectLanguage(false));

        findViewById(R.id.btnLogout).setOnClickListener(v -> showCustomLogoutDialog());

        findViewById(R.id.btnHome).setOnClickListener(v -> finish());
        findViewById(R.id.btnProducts).setOnClickListener(v -> {
            Toast.makeText(this, "Màn hình Sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.btnCart).setOnClickListener(v -> {
            Toast.makeText(this, "Giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.btnNews).setOnClickListener(v -> {
            Toast.makeText(this, "Tin tức", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void selectLanguage(boolean isVI) {
        if (isVI) {
            btnLangVI.setBackgroundResource(R.drawable.bg_language_selected);
            btnLangVI.setTextColor(Color.parseColor("#41413F"));
            btnLangVI.setTypeface(null, android.graphics.Typeface.BOLD);

            btnLangEN.setBackgroundColor(Color.TRANSPARENT);
            btnLangEN.setTextColor(Color.parseColor("#8F8E8A"));
            btnLangEN.setTypeface(null, android.graphics.Typeface.NORMAL);

            Toast.makeText(this, "Đã chuyển ngôn ngữ sang Tiếng Việt", Toast.LENGTH_SHORT).show();
        } else {
            btnLangEN.setBackgroundResource(R.drawable.bg_language_selected);
            btnLangEN.setTextColor(Color.parseColor("#41413F"));
            btnLangEN.setTypeface(null, android.graphics.Typeface.BOLD);

            btnLangVI.setBackgroundColor(Color.TRANSPARENT);
            btnLangVI.setTextColor(Color.parseColor("#8F8E8A"));
            btnLangVI.setTypeface(null, android.graphics.Typeface.NORMAL);

            Toast.makeText(this, "Language switched to English", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCustomLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }

        MaterialButton btnConfirmLogout = dialog.findViewById(R.id.btnConfirmLogout);
        MaterialButton btnCancelLogout = dialog.findViewById(R.id.btnCancelLogout);

        btnConfirmLogout.setOnClickListener(v -> {
            dialog.dismiss();
            performServerLogout();
        });

        btnCancelLogout.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void performServerLogout() {
        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().logout(tokenHeader).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                // Wipe local preferences anyway
                sharedPreferencesManager.logout();
                Toast.makeText(ProfileActivity.this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                sharedPreferencesManager.logout();
                Toast.makeText(ProfileActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
