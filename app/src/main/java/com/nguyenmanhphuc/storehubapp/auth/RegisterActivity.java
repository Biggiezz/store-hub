package com.nguyenmanhphuc.storehubapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenmanhphuc.storehubapp.BaseActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.request.RegisterRequest;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper;

import retrofit2.Call;
import retrofit2.Callback;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText edtFullName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword, tilConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginNow;

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

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLoginNow);
    }

    private void setUpListener() {
        if (tvLoginNow != null) {
            tvLoginNow.setOnClickListener(v -> finish());
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> handleRegister());
        }

        if (edtFullName != null) {
            edtFullName.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilFullName != null) tilFullName.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        if (edtEmail != null) {
            edtEmail.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilEmail != null) tilEmail.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        if (edtPhone != null) {
            edtPhone.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilPhone != null) tilPhone.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        if (edtPassword != null) {
            edtPassword.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilPassword != null) tilPassword.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
        if (edtConfirmPassword != null) {
            edtConfirmPassword.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tilConfirmPassword != null) tilConfirmPassword.setError(null);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                }
            });
        }
    }

    private void handleRegister() {
        String name = edtFullName != null && edtFullName.getText() != null ? edtFullName.getText().toString().trim() : "";
        String email = edtEmail != null && edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String phone = edtPhone != null && edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
        String password = edtPassword != null && edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
        String confirmPassword = edtConfirmPassword != null && edtConfirmPassword.getText() != null ? edtConfirmPassword.getText().toString().trim() : "";

        if (tilFullName != null) tilFullName.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);

        boolean hasError = false;
        android.view.View firstErrorView = null;

        if (name.isEmpty()) {
            if (tilFullName != null) {
                tilFullName.setError("Vui lòng nhập họ và tên");
                if (firstErrorView == null) firstErrorView = tilFullName;
            }
            hasError = true;
        }

        if (email.isEmpty()) {
            if (tilEmail != null) {
                tilEmail.setError("Vui lòng nhập Email");
                if (firstErrorView == null) firstErrorView = tilEmail;
            }
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (tilEmail != null) {
                tilEmail.setError("Email không hợp lệ");
                if (firstErrorView == null) firstErrorView = tilEmail;
            }
            hasError = true;
        }

        if (phone.isEmpty()) {
            if (tilPhone != null) {
                tilPhone.setError("Vui lòng nhập số điện thoại");
                if (firstErrorView == null) firstErrorView = tilPhone;
            }
            hasError = true;
        } else if (!phone.matches("^[0-9]{10,11}$")) {
            if (tilPhone != null) {
                tilPhone.setError("Số điện thoại phải từ 10-11 chữ số");
                if (firstErrorView == null) firstErrorView = tilPhone;
            }
            hasError = true;
        }

        if (password.isEmpty()) {
            if (tilPassword != null) {
                tilPassword.setError("Vui lòng nhập mật khẩu");
                if (firstErrorView == null) firstErrorView = tilPassword;
            }
            hasError = true;
        } else if (password.length() < 6) {
            if (tilPassword != null) {
                tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                if (firstErrorView == null) firstErrorView = tilPassword;
            }
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            if (tilConfirmPassword != null) {
                tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
                if (firstErrorView == null) firstErrorView = tilConfirmPassword;
            }
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            if (tilConfirmPassword != null) {
                tilConfirmPassword.setError("Mật khẩu nhập lại không khớp");
                if (firstErrorView == null) firstErrorView = tilConfirmPassword;
            }
            hasError = true;
        }

        if (hasError) {
            if (firstErrorView != null) {
                firstErrorView.requestFocus();
            }
            return;
        }

        LoadingDialogHelper loadingDialog = new LoadingDialogHelper(this);
        loadingDialog.setMessage("Đang đăng ký tài khoản...");
        loadingDialog.show();

        // Tạo đối tượng Request gửi đi
        RegisterRequest request = new RegisterRequest(name, email, phone, password);

        // Gọi API
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().register(request).enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(@NonNull Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Response<User> apiResponse = response.body();
                    if (apiResponse.getCode() == 201) {
                        Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.toast_dang_ky_thanh_cong), Toast.LENGTH_SHORT).show();
                        // Quay lại màn hình Login
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.toast_dang_ky_that_bai_hoac_email_da_ton_tai), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<User>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
