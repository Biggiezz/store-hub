package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.NewsAdapter;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Activity hiển thị danh sách toàn bộ các bài viết tin tức.
 * Hỗ trợ công nghệ Phân trang (Pagination) và Tải lười cuộn vô hạn (Infinite Scroll / Lazy Loading).
 */
public class NewsActivity extends AppCompatActivity {

    private RecyclerView rvAllNews;
    private NewsAdapter newsAdapter;

    // Các biến trạng thái phục vụ giải thuật phân trang
    private int currentPage = 1;         // Trang hiện tại
    private final int limit = 5;        // Số lượng tin tức tối đa mỗi trang (Đặt bằng 5 để khớp với trang chủ)
    private boolean isLoading = false;    // Trạng thái đang tải dữ liệu từ API
    private boolean isLastPage = false;   // Trạng thái đã tải hết tất cả các trang tin tức từ Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // Ánh xạ RecyclerView
        rvAllNews = findViewById(R.id.rvAllNews);
        
        // Khởi tạo Adapter
        newsAdapter = new NewsAdapter(this);
        rvAllNews.setAdapter(newsAdapter);

        // Thiết lập sự kiện click cho Bottom Navigation
        setupBottomNavigation();

        // Bắt sự kiện Click cho nút Quay lại (Back Arrow) trên Header
        findViewById(R.id.btnBackNews).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Thiết lập sự kiện lắng nghe cuộn của RecyclerView (Infinite Scroll)
        setupScrollListener();

        // Nhận dữ liệu danh sách tin tức đã tải sẵn từ MainActivity (Trang 1) để hiển thị ngay lập tức
        ArrayList<News> newsList = (ArrayList<News>) getIntent().getSerializableExtra("news_list");
        if (newsList != null && !newsList.isEmpty()) {
            newsAdapter.updateData(newsList);
            // Nếu trang đầu tiên có số lượng ít hơn giới hạn (limit), coi như đã tới trang cuối
            if (newsList.size() < limit) {
                isLastPage = true;
            }
        } else {
            // Tải trang 1 từ API Server nếu không có dữ liệu truyền qua
            fetchNews();
        }
    }

    /**
     * Cấu hình sự kiện cuộn vô hạn (Lazy Loading) khi vuốt danh sách
     */
    private void setupScrollListener() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvAllNews.getLayoutManager();
        if (layoutManager == null) return;

        rvAllNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Kiểm tra xem người dùng đang cuộn xuống (dy > 0)
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();      // Số phần tử đang hiện trên màn hình
                    int totalItemCount = layoutManager.getItemCount();          // Tổng số phần tử trong danh sách hiện tại
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition(); // Vị trí phần tử đầu tiên đang hiển thị

                    // Điều kiện kích hoạt tải thêm trang mới:
                    // 1. Hệ thống không ở trạng thái đang gọi API tải dữ liệu (!isLoading)
                    // 2. Chưa đạt đến trang cuối cùng của Database (!isLastPage)
                    // 3. Người dùng đã cuộn tới phần tử cuối cùng của danh sách
                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            isLoading = true;
                            currentPage++; // Tăng trang lên tiếp theo
                            loadMoreNews(currentPage);
                        }
                    }
                }
            }
        });
    }

    /**
     * Cấu hình điều hướng cho thanh Bottom Navigation
     */
    private void setupBottomNavigation() {
        // Nút "Trang chủ" -> quay lại MainActivity
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Các nút khác (Sản phẩm, Giỏ hàng) -> tạm thời thông báo đang phát triển
        findViewById(R.id.btnProducts).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Sản phẩm đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnCart).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Giỏ hàng đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnNews).setOnClickListener(v -> {

        });
    }

    /**
     * API tải trang đầu tiên (Dành cho trường hợp dự phòng không có dữ liệu truyền qua từ MainActivity)
     */
    private void fetchNews() {
        isLoading = true;
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListNews(1, limit).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<News>>> call, retrofit2.Response<Response<ArrayList<News>>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<News>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        ArrayList<News> newsList = apiResponse.getData();
                        newsAdapter.updateData(newsList);
                        
                        // Nếu trang 1 trả về số lượng bài viết ít hơn giới hạn (limit) -> Đã là trang cuối
                        if (newsList.size() < limit) {
                            isLastPage = true;
                        }
                    } else {
                        Log.e("NewsActivity", "Lỗi dữ liệu trả về từ server: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("NewsActivity", "Không thể lấy tin tức: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e("NewsActivity", "Lỗi kết nối khi tải tin tức", t);
                Toast.makeText(NewsActivity.this, "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * API tải các trang tiếp theo khi người dùng cuộn (Nối thêm dữ liệu vào cuối RecyclerView)
     */
    private void loadMoreNews(int page) {
        // Hiển thị thông báo đang tải nhẹ
        Toast.makeText(this, "Đang tải thêm tin tức...", Toast.LENGTH_SHORT).show();
        
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListNews(page, limit).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<News>>> call, retrofit2.Response<Response<ArrayList<News>>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<News>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        ArrayList<News> newNewsList = apiResponse.getData();
                        
                        if (newNewsList.isEmpty()) {
                            // Nếu trang tiếp theo rỗng -> Đã tải hết bài viết
                            isLastPage = true;
                            Toast.makeText(NewsActivity.this, "Đã hiển thị toàn bộ tin tức!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Nối tiếp bài viết mới vào cuối danh sách hiện tại
                            newsAdapter.addData(newNewsList);
                            
                            // Nếu số bài viết lấy được ít hơn giới hạn -> Đã là trang cuối
                            if (newNewsList.size() < limit) {
                                isLastPage = true;
                                Toast.makeText(NewsActivity.this, "Đã hiển thị toàn bộ tin tức!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e("NewsActivity", "Lỗi kết nối khi tải thêm trang mới", t);
                Toast.makeText(NewsActivity.this, "Không thể tải thêm tin tức!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
