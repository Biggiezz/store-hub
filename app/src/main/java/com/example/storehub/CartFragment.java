package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Màn hình giao diện đơn hàng. Dữ liệu hiện tại chỉ là dữ liệu mẫu trong XML,
 * chưa kết nối API và chưa có xử lý nghiệp vụ.
 */
public class CartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        view.findViewById(R.id.btnViewShippingOrder).setOnClickListener(v ->
                openDetail(ShippingOrderDetailActivity.class));
        // Mockup: chưa cập nhật server, chỉ chuyển sang giao diện trạng thái đã hủy.
        view.findViewById(R.id.btnCancelOrder).setOnClickListener(v ->
                openDetail(CancelledOrderDetailActivity.class));

        return view;
    }

    private void openDetail(Class<? extends AppCompatActivity> destination) {
        startActivity(new Intent(requireContext(), destination));
    }
}
