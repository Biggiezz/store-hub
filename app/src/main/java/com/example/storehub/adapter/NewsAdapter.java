package com.example.storehub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.NewsDetailActivity;
import com.example.storehub.R;
import com.example.storehub.model.News;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * RecyclerView Adapter for binding News data to item_new.xml layout.
 * Handles the click event to open NewsDetailActivity.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<News> listNews;

    public NewsAdapter(Context context) {
        this.context = context;
        this.listNews = new ArrayList<>();
    }

    /**
     * Cập nhật danh sách tin tức mới và vẽ lại giao diện.
     */
    public void updateData(ArrayList<News> list) {
        this.listNews = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_new, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = listNews.get(position);
        
        // Thiết lập tiêu đề bài viết
        holder.tvNewsTitle.setText(news.getTitle());

        // Định dạng thời gian từ MongoDB ISO String sang định dạng trực quan dd/MM/yyyy HH:mm
        String formattedDate = formatDateString(news.getCreatedAt());
        holder.tvNewsTime.setText(formattedDate);

        // Xử lý sự kiện click vào phần tử bài viết để mở màn hình chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            // Truyền đối tượng News qua Intent
            intent.putExtra("news_item", news);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listNews.size();
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
            // Hiển thị theo múi giờ địa phương của thiết bị
            formatter.setTimeZone(TimeZone.getDefault());
            
            return formatter.format(date);
        } catch (Exception e) {
            // Nếu parse lỗi, thử parse dạng ISO ngắn hơn hoặc trả về chuỗi gốc
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNewsTitle;
        TextView tvNewsTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsTime = itemView.findViewById(R.id.tvNewsTime);
        }
    }
}
