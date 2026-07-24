package com.example.storehub;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;

public class EditProfileActivity extends BaseActivity {

    private EditText edtProfileName;
    private EditText edtProfileEmail;
    private EditText edtProfilePhone;
    private EditText edtProfileAddress;
    
    private ImageView imgSmallAvatar;
    private ImageView imgLargeAvatar;
    private MaterialButton btnSaveChanges;
    
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
                        imgSmallAvatar.setImageURI(croppedImageUri);
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
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
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
            imagePickerLauncher.launch("image/*");
        });

        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "avatar_" + UUID.randomUUID().toString() + ".jpg";
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setToolbarTitle("Cắt ảnh đại diện");
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
        String name = edtProfileName.getText().toString().trim();
        String phone = edtProfilePhone.getText().toString().trim();
        String address = edtProfileAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String tokenHeader = "Bearer " + sharedPreferencesManager.getToken();
        HttpResquest httpResquest = new HttpResquest();
        Call<Response<User>> call;

        if (croppedImageUri != null) {
            // Multipart update
            File file = new File(croppedImageUri.getPath());
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            RequestBody rbName = RequestBody.create(MediaType.parse("text/plain"), name);
            RequestBody rbPhone = RequestBody.create(MediaType.parse("text/plain"), phone);
            RequestBody rbAddress = RequestBody.create(MediaType.parse("text/plain"), address);

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
            public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<User> res = response.body();
                    if (res.getCode() == 200 && res.getData() != null) {
                        sharedPreferencesManager.updateUser(res.getData());
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
