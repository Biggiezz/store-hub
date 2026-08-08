package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordActivity extends BaseActivity {

    private EditText edtCurrentPassword, edtNewPassword, edtConfirmNewPassword;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private MaterialButton btnUpdatePassword;

    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.change_password_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferencesManager = new SharedPreferencesManager(this);

        initUi();
        setupClickListeners();
    }

    private void initUi() {
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);

        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = findViewById(R.id.tilConfirmNewPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnUpdatePassword.setOnClickListener(v -> handlePasswordUpdate());

        if (edtCurrentPassword != null) {
            edtCurrentPassword.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilCurrentPassword != null) tilCurrentPassword.setError(null);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
        if (edtNewPassword != null) {
            edtNewPassword.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilNewPassword != null) tilNewPassword.setError(null);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
        if (edtConfirmNewPassword != null) {
            edtConfirmNewPassword.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilConfirmNewPassword != null) tilConfirmNewPassword.setError(null);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void handlePasswordUpdate() {
        String oldPass = edtCurrentPassword.getText().toString();
        String newPass = edtNewPassword.getText().toString();
        String confirmPass = edtConfirmNewPassword.getText().toString();

        if (tilCurrentPassword != null) tilCurrentPassword.setError(null);
        if (tilNewPassword != null) tilNewPassword.setError(null);
        if (tilConfirmNewPassword != null) tilConfirmNewPassword.setError(null);

        if (oldPass.isEmpty()) {
            if (tilCurrentPassword != null) {
                tilCurrentPassword.setError(getString(R.string.password_current_required));
                tilCurrentPassword.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_nhap_mat_khau_hien_tai), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (newPass.isEmpty()) {
            if (tilNewPassword != null) {
                tilNewPassword.setError(getString(R.string.password_new_required));
                tilNewPassword.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_nhap_mat_khau_moi), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (newPass.length() < 8 || !newPass.matches(".*[a-zA-Z].*") || !newPass.matches(".*\\d.*")) {
            if (tilNewPassword != null) {
                tilNewPassword.setError(getString(R.string.password_new_validation));
                tilNewPassword.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_mat_khau_moi_phai_toi_thieu_8_ky_tu_bao_), Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (confirmPass.isEmpty()) {
            if (tilConfirmNewPassword != null) {
                tilConfirmNewPassword.setError(getString(R.string.password_confirm_required));
                tilConfirmNewPassword.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_xac_nhan_mat_khau_moi), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!newPass.equals(confirmPass)) {
            if (tilConfirmNewPassword != null) {
                tilConfirmNewPassword.setError(getString(R.string.password_confirm_mismatch));
                tilConfirmNewPassword.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_mat_khau_xac_nhan_khong_khop), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        LoadingDialogHelper loadingDialog = new LoadingDialogHelper(this);
        loadingDialog.setMessage(getString(R.string.updating_password));
        loadingDialog.show();

        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPass);
        body.put("newPassword", newPass);

        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().changePassword(tokenHeader, body).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Response<Void> res = response.body();
                    if (res.getCode() == 200) {
                        Toast.makeText(ChangePasswordActivity.this, ChangePasswordActivity.this.getString(R.string.toast_doi_mat_khau_thanh_cong), Toast.LENGTH_SHORT).show();

                        User user = sharedPreferencesManager.getUser();
                        if (user != null) {
                            user.setChangePasswordDate(DateTimeUtils.formatToISO(new Date()));
                            sharedPreferencesManager.updateUser(user);
                        }

                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (tilCurrentPassword != null) {
                        tilCurrentPassword.setError(getString(R.string.password_incorrect));
                        tilCurrentPassword.requestFocus();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, ChangePasswordActivity.this.getString(R.string.toast_mat_khau_hien_tai_khong_chinh_xac), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ChangePasswordActivity.this, ChangePasswordActivity.this.getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
