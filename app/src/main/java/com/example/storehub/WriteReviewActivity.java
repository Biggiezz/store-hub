package com.example.storehub;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.Product;
import com.example.storehub.model.ProductReview;
import com.example.storehub.model.User;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteReviewActivity extends BaseActivity {

    private ImageView btnBack;
    private ShapeableImageView ivReviewProductImage;
    private TextView tvReviewProductName, tvReviewProductVariant;
    private ImageView[] starViews = new ImageView[5];
    private EditText edtReviewContent;
    private MaterialButton btnSubmitReview;

    private int selectedRating = 5; // Mặc định 5 sao
    private ApiServices apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        apiServices = new HttpResquest().callAPI();
        initUi();

        btnBack.setOnClickListener(v -> finish());

        Order order = (Order) getIntent().getSerializableExtra("order_item");
        if (order != null) {
            tvReviewProductName.setText(order.getProductName());
            tvReviewProductVariant.setText(order.getProductVariant());
            Glide.with(this)
                    .load(order.getProductImage())
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(ivReviewProductImage);
        }

        setupStarRating();

        btnSubmitReview.setOnClickListener(v -> {
            String content = edtReviewContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung nhận xét!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
                Toast.makeText(this, "Không có thông tin sản phẩm để đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy productId từ CartItem đầu tiên
            CartItem firstItem = order.getItems().get(0);
            String productId = firstItem.getProductId();
            if (productId.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy mã sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }

            String customerName = "Khách hàng";
            String customerImage = "";
            User user = SharedPreferencesManager.getInstance(this).getUser();
            if (user != null) {
                if (user.getName() != null && !user.getName().isEmpty()) {
                    customerName = user.getName();
                }
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    customerImage = user.getImage();
                }
            }

            ProductReview.AddRequest request = new ProductReview.AddRequest(productId, customerName, customerImage, selectedRating, content);
            apiServices.addReview(request).enqueue(new Callback<com.example.storehub.model.Response<Product>>() {
                @Override
                public void onResponse(Call<com.example.storehub.model.Response<Product>> call, Response<com.example.storehub.model.Response<Product>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                        Toast.makeText(WriteReviewActivity.this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(WriteReviewActivity.this, "Gửi đánh giá thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi server"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.example.storehub.model.Response<Product>> call, Throwable t) {
                    Toast.makeText(WriteReviewActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);
        ivReviewProductImage = findViewById(R.id.ivReviewProductImage);
        tvReviewProductName = findViewById(R.id.tvReviewProductName);
        tvReviewProductVariant = findViewById(R.id.tvReviewProductVariant);

        starViews[0] = findViewById(R.id.ivStar1);
        starViews[1] = findViewById(R.id.ivStar2);
        starViews[2] = findViewById(R.id.ivStar3);
        starViews[3] = findViewById(R.id.ivStar4);
        starViews[4] = findViewById(R.id.ivStar5);

        edtReviewContent = findViewById(R.id.edtReviewContent);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
    }

    private void setupStarRating() {
        for (int i = 0; i < starViews.length; i++) {
            final int starIndex = i + 1;
            starViews[i].setOnClickListener(v -> setRating(starIndex));
        }
        setRating(5);
    }

    private void setRating(int rating) {
        this.selectedRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                starViews[i].setImageResource(R.drawable.ic_star_fill);
            } else {
                starViews[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }
}
