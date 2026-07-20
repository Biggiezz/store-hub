package com.example.storehub;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Giao diện mẫu chi tiết đơn hàng đã hoàn thành. */
public class CompletedOrderDetailActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_completed);
        applySystemBarInsets();
        ((ImageView) findViewById(R.id.imgOrderAvatar)).setImageResource(R.drawable.figma_completed_2_mockup);

        findViewById(R.id.btnBackOrderDetail).setOnClickListener(view -> finish());
    }

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
