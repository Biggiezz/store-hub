package com.nguyenmanhphuc.storehubapp.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nguyenmanhphuc.storehubapp.MainActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.NewsAdapter;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsFragment extends Fragment {
    private static final int LIMIT = 5;

    private RecyclerView rvAllNews;
    private View btnBackNews;
    private TextView btnPrevNewsPage, btnNewsPage1, btnNewsPage2, btnNewsPage3, btnNextNewsPage;
    private NewsAdapter newsAdapter;
    private Call<Response<ArrayList<News>>> currentCall;
    private int loadGeneration;
    private int currentPage = 1;
    private ProgressBar progressBarNews;
    private View llPaginationNews;
    private final Map<Integer, ArrayList<News>> pageCache = new HashMap<>();
    private final List<Call<Response<ArrayList<News>>>> prefetchCalls = new ArrayList<>();

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
        progressBarNews = view.findViewById(R.id.progressBarNews);
        llPaginationNews = view.findViewById(R.id.llPaginationNews);
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

        updatePaginationUi(page);

        if (pageCache.containsKey(page)) {
            // Có cache → hiển thị ngay, không loading, không gọi API
            showNewsPage(pageCache.get(page), loadGeneration);
        } else {
            // Chưa có cache → gọi API bình thường
            loadNews(page);
        }

        // Luôn prefetch ngầm trang liền kề
        prefetchAdjacentPages(page);
    }

    private void prefetchAdjacentPages(int currentPage) {
        int[] pagesToPrefetch = {currentPage - 1, currentPage + 1};

        for (int targetPage : pagesToPrefetch) {
            if (targetPage < 1 || pageCache.containsKey(targetPage)) continue;

            Call<Response<ArrayList<News>>> call =
                    new HttpResquest().callAPI().getListNews(targetPage, LIMIT, "published");
            prefetchCalls.add(call);

            final int page = targetPage;
            call.enqueue(new Callback<Response<ArrayList<News>>>() {
                @Override
                public void onResponse(@NonNull Call<Response<ArrayList<News>>> call,
                                       @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                    if (call.isCanceled()) return;
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getCode() == 200
                            && response.body().getData() != null) {
                        // Lưu vào cache ngầm, không hiển thị gì lên UI
                        pageCache.put(page, response.body().getData());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Response<ArrayList<News>>> call,
                                      @NonNull Throwable t) {
                    // Im lặng — prefetch thất bại không ảnh hưởng gì đến UX
                }
            });
        }
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
        if (progressBarNews != null) progressBarNews.setVisibility(View.VISIBLE);
        if (rvAllNews != null) rvAllNews.setVisibility(View.GONE);
        if (llPaginationNews != null) llPaginationNews.setVisibility(View.GONE);

        if (currentCall != null) currentCall.cancel();
        int requestGeneration = ++loadGeneration;
        currentCall = new HttpResquest().callAPI().getListNews(page, LIMIT, "published");
        currentCall.enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (call.isCanceled() || newsAdapter == null || requestGeneration != loadGeneration) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<News> news = response.body().getData();
                    // Lưu vào cache sau khi nhận được data từ API
                    pageCache.put(page, news);
                    preloadNewsImages(news, () -> showNewsPage(news, requestGeneration));
                } else {
                    showLoadFailure(requestGeneration, "Không thể tải danh sách tin tức", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                showLoadFailure(requestGeneration, "Lỗi tải tin tức", t);
            }
        });
    }

    private void preloadNewsImages(ArrayList<News> news, Runnable onComplete) {
        if (news.isEmpty()) {
            onComplete.run();
            return;
        }

        int[] remaining = {news.size()};
        for (News item : news) {
            Glide.with(this)
                    .load(item.getImage())
                    .placeholder(R.drawable.ic_new)
                    .error(R.drawable.ic_new)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            finishPreload();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            finishPreload();
                            return false;
                        }

                        private void finishPreload() {
                            if (--remaining[0] == 0) onComplete.run();
                        }
                    })
                    .preload();
        }
    }

    private void showNewsPage(ArrayList<News> news, int requestGeneration) {
        if (!isAdded() || newsAdapter == null || requestGeneration != loadGeneration) return;
        newsAdapter.updateData(news);
        if (progressBarNews != null) progressBarNews.setVisibility(View.GONE);
        if (rvAllNews != null) {
            rvAllNews.setVisibility(View.VISIBLE);
            rvAllNews.setAlpha(1.0f);
            rvAllNews.scrollToPosition(0);
        }
        if (llPaginationNews != null) llPaginationNews.setVisibility(View.VISIBLE);
    }

    private void showLoadFailure(int requestGeneration, String message, Throwable error) {
        if (!isAdded() || requestGeneration != loadGeneration) return;
        if (progressBarNews != null) progressBarNews.setVisibility(View.GONE);
        if (rvAllNews != null) rvAllNews.setVisibility(View.VISIBLE);
        if (llPaginationNews != null) llPaginationNews.setVisibility(View.VISIBLE);
        Log.e("NewsFragment", message, error);
        if (error != null) Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) currentCall.cancel();
        // Hủy tất cả prefetch calls đang chạy ngầm
        for (Call<Response<ArrayList<News>>> call : prefetchCalls) call.cancel();
        prefetchCalls.clear();
        // Giải phóng bộ nhớ cache khi thoát khỏi màn hình
        pageCache.clear();
        newsAdapter = null;
        super.onDestroyView();
    }
}
