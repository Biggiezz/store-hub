package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.storehub.model.News;

import com.example.storehub.utils.DateTimeUtils;

/**
 * Activity displaying the detailed view of a News Article.
 * Receives the News object via intent extra.
 */
public class NewsDetailActivity extends AppCompatActivity {

    private ImageView btnBack, ivDetailNewsImage;
    private TextView tvDetailNewsTitle, tvDetailNewsAuthor, tvDetailNewsTime, tvDetailNewsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        initUi();
        setUpListener();

        // Lấy dữ liệu đối tượng News được truyền từ Adapter
        News news = (News) getIntent().getSerializableExtra("news_item");

        if (news != null) {
            displayNewsDetails(news);
        } else {
            Toast.makeText(this, "Không thể tải chi tiết bài viết!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);
        ivDetailNewsImage = findViewById(R.id.ivDetailNewsImage);
        tvDetailNewsTitle = findViewById(R.id.tvDetailNewsTitle);
        tvDetailNewsAuthor = findViewById(R.id.tvDetailNewsAuthor);
        tvDetailNewsTime = findViewById(R.id.tvDetailNewsTime);
        tvDetailNewsContent = findViewById(R.id.tvDetailNewsContent);
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupBottomNavigation();
        setupInteractionButtons();
    }

    /**
     * Hiển thị chi tiết tin tức lên các view tương ứng
     */
    private void displayNewsDetails(News news) {
        tvDetailNewsTitle.setText(news.getTitle());
        tvDetailNewsAuthor.setText("Tác giả: " + (news.getAuthor() != null ? news.getAuthor() : "Admin"));
        tvDetailNewsContent.setText(news.getContent());

        // Định dạng thời gian hiển thị trực quan
        String formattedDate = formatDateString(news.getCreatedAt());
        tvDetailNewsTime.setText("Đăng lúc: " + formattedDate);

        // Sử dụng Glide để tải hình ảnh từ URL server vào ImageView
        Glide.with(this)
                .load(news.getImage())
                .placeholder(R.drawable.ic_new)
                .error(R.drawable.ic_new)
                .into(ivDetailNewsImage);
    }

    /**
     * Chuyển đổi chuỗi ISO Date từ Server sang định dạng dd/MM/yyyy HH:mm
     */
    private String formatDateString(String isoDateString) {
        return DateTimeUtils.formatISOToLocal(isoDateString, "dd/MM/yyyy HH:mm");
    }

    /**
     * Cấu hình điều hướng cho thanh Bottom Navigation ở góc dưới màn hình chi tiết
     */
    private void setupBottomNavigation() {
        findViewById(R.id.btnHome).setOnClickListener(v -> openMainTab(MainActivity.TAB_HOME));

        findViewById(R.id.btnProducts).setOnClickListener(v -> openMainTab(MainActivity.TAB_PRODUCTS));

        findViewById(R.id.btnCart).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Giỏ hàng đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnNews).setOnClickListener(v -> openMainTab(MainActivity.TAB_NEWS));
    }

    private void openMainTab(String tab) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_OPEN_TAB, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Cấu hình sự kiện cho cụm nút tương tác Chia sẻ / Lưu / Thích dưới cùng
     */
    private void setupInteractionButtons() {
        findViewById(R.id.btnShare).setOnClickListener(v -> Toast.makeText(this, "Đã sao chép liên kết bài viết!", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnBookmark).setOnClickListener(v -> Toast.makeText(this, "Đã lưu bài viết vào mục đọc sau!", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnLike).setOnClickListener(v -> Toast.makeText(this, "Đã thêm bài viết vào danh sách yêu thích!", Toast.LENGTH_SHORT).show());
    }
}
