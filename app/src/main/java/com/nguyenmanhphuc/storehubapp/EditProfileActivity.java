package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class EditProfileActivity extends BaseActivity {

    private EditText edtProfileName, edtProfileEmail, edtProfilePhone, edtProfileAddress;
    private TextInputLayout tilProfileName, tilProfilePhone, tilProfileAddress;
    private ImageView imgLargeAvatar;
    private MaterialButton btnSaveChanges, btnDeleteAccount;
    private SharedPreferencesManager sharedPreferencesManager;
    private User currentUser;
    private Uri croppedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    final Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        croppedImageUri = resultUri;
                        imgLargeAvatar.setImageURI(croppedImageUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(result.getData());
                    if (cropError != null) {
                        Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferencesManager = new SharedPreferencesManager(this);
        currentUser = sharedPreferencesManager.getUser();

        if (currentUser == null) {
            Toast.makeText(this, this.getString(R.string.toast_vui_long_dang_nhap), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUi();
        bindUserData();
        setupClickListeners();
    }

    private void initUi() {
        edtProfileName = findViewById(R.id.edtProfileName);
        edtProfileEmail = findViewById(R.id.edtProfileEmail);
        edtProfilePhone = findViewById(R.id.edtProfilePhone);
        edtProfileAddress = findViewById(R.id.edtProfileAddress);

        tilProfileName = findViewById(R.id.tilProfileName);
        tilProfilePhone = findViewById(R.id.tilProfilePhone);
        tilProfileAddress = findViewById(R.id.tilProfileAddress);

        imgLargeAvatar = findViewById(R.id.imgLargeAvatar);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        if (currentUser != null && currentUser.isSuperAdmin()) {
            btnDeleteAccount.setVisibility(View.GONE);
        } else {
            btnDeleteAccount.setVisibility(View.VISIBLE);
        }
    }

    private void bindUserData() {
        edtProfileName.setText(currentUser.getName());
        edtProfileEmail.setText(currentUser.getEmail());
        edtProfilePhone.setText(currentUser.getPhone());
        edtProfileAddress.setText(currentUser.getAddress());

        if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getImage())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .thumbnail(Glide.with(this).load(currentUser.getImage()).override(10))
                    .into(imgLargeAvatar);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnChangeAvatarPhoto).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "avatar_" + UUID.randomUUID().toString() + ".jpg";
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setToolbarTitle(getString(R.string.crop_avatar_title));
        options.setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.dark_green));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));

        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        Intent cropIntent = UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .getIntent(this);

        cropResultLauncher.launch(cropIntent);
    }

    private void saveProfileChanges() {
        String name = edtProfileName != null && edtProfileName.getText() != null ? edtProfileName.getText().toString().trim() : "";
        String phone = edtProfilePhone != null && edtProfilePhone.getText() != null ? edtProfilePhone.getText().toString().trim() : "";
        String address = edtProfileAddress != null && edtProfileAddress.getText() != null ? edtProfileAddress.getText().toString().trim() : "";

        if (tilProfileName != null) tilProfileName.setError(null);
        if (tilProfilePhone != null) tilProfilePhone.setError(null);
        if (tilProfileAddress != null) tilProfileAddress.setError(null);

        if (name.isEmpty()) {
            if (tilProfileName != null) {
                tilProfileName.setError(getString(R.string.toast_vui_long_nhap_ho_va_ten));
                tilProfileName.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_nhap_ho_va_ten), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (phone.isEmpty()) {
            if (tilProfilePhone != null) {
                tilProfilePhone.setError(getString(R.string.toast_vui_long_nhap_so_dien_thoai));
                tilProfilePhone.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_nhap_so_dien_thoai), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!phone.matches("^[0-9]{10,11}$")) {
            if (tilProfilePhone != null) {
                tilProfilePhone.setError(getString(R.string.toast_so_dien_thoai_phai_tu_10_11_chu_so));
                tilProfilePhone.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_so_dien_thoai_phai_tu_10_11_chu_so), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (address.isEmpty()) {
            if (tilProfileAddress != null) {
                tilProfileAddress.setError(getString(R.string.toast_vui_long_nhap_dia_chi));
                tilProfileAddress.requestFocus();
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_nhap_dia_chi), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        LoadingDialogHelper loadingDialog = new LoadingDialogHelper(this);
        loadingDialog.setMessage(getString(R.string.saving_changes_loading));
        loadingDialog.show();

        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();
        HttpResquest httpResquest = new HttpResquest();
        Call<Response<User>> call;

        if (croppedImageUri != null) {
            // Multipart update
            File file = new File(croppedImageUri.getPath());

            // Senior Tip: Sử dụng "image/jpeg" thay vì "image/*" để tránh lỗi mime-type trên Server
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            // Gửi dữ liệu text đơn giản
            RequestBody rbName = RequestBody.create(MediaType.parse("multipart/form-data"), name);
            RequestBody rbPhone = RequestBody.create(MediaType.parse("multipart/form-data"), phone);
            RequestBody rbAddress = RequestBody.create(MediaType.parse("multipart/form-data"), address);

            call = httpResquest.callAPI().updateProfileMultipart(tokenHeader, rbName, rbPhone, rbAddress, imagePart);
        } else {
            // Standard update
            Map<String, String> body = new HashMap<>();
            body.put("name", name);
            body.put("phone", phone);
            body.put("address", address);
            call = httpResquest.callAPI().updateProfile(tokenHeader, body);
        }

        call.enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(@NonNull Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Response<User> res = response.body();
                    if (res.getCode() == 200 && res.getData() != null) {
                        sharedPreferencesManager.updateUser(res.getData());
                        Toast.makeText(EditProfileActivity.this, EditProfileActivity.this.getString(R.string.toast_cap_nhat_thong_tin_thanh_cong), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, EditProfileActivity.this.getString(R.string.toast_khong_the_cap_nhat_thong_tin_vui_long_th), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<User>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, EditProfileActivity.this.getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete_account, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputLayout tilDeletePassword = dialogView.findViewById(R.id.tilDeletePassword);
        TextInputEditText edtDeletePassword = dialogView.findViewById(R.id.edtDeletePassword);
        MaterialButton btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);
        MaterialButton btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);

        if (btnConfirmDelete != null) {
            btnConfirmDelete.setOnClickListener(v -> {
                String password = edtDeletePassword != null && edtDeletePassword.getText() != null 
                        ? edtDeletePassword.getText().toString().trim() : "";
                
                if (password.isEmpty()) {
                    if (tilDeletePassword != null) {
                        tilDeletePassword.setError(getString(R.string.error_empty_password));
                    } else {
                        Toast.makeText(this, getString(R.string.error_empty_password), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (tilDeletePassword != null) {
                    tilDeletePassword.setError(null);
                }

                dialog.dismiss();
                performDeleteAccount(password);
            });
        }

        if (btnCancelDelete != null) {
            btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void performDeleteAccount(String password) {
        LoadingDialogHelper loadingDialog = new LoadingDialogHelper(this);
        loadingDialog.setMessage(getString(R.string.deleting_profile));
        loadingDialog.show();

        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();
        Map<String, String> body = new HashMap<>();
        body.put("password", password);

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().deleteMe(tokenHeader, body).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    sharedPreferencesManager.logout();
                    MainActivity.preloadedProducts = null;
                    MainActivity.preloadedNews = null;

                    Toast.makeText(EditProfileActivity.this, getString(R.string.delete_account_success_toast), Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = getString(R.string.delete_account_failed_toast);
                    try {
                        if (response.errorBody() != null) {
                            String errStr = response.errorBody().string();
                            JsonObject errObj = new Gson().fromJson(errStr, JsonObject.class);
                            if (errObj.has("message")) {
                                errorMsg = errObj.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
