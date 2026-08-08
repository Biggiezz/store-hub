package com.nguyenmanhphuc.storehubapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;

import com.nguyenmanhphuc.storehubapp.NewsDetailActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.PostManagementAdapter;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class PostManagementFragment extends Fragment implements PostManagementAdapter.OnPostActionListener {

    private RecyclerView rvPosts;
    private PostManagementAdapter adapter;
    private FloatingActionButton fabAdd;
    private ApiServices apiServices;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiServices = new HttpResquest().callAPI();
        rvPosts = view.findViewById(R.id.rvPosts);
        fabAdd = view.findViewById(R.id.fabAdd);

        adapter = new PostManagementAdapter(getContext(), this);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddNewsActivity.class));
        });

        loadNews();

        view.findViewById(R.id.btnRefresh).setOnClickListener(v -> loadNews());

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::loadNews);
        }
    }

    private void loadNews() {
        apiServices.getListNews(1, 100).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), getContext().getString(R.string.toast_loi_tai_du_lieu), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDelete(News news) {
        apiServices.deleteNews(news.get_id()).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), getContext().getString(R.string.toast_da_xoa_bai_viet), Toast.LENGTH_SHORT).show();
                    }
                    loadNews();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), getContext().getString(R.string.toast_loi_khi_xoa), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(News news) {
        Intent intent = new Intent(getContext(), NewsDetailActivity.class);
        intent.putExtra("news_item", news);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNews();
    }
}
