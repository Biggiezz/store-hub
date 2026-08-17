package com.nguyenmanhphuc.storehubapp.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilOtp, tilNewPassword;
    private TextInputEditText etEmail, etOtp, etNewPassword;
    private LinearLayout layoutResetFields;
    private MaterialButton btnAction;
    private TextView tvBackToLogin, tvDescription;

    private boolean isOtpSent = false;
    private ApiServices apiServices;
    private LoadingDialogHelper loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiServices = new HttpResquest().callAPI();
        loadingDialog = new LoadingDialogHelper(this);

        initUi();
        setUpListener();
    }

    private void initUi() {
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilOtp = findViewById(R.id.tilOtp);
        etOtp = findViewById(R.id.etOtp);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        layoutResetFields = findViewById(R.id.layoutResetFields);
        btnAction = findViewById(R.id.btnAction);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvDescription = findViewById(R.id.tvDescription);
    }

    private void setUpListener() {
        btnAction.setOnClickListener(v -> {
            if (!isOtpSent) {
                requestOtp();
            } else {
                resetPassword();
            }
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void requestOtp() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }
        tilEmail.setError(null);

        loadingDialog.setMessage("Đang gửi OTP...");
        loadingDialog.show();

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        apiServices.forgotPassword(body).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    isOtpSent = true;
                    tvDescription.setText("Nhập mã OTP đã nhận và mật khẩu mới");
                    layoutResetFields.setVisibility(View.VISIBLE);
                    btnAction.setText("Xác nhận đổi mật khẩu");
                    tilEmail.setEnabled(false); // Lock email
                    Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Không thể gửi OTP.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String otp = etOtp.getText() != null ? etOtp.getText().toString().trim() : "";
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";

        boolean hasError = false;

        if (otp.isEmpty()) {
            tilOtp.setError("Vui lòng nhập mã OTP");
            hasError = true;
        } else {
            tilOtp.setError(null);
        }

        if (newPassword.isEmpty()) {
            tilNewPassword.setError("Vui lòng nhập mật khẩu mới");
            hasError = true;
        } else if (newPassword.length() < 6) {
            tilNewPassword.setError("Mật khẩu phải từ 6 ký tự");
            hasError = true;
        } else {
            tilNewPassword.setError(null);
        }

        if (hasError) return;

        loadingDialog.setMessage("Đang đặt lại mật khẩu...");
        loadingDialog.show();

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);
        body.put("newPassword", newPassword);

        apiServices.verifyResetOtp(body).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    Toast.makeText(ForgotPasswordActivity.this, "Đổi mật khẩu thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Đặt lại mật khẩu thất bại.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
