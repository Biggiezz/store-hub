package com.nguyenmanhphuc.storehubapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;

import java.util.ArrayList;

public class ReviewMediaAdapter extends RecyclerView.Adapter<ReviewMediaAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> mediaUrls;

    public ReviewMediaAdapter(Context context, ArrayList<String> mediaUrls) {
        this.context = context;
        this.mediaUrls = mediaUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = mediaUrls.get(position);

        // Load media thumbnail using Glide
        Glide.with(context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.ivThumbnail);

        // Check if the URL points to a video
        boolean isVideo = isVideoUrl(url);
        holder.ivPlayIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        // Click to view fullscreen
        holder.itemView.setOnClickListener(v -> showFullscreenMedia(url, isVideo));
    }

    private boolean isVideoUrl(String url) {
        if (url == null) return false;
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains("/video/upload/") ||
                lowerUrl.endsWith(".mp4") ||
                lowerUrl.endsWith(".3gp") ||
                lowerUrl.endsWith(".mkv") ||
                lowerUrl.endsWith(".webm") ||
                lowerUrl.endsWith(".mov");
    }

    private void showFullscreenMedia(String url, boolean isVideo) {
        // Khởi tạo Dialog sử dụng theme FullscreenDialogTheme tùy chỉnh để phủ kín 100% màn hình
        Dialog dialog = new Dialog(context, R.style.FullscreenDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_media);

        ImageView ivFullscreen = dialog.findViewById(R.id.ivFullscreen);
        VideoView vvFullscreen = dialog.findViewById(R.id.vvFullscreen);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        View btnClose = dialog.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        if (isVideo) {
            ivFullscreen.setVisibility(View.GONE);
            vvFullscreen.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            vvFullscreen.setVideoURI(Uri.parse(url));

            vvFullscreen.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                mp.setLooping(true); // Tự động lặp lại video vô hạn
                vvFullscreen.start();
            });

            vvFullscreen.setOnErrorListener((mp, what, extra) -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Không thể tải video này", Toast.LENGTH_SHORT).show();
                return true;
            });

            // Dọn dẹp VideoView khi tắt dialog để tránh rò rỉ bộ nhớ
            dialog.setOnDismissListener(dialogInterface -> {
                try {
                    if (vvFullscreen.isPlaying()) {
                        vvFullscreen.stopPlayback();
                    }
                } catch (Exception e) {
                    Log.e("ReviewMediaAdapter", "Lỗi dừng phát video", e);
                }
            });

        } else {
            ivFullscreen.setVisibility(View.VISIBLE);
            vvFullscreen.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            Glide.with(context)
                    .load(url)
                    .fitCenter()
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(ivFullscreen);
        }

        dialog.show();

        // Thiết lập Window sau khi show() để đảm bảo kích thước MATCH_PARENT không bị Android ghi đè
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK));
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            
            // Ép buộc vẽ tràn viền xuyên qua khu vực Notch (tai thỏ)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                window.getAttributes().layoutInDisplayCutoutMode = 
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            // Tắt tự động căn lề hệ thống để phủ kín toàn bộ màn hình
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false);
        }
    }

    @Override
    public int getItemCount() {
        return mediaUrls != null ? mediaUrls.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageView ivPlayIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
        }
    }
}
