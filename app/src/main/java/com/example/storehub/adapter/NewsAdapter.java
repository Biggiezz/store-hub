package com.example.storehub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.NewsDetailActivity;
import com.example.storehub.R;
import com.example.storehub.model.News;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter hiển thị danh sách tin tức với 2 loại giao diện (Multi-type):
 * 1. Bài viết đầu tiên (Vị trí 0): Thẻ lớn phóng to, ảnh rộng, tóm tắt nội dung dài.
 * 2. Các bài viết tiếp theo: Dạng dòng nằm ngang, ảnh vuông nhỏ bên trái, text bên phải.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private static final int TYPE_FEATURED = 0;  // Kiểu hiển thị nổi bật thẻ lớn
    private static final int TYPE_STANDARD = 1;  // Kiểu hiển thị dòng ngang tiêu chuẩn

    private final Context context;
    private ArrayList<News> listNews;
    private boolean isMultiTypeEnabled = true; // Mặc định bật chế độ hiển thị hỗn hợp (phóng to tin đầu)

    public NewsAdapter(Context context) {
        this.context = context;
        this.listNews = new ArrayList<>();
    }

    /**
     * Bật hoặc tắt chế độ hiển thị hỗn hợp (Dùng để tắt chế độ thẻ to ở màn hình Trang chủ)
     */
    public void setMultiTypeEnabled(boolean enabled) {
        this.isMultiTypeEnabled = enabled;
    }

    /**
     * Cập nhật danh sách tin tức mới và vẽ lại giao diện.
     */
    public void updateData(ArrayList<News> list) {
        this.listNews = list;
        notifyDataSetChanged();
    }

    /**
     * Nối thêm danh sách bài viết mới vào cuối danh sách hiện có (phục vụ phân trang cuộn vô hạn).
     */
    public void addData(ArrayList<News> newNews) {
        if (newNews == null || newNews.isEmpty()) return;
        int oldSize = this.listNews.size();
        this.listNews.addAll(newNews);
        notifyItemRangeInserted(oldSize, newNews.size());
    }

    @Override
    public int getItemViewType(int position) {
        // Nếu bật chế độ hỗn hợp và là phần tử đầu tiên -> dùng thẻ lớn nổi bật
        if (isMultiTypeEnabled && position == 0) {
            return TYPE_FEATURED;
        }
        // Các trường hợp khác hoặc khi tắt chế độ hỗn hợp -> dùng dòng ngang thông thường
        return TYPE_STANDARD;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FEATURED) {
            // Nạp layout thẻ to nổi bật
            view = LayoutInflater.from(context).inflate(R.layout.item_news_featured, parent, false);
        } else {
            // Nạp layout dòng ngang thông thường
            view = LayoutInflater.from(context).inflate(R.layout.item_news_standard, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = listNews.get(position);
        
        // Thiết lập tiêu đề bài viết
        holder.tvNewsTitle.setText(news.getTitle());

        // Lấy đoạn tóm tắt ngắn từ nội dung gốc của bài viết (cắt khoảng 120 ký tự đầu)
        String summary = news.getContent();
        if (summary != null && summary.length() > 120) {
            summary = summary.substring(0, 120) + "...";
        }
        holder.tvNewsDesc.setText(summary);

        // Định dạng thời gian từ MongoDB ISO String sang định dạng trực quan (ví dụ: dd/MM/yyyy HH:mm)
        String formattedDate = formatDateString(news.getCreatedAt());
        holder.tvNewsTime.setText(formattedDate);

        // Tải hình ảnh bài viết bằng Glide
        if (holder.ivNewsImage != null) {
            Glide.with(context)
                    .load(news.getImage())
                    .placeholder(R.drawable.ic_new) // Ảnh mặc định trong khi tải
                    .error(R.drawable.ic_new)       // Ảnh khi tải lỗi
                    .into(holder.ivNewsImage);
        }

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
        ImageView ivNewsImage;
        TextView tvNewsTitle;
        TextView tvNewsDesc;
        TextView tvNewsTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view có cùng ID trong cả 2 file layout (item_news_featured và item_news_standard)
            ivNewsImage = itemView.findViewById(R.id.ivNewsImage);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsDesc = itemView.findViewById(R.id.tvNewsDesc);
            tvNewsTime = itemView.findViewById(R.id.tvNewsTime);
        }
    }
}
