package com.nguyenmanhphuc.storehubapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AlertDialog;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.auth.LoginActivity;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import com.nguyenmanhphuc.storehubapp.utils.LocaleHelper;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;

public class ProfileActivity extends BaseActivity {

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
        updateLanguageToggleUI();
        bindUserData();
        setupClickListeners();
    }

    private void updateLanguageToggleUI() {
        String lang = SharedPreferencesManager.getInstance(this).getLanguage();
        if (lang.equals("vi")) {
            btnLangVI.setBackgroundResource(R.drawable.bg_language_selected);
            btnLangVI.setTextColor(Color.parseColor("#41413F"));
            btnLangVI.setTypeface(null, android.graphics.Typeface.BOLD);

            btnLangEN.setBackgroundColor(Color.TRANSPARENT);
            btnLangEN.setTextColor(Color.parseColor("#8F8E8A"));
            btnLangEN.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            btnLangEN.setBackgroundResource(R.drawable.bg_language_selected);
            btnLangEN.setTextColor(Color.parseColor("#41413F"));
            btnLangEN.setTypeface(null, android.graphics.Typeface.BOLD);

            btnLangVI.setBackgroundColor(Color.TRANSPARENT);
            btnLangVI.setTextColor(Color.parseColor("#8F8E8A"));
            btnLangVI.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
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
            Toast.makeText(this, R.string.no_user_info, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String role = user.getRole().equalsIgnoreCase("admin") ? getString(R.string.role_admin) : getString(R.string.role_customer);
        txtProfileName.setText(user.getName() + " • " + role);
        txtEmailValue.setText(user.getEmail());
        txtPhoneValue.setText(user.getPhone());
        txtAddressValue.setText(user.getAddress() == null || user.getAddress().isEmpty() ? getString(R.string.no_address_update) : user.getAddress());

        updatePasswordChangeSubtext(user.getChangePasswordDate());

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            Glide.with(this)
                    .load(user.getImage())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(imgProfileAvatar);
        }

        if (user.getId() != null && !user.getId().isEmpty()) {
            new HttpResquest().callAPI().getUserById(user.getId()).enqueue(new Callback<Response<User>>() {
                @Override
                public void onResponse(@NonNull Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        User freshUser = response.body().getData();
                        sharedPreferencesManager.updateUser(freshUser);
                        if (freshUser.getImage() != null && !freshUser.getImage().isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(freshUser.getImage())
                                    .placeholder(R.drawable.ic_avatar)
                                    .error(R.drawable.ic_avatar)
                                    .into(imgProfileAvatar);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Response<User>> call, @NonNull Throwable t) {
                }
            });
        }
    }

    private void updatePasswordChangeSubtext(String changeDateStr) {
        if (changeDateStr == null || changeDateStr.isEmpty()) {
            TextView txtSub = findViewById(R.id.profile_activity).findViewWithTag("password_sub");
            if (txtSub != null) txtSub.setText(getString(R.string.no_password_change));
            return;
        }

        try {
            Date changeDate = DateTimeUtils.parseISO(changeDateStr);
            Date currentDate = new Date();

            if (changeDate != null) {
                long diffMs = currentDate.getTime() - changeDate.getTime();
                long diffDays = diffMs / (1000 * 60 * 60 * 24);

                String relativeText;
                if (diffDays < 1) {
                    relativeText = getString(R.string.password_changed_today);
                } else if (diffDays < 30) {
                    relativeText = String.format(getString(R.string.password_changed_days_ago), diffDays);
                } else {
                    long diffMonths = diffDays / 30;
                    relativeText = String.format(getString(R.string.password_changed_months_ago), diffMonths);
                }

                if (txtPasswordChangedSub != null) {
                    txtPasswordChangedSub.setText(relativeText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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
    }

    private void selectLanguage(boolean isVI) {
        String lang = isVI ? "vi" : "en";
        LocaleHelper.setLocale(this, lang);

        // Cập nhật UI ngay lập tức bằng cách khởi động lại activity
        Intent intent = getIntent();
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void showCustomLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        MaterialButton btnConfirmLogout = dialogView.findViewById(R.id.btnConfirmLogout);
        MaterialButton btnCancelLogout = dialogView.findViewById(R.id.btnCancelLogout);

        btnConfirmLogout.setOnClickListener(v -> {
            dialog.dismiss();
            performServerLogout();
        });

        btnCancelLogout.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void performServerLogout() {
        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().logout(tokenHeader).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                // Wipe local preferences and static cache
                sharedPreferencesManager.logout();
                MainActivity.preloadedProducts = null;
                MainActivity.preloadedNews = null;
                Toast.makeText(getApplicationContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                sharedPreferencesManager.logout();
                MainActivity.preloadedProducts = null;
                MainActivity.preloadedNews = null;
                Toast.makeText(getApplicationContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
