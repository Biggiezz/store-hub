package com.example.storehub;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.NewsAdapter;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsFragment extends Fragment {
    private static final int LIMIT = 5;
    private NewsAdapter newsAdapter;
    private Call<Response<ArrayList<News>>> currentCall;
    private int currentPage = 1;
    private boolean isLoading;
    private boolean isLastPage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvAllNews = view.findViewById(R.id.rvAllNews);
        newsAdapter = new NewsAdapter(requireContext());
        rvAllNews.setAdapter(newsAdapter);
        view.findViewById(R.id.btnBackNews)
                .setOnClickListener(v -> ((MainActivity) requireActivity()).showHome());

        LinearLayoutManager layoutManager = (LinearLayoutManager) rvAllNews.getLayoutManager();
        rvAllNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 || isLoading || isLastPage || layoutManager == null) return;
                if (layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition()
                        >= layoutManager.getItemCount()) {
                    loadNews(++currentPage, true);
                }
            }
        });

        loadNews(1, false);
    }

    private void loadNews(int page, boolean append) {
        isLoading = true;
        currentCall = new HttpResquest().callAPI().getListNews(page, LIMIT);
        currentCall.enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (call.isCanceled() || newsAdapter == null) return;
                isLoading = false;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<News> news = response.body().getData();
                    if (append) newsAdapter.addData(news);
                    else newsAdapter.updateData(news);
                    isLastPage = news.size() < LIMIT;
                } else {
                    Log.e("NewsFragment", "Không thể tải danh sách tin tức");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading = false;
                if (append) currentPage--;
                Log.e("NewsFragment", "Lỗi tải tin tức", t);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) currentCall.cancel();
        newsAdapter = null;
        super.onDestroyView();
    }
}
