package com.nguyenmanhphuc.storehubapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import androidx.annotation.NonNull;
import com.nguyenmanhphuc.storehubapp.model.response.Response;

/**
 * Activity displaying the detailed view of a News Article.
 * Receives the News object via intent extra.
 */
public class NewsDetailActivity extends BaseActivity {

    private ImageView btnBack, ivDetailNewsImage, btnBookmark, btnLike, btnShare;
    private TextView tvDetailNewsTitle, tvDetailNewsAuthor, tvDetailNewsTime, tvDetailNewsContent, tvLikeCount;
    private News newsItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.news_detail_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        setUpListener();

        // Lấy dữ liệu đối tượng News được truyền từ Adapter
        News news = (News) getIntent().getSerializableExtra("news_item");
        boolean isAdmin = getIntent().getBooleanExtra("is_admin", false);

        if (isAdmin) {
            View shareSection = findViewById(R.id.layoutShareSection);
            View bottomNav = findViewById(R.id.bottomNavigation);
            if (shareSection != null) shareSection.setVisibility(View.GONE);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }

        if (news != null) {
            this.newsItem = news;
            displayNewsDetails(news);
            setupInteractionButtons();
        } else {
            Toast.makeText(this, this.getString(R.string.toast_khong_the_tai_chi_tiet_bai_viet), Toast.LENGTH_SHORT).show();
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
        btnBookmark = findViewById(R.id.btnBookmark);
        btnLike = findViewById(R.id.btnLike);
        btnShare = findViewById(R.id.btnShare);
        tvLikeCount = findViewById(R.id.tvLikeCount);
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    /**
     * Hiển thị chi tiết tin tức lên các view tương ứng
     */
    private void displayNewsDetails(News news) {
        tvDetailNewsTitle.setText(news.getTitle());
        tvDetailNewsAuthor.setText(getString(R.string.author_prefix, (news.getAuthor() != null ? news.getAuthor() : "Admin")));
        tvDetailNewsContent.setText(news.getContent());
        if (tvLikeCount != null) {
            tvLikeCount.setText(String.valueOf(news.getLikes()));
        }

        // Định dạng thời gian hiển thị trực quan
        String formattedDate = formatDateString(news.getCreatedAt());
        tvDetailNewsTime.setText(getString(R.string.published_at_prefix, formattedDate));

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
        if (newsItem == null) return;

        android.content.SharedPreferences prefs = getSharedPreferences("news_prefs", MODE_PRIVATE);
        String newsId = newsItem.get_id() != null ? newsItem.get_id() : "";

        // Initial state load
        final boolean[] isLiked = {prefs.getBoolean("like_" + newsId, false)};
        final boolean[] isBookmarked = {prefs.getBoolean("bookmark_" + newsId, false)};

        int darkGreen = androidx.core.content.ContextCompat.getColor(this, R.color.dark_green);
        int gold = androidx.core.content.ContextCompat.getColor(this, R.color.rating_gold);
        int red = androidx.core.content.ContextCompat.getColor(this, R.color.error_red);

        if (btnLike != null) {
            btnLike.setColorFilter(isLiked[0] ? red : darkGreen);
            btnLike.setOnClickListener(v -> {
                isLiked[0] = !isLiked[0];
                prefs.edit().putBoolean("like_" + newsId, isLiked[0]).apply();
                btnLike.setColorFilter(isLiked[0] ? red : darkGreen);

                java.util.HashMap<String, String> body = new java.util.HashMap<>();
                body.put("action", isLiked[0] ? "like" : "unlike");

                new com.nguyenmanhphuc.storehubapp.services.HttpResquest().callAPI().likeNews(newsId, body)
                        .enqueue(new retrofit2.Callback<Response<Integer>>() {
                            @Override
                            public void onResponse(@NonNull retrofit2.Call<Response<Integer>> call, @NonNull retrofit2.Response<Response<Integer>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                                    int newLikes = response.body().getData() != null ? response.body().getData() : 0;
                                    newsItem.setLikes(newLikes);
                                    if (tvLikeCount != null) {
                                        tvLikeCount.setText(String.valueOf(newLikes));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull retrofit2.Call<Response<Integer>> call, @NonNull Throwable t) {
                                // Mạng lỗi thì bỏ qua hoặc khôi phục UI local
                            }
                        });
            });
        }

        if (btnBookmark != null) {
            btnBookmark.setColorFilter(isBookmarked[0] ? gold : darkGreen);
            btnBookmark.setOnClickListener(v -> {
                isBookmarked[0] = !isBookmarked[0];
                prefs.edit().putBoolean("bookmark_" + newsId, isBookmarked[0]).apply();
                btnBookmark.setColorFilter(isBookmarked[0] ? gold : darkGreen);
                Toast.makeText(this, isBookmarked[0] ? getString(R.string.toast_bookmark_saved) : "Đã bỏ lưu bài viết!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareUrl = com.nguyenmanhphuc.storehubapp.services.HttpResquest.BASE_URL + "api/newsRouter/share/news/" + newsId;
                String shareBody = newsItem.getTitle() + "\n\nXem chi tiết bài viết tại đây: " + shareUrl + "\n\nStoreHub - Ứng dụng đọc tin tức công nghệ hàng đầu!";
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, newsItem.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ tin tức qua"));
            });
        }
    }
}
