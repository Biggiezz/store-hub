package com.example.storehub;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.storehub.model.News;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Activity displaying the detailed view of a News Article.
 * Receives the News object via intent extra.
 */
public class NewsDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView ivDetailNewsImage;
    private TextView tvDetailNewsTitle;
    private TextView tvDetailNewsAuthor;
    private TextView tvDetailNewsTime;
    private TextView tvDetailNewsContent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        // Ánh xạ các thành phần giao diện
        btnBack = findViewById(R.id.btnBack);
        ivDetailNewsImage = findViewById(R.id.ivDetailNewsImage);
        tvDetailNewsTitle = findViewById(R.id.tvDetailNewsTitle);
        tvDetailNewsAuthor = findViewById(R.id.tvDetailNewsAuthor);
        tvDetailNewsTime = findViewById(R.id.tvDetailNewsTime);
        tvDetailNewsContent = findViewById(R.id.tvDetailNewsContent);

        // Bắt sự kiện nút Quay lại (Back)
        btnBack.setOnClickListener(v -> finish());

        // Lấy dữ liệu đối tượng News được truyền từ Adapter
        News news = (News) getIntent().getSerializableExtra("news_item");

        if (news != null) {
            displayNewsDetails(news);
        } else {
            Toast.makeText(this, "Không thể tải chi tiết bài viết!", Toast.LENGTH_SHORT).show();
            finish();
        }
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
                .placeholder(R.drawable.ic_new) // Ảnh mặc định khi đang tải
                .error(R.drawable.ic_new)       // Ảnh khi tải lỗi
                .into(ivDetailNewsImage);
    }

    /**
     * Chuyển đổi chuỗi ISO Date từ Server sang định dạng dd/MM/yyyy HH:mm
     */
    private String formatDateString(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "";
        }
        try {
            // Định dạng chuỗi gốc từ MongoDB (ví dụ: 2026-07-18T01:51:40.000Z)
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = parser.parse(isoDateString);

            // Định dạng hiển thị mong muốn
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getDefault());
            
            return formatter.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = parser.parse(isoDateString);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                formatter.setTimeZone(TimeZone.getDefault());
                return formatter.format(date);
            } catch (Exception ex) {
                return isoDateString;
            }
        }
    }
}
