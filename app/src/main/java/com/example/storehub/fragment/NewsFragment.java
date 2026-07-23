package com.example.storehub.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.MainActivity;
import com.example.storehub.R;
import com.example.storehub.adapter.NewsAdapter;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsFragment extends Fragment {
    private static final int LIMIT = 5;
    private static final int PREFETCH_PAGES_COUNT = 3; // Số lượng trang tải ngầm trước (Tải trước 3 trang tiếp theo)

    private RecyclerView rvAllNews;
    private View btnBackNews;
    private TextView btnPrevNewsPage, btnNewsPage1, btnNewsPage2, btnNewsPage3, btnNextNewsPage;
    private NewsAdapter newsAdapter;
    private Call<Response<ArrayList<News>>> currentCall;
    private int currentPage = 1;
    private boolean isLoading;

    // Bộ nhớ đệm Cache lưu danh sách các trang đã và sắp tải (Trải nghiệm chuyển trang 0.0 giây)
    private final Map<Integer, ArrayList<News>> pageCache = new HashMap<>();
    private boolean isLastPage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);
        setUpAdapter();
        setUpListener();

        goToPage(1);
    }

    private void initUi(View view) {
        rvAllNews = view.findViewById(R.id.rvAllNews);
        btnBackNews = view.findViewById(R.id.btnBackNews);
        btnPrevNewsPage = view.findViewById(R.id.btnPrevNewsPage);
        btnNewsPage1 = view.findViewById(R.id.btnNewsPage1);
        btnNewsPage2 = view.findViewById(R.id.btnNewsPage2);
        btnNewsPage3 = view.findViewById(R.id.btnNewsPage3);
        btnNextNewsPage = view.findViewById(R.id.btnNextNewsPage);
    }

    private void setUpAdapter() {
        newsAdapter = new NewsAdapter(requireContext());
        if (rvAllNews != null) {
            rvAllNews.setAdapter(newsAdapter);
        }
    }

    private void setUpListener() {
        if (btnBackNews != null) {
            btnBackNews.setOnClickListener(v -> ((MainActivity) requireActivity()).showHome());
        }

        btnPrevNewsPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                goToPage(currentPage - 1);
            }
        });

        btnNextNewsPage.setOnClickListener(v -> {
            goToPage(currentPage + 1);
        });

        btnNewsPage1.setOnClickListener(v -> {
            try {
                int pageVal = Integer.parseInt(btnNewsPage1.getText().toString());
                goToPage(pageVal);
            } catch (Exception ignored) {}
        });

        btnNewsPage2.setOnClickListener(v -> {
            try {
                int pageVal = Integer.parseInt(btnNewsPage2.getText().toString());
                goToPage(pageVal);
            } catch (Exception ignored) {}
        });

        btnNewsPage3.setOnClickListener(v -> {
            try {
                int pageVal = Integer.parseInt(btnNewsPage3.getText().toString());
                goToPage(pageVal);
            } catch (Exception ignored) {}
        });
    }

    private void goToPage(int page) {
        if (page < 1) return;
        this.currentPage = page;

        // Phản hồi giao diện thanh phân trang NGAY LẬP TỨC (0.0s delay)
        updatePaginationUi(page);

        // Kiểm tra xem trang này đã được Cache/Pre-fetch trước đó chưa
        if (pageCache.containsKey(page)) {
            ArrayList<News> cachedNews = pageCache.get(page);
            if (cachedNews != null && !cachedNews.isEmpty()) {
                // Hiển thị NGAY TỨC THÌ từ RAM (0ms delay)
                newsAdapter.updateData(cachedNews);
                if (rvAllNews != null) {
                    rvAllNews.setAlpha(1.0f);
                    rvAllNews.scrollToPosition(0);
                }
                // Tải trước ngầm 3 trang tiếp theo phía sau
                prefetchNextPages(page, PREFETCH_PAGES_COUNT);
                return;
            }
        }

        // Nếu chưa có trong Cache -> Làm mờ nhẹ giao diện cũ để người dùng thấy ứng dụng phản hồi ngay
        if (rvAllNews != null) {
            rvAllNews.animate().alpha(0.4f).setDuration(100).start();
        }

        loadNews(page);
    }

    private void updatePaginationUi(int page) {
        int startPage = (page <= 3) ? 1 : (page - 2);

        int p1 = startPage;
        int p2 = startPage + 1;
        int p3 = startPage + 2;

        btnNewsPage1.setText(String.valueOf(p1));
        btnNewsPage2.setText(String.valueOf(p2));
        btnNewsPage3.setText(String.valueOf(p3));

        boolean isP1Active = (page == p1);
        boolean isP2Active = (page == p2);
        boolean isP3Active = (page == p3);

        btnNewsPage1.setBackgroundResource(isP1Active ? R.drawable.bg_pagination_active : R.drawable.bg_pagination_inactive);
        btnNewsPage1.setTextColor(isP1Active ? Color.WHITE : ContextCompat.getColor(requireContext(), R.color.text_primary));

        btnNewsPage2.setBackgroundResource(isP2Active ? R.drawable.bg_pagination_active : R.drawable.bg_pagination_inactive);
        btnNewsPage2.setTextColor(isP2Active ? Color.WHITE : ContextCompat.getColor(requireContext(), R.color.text_primary));

        btnNewsPage3.setBackgroundResource(isP3Active ? R.drawable.bg_pagination_active : R.drawable.bg_pagination_inactive);
        btnNewsPage3.setTextColor(isP3Active ? Color.WHITE : ContextCompat.getColor(requireContext(), R.color.text_primary));

        btnPrevNewsPage.setAlpha(page > 1 ? 1.0f : 0.4f);
    }

    private void loadNews(int page) {
        isLoading = true;
        if (currentCall != null) currentCall.cancel();
        currentCall = new HttpResquest().callAPI().getListNews(page, LIMIT);
        currentCall.enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (call.isCanceled() || newsAdapter == null) return;
                isLoading = false;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<News> news = response.body().getData();

                    // Lưu dữ liệu vào Cache
                    pageCache.put(page, news);

                    newsAdapter.updateData(news);
                    if (rvAllNews != null) {
                        rvAllNews.animate().alpha(1.0f).setDuration(150).start();
                        rvAllNews.scrollToPosition(0);
                    }

                    // Tải trước ngầm 3 trang tiếp theo phía sau
                    prefetchNextPages(page, PREFETCH_PAGES_COUNT);
                } else {
                    if (rvAllNews != null) rvAllNews.setAlpha(1.0f);
                    Log.e("NewsFragment", "Không thể tải danh sách tin tức");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading = false;
                if (rvAllNews != null) rvAllNews.setAlpha(1.0f);
                Log.e("NewsFragment", "Lỗi tải tin tức", t);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Tải trước ngầm (Pre-fetch) N trang tiếp theo phía sau khi người dùng đang ở trang hiện tại.
     * Khi người dùng bấm Next '>', hoặc bấm vào các số trang tiếp theo, dữ liệu đã có sẵn trên RAM nên sẽ hiển thị LẬP TỨC (0.0s delay).
     */
    private void prefetchNextPages(int startPage, int count) {
        for (int i = 1; i <= count; i++) {
            int targetPage = startPage + i;
            if (pageCache.containsKey(targetPage)) continue; // Đã có trong cache

            final int pageToFetch = targetPage;
            new HttpResquest().callAPI().getListNews(pageToFetch, LIMIT).enqueue(new Callback<Response<ArrayList<News>>>() {
                @Override
                public void onResponse(@NonNull Call<Response<ArrayList<News>>> call,
                                       @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getCode() == 200 && response.body().getData() != null) {
                        ArrayList<News> nextNews = response.body().getData();
                        if (!nextNews.isEmpty()) {
                            pageCache.put(pageToFetch, nextNews);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                    // Bỏ qua lỗi tải ngầm phía sau
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) currentCall.cancel();
        newsAdapter = null;
        super.onDestroyView();
    }
}
