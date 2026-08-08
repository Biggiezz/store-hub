package com.nguyenmanhphuc.storehubapp.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;

public class AddNewsActivity extends AppCompatActivity {

    private ImageView btnBack, ivPreview;
    private RelativeLayout btnSelectImage;
    private View llPlaceholder;
    private EditText etTitle, etContent;
    private Spinner spStatus;
    private TextView tvAuthor;
    private View btnCancel, btnSave;

    private Uri imageUri;
    private SharedPreferencesManager prefManager;
    private ApiServices apiServices;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivPreview.setImageURI(imageUri);
                    ivPreview.setVisibility(View.VISIBLE);
                    llPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_news_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivPreview = findViewById(R.id.ivPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        llPlaceholder = findViewById(R.id.llPlaceholder);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        spStatus = findViewById(R.id.spStatus);
        tvAuthor = findViewById(R.id.tvAuthor);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupData() {
        prefManager = new SharedPreferencesManager(this);
        apiServices = new HttpResquest().callAPI();

        // Setup Author
        User currentUser = prefManager.getUser();
        if (currentUser != null) {
            tvAuthor.setText(currentUser.getName());
        }

        // Setup Status Spinner
        String[] statuses = {"Bản nháp", "Đã xuất bản"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStatus.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveNews());
    }

    private void saveNews() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String status = spStatus.getSelectedItem().toString();
        String author = tvAuthor.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, this.getString(R.string.toast_vui_long_nhap_day_du_thong_tin), Toast.LENGTH_SHORT).show();
            return;
        }

        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setStatus(status);
        news.setAuthor(author);
        // In a real app, you'd upload the image first and get a URL
        news.setImage(imageUri != null ? imageUri.toString() : "");

        apiServices.addNews(news).enqueue(new Callback<Response<News>>() {
            @Override
            public void onResponse(@NonNull Call<Response<News>> call, @NonNull retrofit2.Response<Response<News>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddNewsActivity.this, AddNewsActivity.this.getString(R.string.toast_luu_bai_viet_thanh_cong), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddNewsActivity.this, AddNewsActivity.this.getString(R.string.toast_loi_khi_luu_bai_viet), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<News>> call, @NonNull Throwable t) {
                Toast.makeText(AddNewsActivity.this, AddNewsActivity.this.getString(R.string.toast_loi_ket_noi), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
