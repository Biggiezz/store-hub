package com.example.storehub;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.services.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtCurrentPassword;
    private EditText edtNewPassword;
    private EditText edtConfirmNewPassword;

    private ImageView btnToggleCurrentPassword;
    private ImageView btnToggleNewPassword;
    private ImageView btnToggleConfirmNewPassword;
    private MaterialButton btnUpdatePassword;

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);

        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmNewPassword = findViewById(R.id.btnToggleConfirmNewPassword);
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Toggle Visibility Listeners
        btnToggleCurrentPassword.setOnClickListener(v -> {
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
            togglePasswordVisibility(edtCurrentPassword, btnToggleCurrentPassword, isCurrentPasswordVisible);
        });

        btnToggleNewPassword.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            togglePasswordVisibility(edtNewPassword, btnToggleNewPassword, isNewPasswordVisible);
        });

        btnToggleConfirmNewPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(edtConfirmNewPassword, btnToggleConfirmNewPassword, isConfirmPasswordVisible);
        });

        btnUpdatePassword.setOnClickListener(v -> handlePasswordUpdate());
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        }
        // Move selection to the end
        editText.setSelection(editText.getText().length());
    }

    private void handlePasswordUpdate() {
        String oldPass = edtCurrentPassword.getText().toString();
        String newPass = edtNewPassword.getText().toString();
        String confirmPass = edtConfirmNewPassword.getText().toString();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate criteria: minimum 8 characters, containing both letters and numbers
        if (newPass.length() < 8 || !newPass.matches(".*[a-zA-Z].*") || !newPass.matches(".*\\d.*")) {
            Toast.makeText(this, "Mật khẩu mới phải tối thiểu 8 ký tự, bao gồm cả chữ cái và số", Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPass);
        body.put("newPassword", newPass);

        String tokenHeader = "Bearer " + sessionManager.getToken();

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().changePassword(tokenHeader, body).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Void> res = response.body();
                    if (res.getCode() == 200) {
                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Update change password date locally to now in ISO format
                        User user = sessionManager.getUser();
                        if (user != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            user.setChangePasswordDate(sdf.format(new Date()));
                            sessionManager.updateUser(user);
                        }
                        
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
