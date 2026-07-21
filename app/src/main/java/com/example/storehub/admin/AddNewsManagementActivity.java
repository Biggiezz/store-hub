package com.example.storehub.admin;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storehub.R;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class AddNewsManagementActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Spinner spinnerStatus;
    private TextView txtAuthorName;
    private ImageView btnBack, imgPreview;
    private View layoutUploadPlaceholder;
    private MaterialButton btnAddPost;
    private HttpResquest httpRequest;
    private SharedPreferencesManager sharedPreferencesManager;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                selectedImageUri = uri;
                imgPreview.setImageURI(uri);
                imgPreview.setVisibility(View.VISIBLE);
                layoutUploadPlaceholder.setVisibility(View.GONE);
            }
    );

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_post_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferencesManager = new SharedPreferencesManager(this);
        User user = sharedPreferencesManager.getUser();
        String role = user != null && user.getRole() != null
                ? user.getRole().replace(" ", "").toLowerCase(Locale.ROOT)
                : "";
        if (!"admin".equals(role) && !"superadmin".equals(role)) {
            Toast.makeText(this, "Bạn không có quyền thêm bài viết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        httpRequest = new HttpResquest();

        initUi();
        setUpAdapter();
        setUpListener();
        setAuthorInfo();
    }

    private void initUi() {
        edtTitle = findViewById(R.id.edtPostTitle);
        edtContent = findViewById(R.id.edtPostContent);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        txtAuthorName = findViewById(R.id.txtAuthorName);
        btnBack = findViewById(R.id.btnBack);
        imgPreview = findViewById(R.id.imgPreview);
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder);
        btnAddPost = findViewById(R.id.btnAddPost);
    }

    private void setUpAdapter() {
        String[] statusOptions = {"Đã xuất bản", "Bản nháp", "Riêng tư"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerStatus != null) {
            spinnerStatus.setAdapter(adapter);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnCancel = findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        View btnUploadImage = findViewById(R.id.btnUploadImage);
        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        }

        if (btnAddPost != null) {
            btnAddPost.setOnClickListener(v -> handleAddPost());
        }
    }

    private void setAuthorInfo() {
        if (sharedPreferencesManager != null && sharedPreferencesManager.isLoggedIn() && sharedPreferencesManager.getUser() != null) {
            txtAuthorName.setText(sharedPreferencesManager.getUser().getName());
        } else {
            txtAuthorName.setText("Admin");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void handleAddPost() {
        String title = edtTitle != null && edtTitle.getText() != null ? edtTitle.getText().toString().trim() : "";
        String content = edtContent != null && edtContent.getText() != null ? edtContent.getText().toString().trim() : "";
        String selectedStatus = spinnerStatus != null && spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString() : "Đã xuất bản";

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung bài viết", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện bài viết", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = "published";
        if ("Bản nháp".equals(selectedStatus)) {
            status = "draft";
        } else if ("Riêng tư".equals(selectedStatus)) {
            status = "hidden";
        }

        saveNews(title, content, status);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void saveNews(String title, String content, String status) {
        String token = sharedPreferencesManager != null ? sharedPreferencesManager.getToken() : "";
        String authHeader = "Bearer " + token;

        try (InputStream input = getContentResolver().openInputStream(selectedImageUri)) {
            if (input == null) throw new IOException("Không đọc được ảnh");
            String mimeType = getContentResolver().getType(selectedImageUri);
            if (mimeType == null) mimeType = "image/jpeg";
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension == null) extension = "jpg";

            RequestBody imageBody = RequestBody.create(MediaType.parse(mimeType), input.readAllBytes());
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "news." + extension, imageBody);
            RequestBody textType = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody contentType = RequestBody.create(MediaType.parse("text/plain"), content);
            RequestBody statusType = RequestBody.create(MediaType.parse("text/plain"), status);

            btnAddPost.setEnabled(false);
            httpRequest.callAPI().addAdminNews(authHeader, textType, contentType, statusType, imagePart)
                    .enqueue(createAddNewsCallback());
        } catch (IOException error) {
            Toast.makeText(this, "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private Callback<Response<News>> createAddNewsCallback() {
        return new Callback<Response<News>>() {
            @Override
            public void onResponse(@NonNull Call<Response<News>> call, @NonNull retrofit2.Response<Response<News>> response) {
                btnAddPost.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 201) {
                    Toast.makeText(AddNewsManagementActivity.this, "Tạo bài viết mới thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.body() != null && response.body().getMessage() != null) {
                    Toast.makeText(AddNewsManagementActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddNewsManagementActivity.this, "Lỗi khi tạo bài viết mới", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<News>> call, @NonNull Throwable t) {
                btnAddPost.setEnabled(true);
                Toast.makeText(AddNewsManagementActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}
