package com.example.storehub;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.storehub.model.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class WriteReviewActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ShapeableImageView ivReviewProductImage;
    private TextView tvReviewProductName, tvReviewProductVariant;
    private ImageView[] starViews = new ImageView[5];
    private EditText edtReviewContent;
    private MaterialButton btnSubmitReview;

    private int selectedRating = 5; // Mặc định 5 sao

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

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
            String review = edtReviewContent.getText().toString().trim();
            if (review.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung nhận xét!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Cảm ơn bạn đã gửi đánh giá " + selectedRating + " sao!", Toast.LENGTH_SHORT).show();
            finish();
        });
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
