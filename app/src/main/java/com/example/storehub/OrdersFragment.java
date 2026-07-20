package com.example.storehub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.OrderAdapter;
import com.example.storehub.model.Order;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> allOrders;
    private MaterialButton chipAll, chipPending, chipShipping, chipCompleted, chipCancelled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rvOrders);
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipShipping = view.findViewById(R.id.chipShipping);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);

        orderAdapter = new OrderAdapter(requireContext(), this::showCancelDialog);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(orderAdapter);

        setupFilterChips();
        loadMockOrders();
        filterOrders("all");
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            selectChip(chipAll);
            filterOrders("all");
        });

        chipPending.setOnClickListener(v -> {
            selectChip(chipPending);
            filterOrders("processing");
        });

        chipShipping.setOnClickListener(v -> {
            selectChip(chipShipping);
            filterOrders("shipping");
        });

        chipCompleted.setOnClickListener(v -> {
            selectChip(chipCompleted);
            filterOrders("completed");
        });

        chipCancelled.setOnClickListener(v -> {
            selectChip(chipCancelled);
            filterOrders("cancelled");
        });
    }

    private void selectChip(MaterialButton selected) {
        MaterialButton[] chips = new MaterialButton[]{chipAll, chipPending, chipShipping, chipCompleted, chipCancelled};
        for (MaterialButton chip : chips) {
            if (chip == selected) {
                chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8DFD7));
                chip.setTextColor(0xFF14291F);
                chip.setStrokeWidth(0);
            } else {
                chip.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
                chip.setTextColor(0xFF675C53);
                chip.setStrokeColor(android.content.res.ColorStateList.valueOf(0xFFD6CEC4));
                chip.setStrokeWidth(1);
            }
        }
    }

    private void filterOrders(String status) {
        if ("all".equalsIgnoreCase(status)) {
            orderAdapter.setOrders(allOrders);
        } else {
            ArrayList<Order> filtered = new ArrayList<>();
            for (Order o : allOrders) {
                if (status.equalsIgnoreCase(o.getStatus())) {
                    filtered.add(o);
                }
            }
            orderAdapter.setOrders(filtered);
        }
    }

    private void loadMockOrders() {
        allOrders = new ArrayList<>();

        // Order 1: Đang giao hàng (#SH-882941)
        Order o1 = new Order(
                "#SH-882941", "shipping", "Đang giao hàng",
                "Bình gốm thủ công - Sage",
                "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=500",
                "Phân loại: Gốm nung men đốm / 350ml", 1, 450000, 25000, 40000,
                "Nguyễn Minh Tuấn", "090 123 4567", "123 Đường Tự Do, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh",
                "20/05/2024", "24 Tháng 5, 2024"
        );
        ArrayList<Order.TimelineStep> t1 = new ArrayList<>();
        t1.add(new Order.TimelineStep("Đã xác nhận", "20/06/2024 • 09:15", "", true, false));
        t1.add(new Order.TimelineStep("Đã rời kho", "21/05/2024 • 14:30", "", true, false));
        t1.add(new Order.TimelineStep("Đang giao hàng", "22/06/2024 • 08:00", "Đơn hàng đang được shipper vận chuyển đến bạn.", true, true));
        t1.add(new Order.TimelineStep("Đã giao hàng", "Dự kiến: 24/05/2024", "", false, false));
        o1.setTimeline(t1);
        allOrders.add(o1);

        // Order 2: Đã hoàn thành (#SH-875022)
        Order o2 = new Order(
                "#SH-875022", "completed", "Đã hoàn thành",
                "Tai nghe SoundElite",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
                "Màu sắc: Charcoal Black", 1, 2450000, 35000, 50000,
                "Nguyễn Minh Tuấn", "090 123 4567", "123 Đường Tự Do, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh",
                "19/05/2024", "20/05/2024"
        );
        o2.setCompletedDate("10:30, 20/05/2024");
        ArrayList<Order.TimelineStep> t2 = new ArrayList<>();
        t2.add(new Order.TimelineStep("Đã xác nhận", "19/05/2024 • 10:00", "", true, false));
        t2.add(new Order.TimelineStep("Đã giao hàng thành công", "20/05/2024 • 10:30", "Giao hàng thành công", true, true));
        o2.setTimeline(t2);
        allOrders.add(o2);

        // Order 3: Đã hủy (#SH-863110)
        Order o3 = new Order(
                "#SH-863110", "cancelled", "Đã hủy",
                "Nến thơm Tinh Dầu",
                "https://images.unsplash.com/photo-1603006905003-be475563bc59?w=500",
                "Phân loại: Gỗ Thông & Rêu", 2, 160000, 30000, 30000,
                "Nguyễn Minh Tuấn", "090 123 4567", "123 Đường Tự Do, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh",
                "18/05/2024", ""
        );
        o3.setCancelReason("Thay đổi ý định");
        allOrders.add(o3);
    }

    private void showCancelDialog(Order order) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnDismissCancel).setOnClickListener(v -> bottomSheet.dismiss());

        view.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> {
            order.setStatus("cancelled");
            order.setStatusText("Đã hủy");
            order.setCancelReason("Thay đổi ý định mua hàng");
            orderAdapter.notifyDataSetChanged();
            bottomSheet.dismiss();
        });

        bottomSheet.show();
    }
}
