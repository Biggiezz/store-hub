package com.example.storehub.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storehub.R;
import com.example.storehub.model.Product;
import com.example.storehub.model.ProductReview;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;

import retrofit2.Call;
import retrofit2.Callback;

public class ReplyReviewActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvProductName, tvCustomerName, tvReviewTime, tvRating, tvReviewContent;
    private EditText edtReply;
    private TextView tvSuggestionOne, tvSuggestionTwo, tvSuggestionThree;
    private View btnCancel, btnSubmitReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reply_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reply_review_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();

        ProductReview review = (ProductReview) getIntent().getSerializableExtra("review_item");
        String productName = getIntent().getStringExtra("product_name");
        String productId = getIntent().getStringExtra("product_id");

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        // Bind data
        if (review != null) {
            tvCustomerName.setText(review.getCustomerName().isEmpty() ? "Khách hàng" : review.getCustomerName());
            
            String dateStr = review.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            tvReviewTime.setText(dateStr != null ? dateStr : "");
            
            tvRating.setText(getStarString(review.rating));
            tvReviewContent.setText(review.content != null ? review.content : "");

            if (review.getReplyContent() != null && !review.getReplyContent().isEmpty()) {
                edtReply.setText(review.getReplyContent());
            }
        }

        if (productName != null) {
            tvProductName.setText(productName);
        }

        // Suggestions click handlers
        tvSuggestionOne.setOnClickListener(v -> edtReply.setText(tvSuggestionOne.getText()));
        tvSuggestionTwo.setOnClickListener(v -> edtReply.setText(tvSuggestionTwo.getText()));
        tvSuggestionThree.setOnClickListener(v -> edtReply.setText(tvSuggestionThree.getText()));

        btnSubmitReply.setOnClickListener(v -> {
            String replyText = edtReply.getText().toString().trim();
            if (replyText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập phản hồi hoặc chọn gợi ý!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (productId == null || productId.isEmpty() || review == null || review.getId() == null || review.getId().isEmpty()) {
                Toast.makeText(this, "Lỗi: Thiếu thông tin sản phẩm hoặc nhận xét!", Toast.LENGTH_SHORT).show();
                return;
            }

            new HttpResquest().callAPI().replyReview(new ProductReview.ReplyRequest(productId, review.getId(), replyText))
                    .enqueue(new Callback<Response<Product>>() {
                        @Override
                        public void onResponse(Call<Response<Product>> call, retrofit2.Response<Response<Product>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                                Toast.makeText(ReplyReviewActivity.this, "Đã gửi phản hồi thành công!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(ReplyReviewActivity.this, "Gửi phản hồi thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi server"), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Response<Product>> call, Throwable t) {
                            Toast.makeText(ReplyReviewActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);
        tvProductName = findViewById(R.id.tvProductName);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvReviewTime = findViewById(R.id.tvReviewTime);
        tvRating = findViewById(R.id.tvRating);
        tvReviewContent = findViewById(R.id.tvReviewContent);

        edtReply = findViewById(R.id.edtReply);
        tvSuggestionOne = findViewById(R.id.tvSuggestionOne);
        tvSuggestionTwo = findViewById(R.id.tvSuggestionTwo);
        tvSuggestionThree = findViewById(R.id.tvSuggestionThree);

        btnCancel = findViewById(R.id.btnCancel);
        btnSubmitReply = findViewById(R.id.btnSubmitReply);
    }

    private String getStarString(float rating) {
        int r = Math.round(rating);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < r) {
                sb.append("★");
            } else {
                sb.append("☆");
            }
        }
        return sb.toString();
    }
}