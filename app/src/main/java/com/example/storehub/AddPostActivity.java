package com.example.storehub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storehub.model.Post;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;

public class AddPostActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private Spinner spinnerStatus;
    private TextView txtAuthorName;
    private ImageView btnBack;
    private HttpResquest httpRequest;
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        sharedPreferencesManager = new SharedPreferencesManager(this);
        initUi();
        httpRequest = new HttpResquest();
        setupSpinner();
        setAuthorInfo();
    }

    private void initUi() {
        edtTitle = findViewById(R.id.edtPostTitle);
        edtContent = findViewById(R.id.edtPostContent);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        txtAuthorName = findViewById(R.id.txtAuthorName);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnUploadImage).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng tải ảnh lên đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnAddPost).setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String content = edtContent.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Post post = new Post(title, content, ""); // Link ảnh rỗng cho mock
            post.setAuthor(txtAuthorName.getText().toString());
            // Bạn có thể thêm trường status vào model Post nếu cần

            savePost(post);
        });
    }

    private void setupSpinner() {
        String[] statusOptions = {"Bản nháp", "Đã xuất bản", "Riêng tư"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void setAuthorInfo() {
        if (sharedPreferencesManager.isLoggedIn()) {
            txtAuthorName.setText(sharedPreferencesManager.getUser().getName());
        } else {
            txtAuthorName.setText("Guest");
        }
    }

    private void savePost(Post post) {
        httpRequest.callAPI().addPost(post).enqueue(new Callback<Response<Post>>() {
            @Override
            public void onResponse(Call<Response<Post>> call, retrofit2.Response<Response<Post>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddPostActivity.this, "Lưu bài viết thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddPostActivity.this, "Lỗi khi lưu bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Post>> call, Throwable t) {
                Toast.makeText(AddPostActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
