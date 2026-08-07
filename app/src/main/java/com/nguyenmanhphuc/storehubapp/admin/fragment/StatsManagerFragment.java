package com.nguyenmanhphuc.storehubapp.admin.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.AdminRecentActivity;
import com.nguyenmanhphuc.storehubapp.admin.adapter.StatsTimeAdapter;
import com.nguyenmanhphuc.storehubapp.model.response.DailyStat;
import com.nguyenmanhphuc.storehubapp.model.response.RecentActivity;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.response.RevenueData;
import com.nguyenmanhphuc.storehubapp.model.response.TopProduct;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class StatsManagerFragment extends Fragment {

    private BarChart barChart;
    private Spinner spTime;
    private View layoutRevenue, layoutOrder;
    private LinearLayout layoutTopProduct, layoutActivity;
    private TextView tvSeeAll;
    private View progressBarStats, layoutStatsContent;
    private SwipeRefreshLayout swipeRefreshLayout;

    public StatsManagerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);
        setUpListener();
        setUpAdapter();
    }

    private void initUi(View view) {
        barChart = view.findViewById(R.id.barChart);
        spTime = view.findViewById(R.id.spTime);
        layoutRevenue = view.findViewById(R.id.layoutRevenue);
        layoutOrder = view.findViewById(R.id.layoutOrder);
        layoutTopProduct = view.findViewById(R.id.layoutTopProduct);
        layoutActivity = view.findViewById(R.id.layoutActivity);
        tvSeeAll = view.findViewById(R.id.tvSeeAll);
        progressBarStats = view.findViewById(R.id.progressBarStats);
        layoutStatsContent = view.findViewById(R.id.layoutStatsContent);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (spTime != null) {
                    onTimeFilterSelected(spTime.getSelectedItemPosition());
                }
            });
        }

        if (barChart != null) {
            // Mặc định hiển thị thông báo chưa có dữ liệu thống kê nếu không có dữ liệu
            barChart.setNoDataText("Chưa có dữ liệu thống kê");
            barChart.setNoDataTextColor(Color.parseColor("#676863"));
        }
    }

    private void setUpAdapter() {
        if (spTime != null && getContext() != null) {
            List<String> times = new ArrayList<>();
            times.add("Tháng này");
            times.add("Tháng trước");
            times.add("Năm 2026");

            StatsTimeAdapter adapter = new StatsTimeAdapter(requireContext(), times);
            spTime.setAdapter(adapter);
        }
    }

    private void setUpListener() {
        if (spTime != null) {
            spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    onTimeFilterSelected(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AdminRecentActivity.class);
            startActivity(intent);
        });
    }

    private void onTimeFilterSelected(int position) {
        if (progressBarStats != null) progressBarStats.setVisibility(View.VISIBLE);
        if (layoutStatsContent != null) layoutStatsContent.setVisibility(View.GONE);

        HttpResquest request = new HttpResquest();
        request.callAPI().getRevenueStats(position).enqueue(new Callback<Response<RevenueData>>() {
            @Override
            public void onResponse(@NonNull Call<Response<RevenueData>> call, @NonNull retrofit2.Response<Response<RevenueData>> response) {
                if (!isAdded()) return;
                if (progressBarStats != null) progressBarStats.setVisibility(View.GONE);
                if (layoutStatsContent != null) layoutStatsContent.setVisibility(View.VISIBLE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    RevenueData data = response.body().getData();
                    List<BarEntry> entries = new ArrayList<>();
                    String[] labels = new String[0];

                    if (data.getLabels() != null) {
                        labels = data.getLabels().toArray(new String[0]);
                    }

                    if (data.getDailyStats() != null && !data.getDailyStats().isEmpty()) {
                        for (DailyStat stat : data.getDailyStats()) {
                            entries.add(new BarEntry(stat.getIndex(), stat.getRevenue()));
                        }
                    }

                    renderChartAndStats(entries, labels, data.getTotalRevenue(), data.getTotalOrders());
                    renderTopProducts(data.getTopProducts());
                    renderRecentActivities(data.getRecentActivities());
                } else {
                    renderChartAndStats(new ArrayList<>(), new String[0], 0L, 0);
                    renderTopProducts(new ArrayList<>());
                    renderRecentActivities(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<RevenueData>> call, @NonNull Throwable t) {
                if (progressBarStats != null) progressBarStats.setVisibility(View.GONE);
                if (layoutStatsContent != null) layoutStatsContent.setVisibility(View.VISIBLE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (isAdded()) {
                    renderChartAndStats(new ArrayList<>(), new String[0], 0L, 0);
                    renderTopProducts(new ArrayList<>());
                    renderRecentActivities(new ArrayList<>());
                }
            }
        });
    }

    private void renderChartAndStats(List<BarEntry> entries, String[] labels, long totalRevenue, int totalOrders) {
        updateStatCards(totalRevenue, totalOrders);

        if (barChart == null) return;

        barChart.setNoDataText("Chưa có dữ liệu thống kê");
        barChart.setNoDataTextColor(Color.parseColor("#676863"));

        boolean hasData = entries != null && !entries.isEmpty();

        if (!hasData) {
            barChart.clear();
            barChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (triệu đ)");
        dataSet.setColor(Color.parseColor("#1A3B2B"));
        dataSet.setValueTextColor(Color.parseColor("#203028"));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        if (labels != null && labels.length > 0) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        }
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void updateStatCards(long revenue, int orders) {
        if (layoutRevenue != null) {
            TextView tvStatTitle = layoutRevenue.findViewById(R.id.tvStatTitle);
            TextView tvStatValue = layoutRevenue.findViewById(R.id.tvStatValue);
            if (tvStatTitle != null) tvStatTitle.setText("Tổng doanh thu");
            if (tvStatValue != null) {
                tvStatValue.setText(revenue == 0 ? "0 đ" : formatPrice(revenue));
            }
        }

        if (layoutOrder != null) {
            TextView tvStatTitle = layoutOrder.findViewById(R.id.tvStatTitle);
            TextView tvStatValue = layoutOrder.findViewById(R.id.tvStatValue);
            if (tvStatTitle != null) tvStatTitle.setText("Tổng đơn hàng");
            if (tvStatValue != null) {
                tvStatValue.setText(orders == 0 ? "0 đơn hàng" : orders + " đơn hàng");
            }
        }
    }

    private void renderTopProducts(List<TopProduct> products) {
        if (!isAdded()) return;
        if (layoutTopProduct == null) return;
        layoutTopProduct.removeAllViews();
        if (products == null || products.isEmpty()) {
            addEmptyText(layoutTopProduct, "Chưa có sản phẩm bán chạy");
            return;
        }

        for (TopProduct product : products) {
            View item = getLayoutInflater().inflate(R.layout.item_best_product, layoutTopProduct, false);
            ImageView image = item.findViewById(R.id.ivProduct);
            image.setContentDescription(product.getName());
            ((TextView) item.findViewById(R.id.tvProductName)).setText(product.getName());
            ((TextView) item.findViewById(R.id.tvProductSales))
                    .setText("Đã bán " + product.getSoldCount() + " sản phẩm");
            Glide.with(this).load(product.getImage()).placeholder(R.drawable.ic_product).into(image);
            layoutTopProduct.addView(item);
        }
    }

    private void renderRecentActivities(List<RecentActivity> activities) {
        if (!isAdded()) return;
        if (layoutActivity == null) return;
        layoutActivity.removeAllViews();
        if (activities == null || activities.isEmpty()) {
            addEmptyText(layoutActivity, "Chưa có hoạt động gần đây");
            return;
        }

        for (int i = 0; i < activities.size(); i++) {
            RecentActivity activity = activities.get(i);
            View item = getLayoutInflater().inflate(R.layout.item_recent_activity, layoutActivity, false);
            ImageView icon = item.findViewById(R.id.ivActivityIcon);
            int iconResource = getRecentActivityIcon(activity.getType());
            icon.setImageResource(iconResource);
            icon.setBackgroundTintList(ColorStateList.valueOf(getRecentActivityIconBackground(activity.getType())));
            icon.setContentDescription(activity.getTitle());
            ((TextView) item.findViewById(R.id.tvActivityTitle)).setText(activity.getTitle());
            ((TextView) item.findViewById(R.id.tvActivityDetail)).setText(activity.getDetail() != null ? activity.getDetail() : "");
            ((TextView) item.findViewById(R.id.tvActivityTime)).setText(formatDate(activity.getCreatedAt()));
            View divider = item.findViewById(R.id.viewDivider);
            if (divider != null) {
                divider.setVisibility(i == activities.size() - 1 ? View.GONE : View.VISIBLE);
            }
            layoutActivity.addView(item);
        }
    }

    private int getRecentActivityIcon(String type) {
        if ("order_created".equals(type)) {
            return R.drawable.ic_order_shipping;
        }
        if ("order_completed".equals(type)) {
            return R.drawable.ic_check;
        }
        if ("order_cancelled".equals(type)) {
            return R.drawable.ic_order_cancelled;
        }
        if ("product_created".equals(type)) {
            return R.drawable.ic_products;
        }
        if ("login_admin".equals(type) || "login_customer".equals(type)) {
            return R.drawable.ic_user_check;
        }
        if ("user_created".equals(type)) {
            return R.drawable.ic_users;
        }
        return R.drawable.ic_check_done;
    }

    private int getRecentActivityIconBackground(String type) {
        if ("order_cancelled".equals(type)) {
            return Color.parseColor("#F9D8D8");
        }
        if ("order_created".equals(type)) {
            return Color.parseColor("#E6E3DD");
        }
        if ("product_created".equals(type)) {
            return Color.parseColor("#E6E3DD");
        }
        if ("login_admin".equals(type) || "login_customer".equals(type)) {
            return Color.parseColor("#DDE8C0");
        }
        if ("user_created".equals(type)) {
            return Color.parseColor("#E4EAD0");
        }
        return Color.parseColor("#EADDD2");
    }

    private void addEmptyText(LinearLayout container, String message) {
        TextView text = new TextView(requireContext(), null, 0, R.style.EmptyStateTextStyle);
        text.setText(message);
        container.addView(text);
    }

    private String formatDate(String value) {
        return DateTimeUtils.formatISOToLocal(value, "dd/MM/yyyy HH:mm");
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}
