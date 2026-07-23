package com.example.storehub.admin;

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
import androidx.appcompat.app.AppCompatActivity;

import com.example.storehub.R;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.model.User;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;

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
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
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
            public void onResponse(Call<Response<News>> call, retrofit2.Response<Response<News>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddNewsActivity.this, "Lưu bài viết thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddNewsActivity.this, "Lỗi khi lưu bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<News>> call, Throwable t) {
                Toast.makeText(AddNewsActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
