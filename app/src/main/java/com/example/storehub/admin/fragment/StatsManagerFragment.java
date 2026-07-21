package com.example.storehub.admin.fragment;

import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.admin.adapter.StatsTimeAdapter;
import com.example.storehub.model.AdminStats;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;

public class StatsManagerFragment extends Fragment {

    private BarChart barChart;
    private Spinner spTime;
    private View layoutRevenue;
    private View layoutOrder;
    private LinearLayout layoutTopProduct;
    private LinearLayout layoutActivity;

    public StatsManagerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
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
    }

    private void onTimeFilterSelected(int position) {
        HttpResquest request = new HttpResquest();
        request.callAPI().getRevenueStats(position).enqueue(new Callback<Response<AdminStats.RevenueData>>() {
            @Override
            public void onResponse(@NonNull Call<Response<AdminStats.RevenueData>> call,
                                   @NonNull retrofit2.Response<Response<AdminStats.RevenueData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    AdminStats.RevenueData data = response.body().getData();
                    List<BarEntry> entries = new ArrayList<>();
                    String[] labels = new String[0];

                    if (data.getLabels() != null) {
                        labels = data.getLabels().toArray(new String[0]);
                    }

                    if (data.getDailyStats() != null && !data.getDailyStats().isEmpty()) {
                        for (AdminStats.DailyStat stat : data.getDailyStats()) {
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
            public void onFailure(@NonNull Call<Response<AdminStats.RevenueData>> call, @NonNull Throwable t) {
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

    private void renderTopProducts(List<AdminStats.TopProduct> products) {
        if (layoutTopProduct == null) return;
        layoutTopProduct.removeAllViews();
        if (products == null || products.isEmpty()) {
            addEmptyText(layoutTopProduct, "Chưa có sản phẩm bán chạy");
            return;
        }

        for (AdminStats.TopProduct product : products) {
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

    private void renderRecentActivities(List<AdminStats.RecentActivity> activities) {
        if (layoutActivity == null) return;
        layoutActivity.removeAllViews();
        if (activities == null || activities.isEmpty()) {
            addEmptyText(layoutActivity, "Chưa có hoạt động gần đây");
            return;
        }

        for (AdminStats.RecentActivity activity : activities) {
            View item = getLayoutInflater().inflate(R.layout.item_recent_activity, layoutActivity, false);
            ImageView icon = item.findViewById(R.id.ivActivityIcon);
            int iconResource = "user".equals(activity.getType())
                    ? R.drawable.ic_user_check
                    : "low_stock".equals(activity.getType())
                    ? R.drawable.ic_product_check
                    : R.drawable.ic_check;
            icon.setImageResource(iconResource);
            icon.setContentDescription(activity.getTitle());
            ((TextView) item.findViewById(R.id.tvActivityTitle)).setText(activity.getTitle());
            ((TextView) item.findViewById(R.id.tvActivityTime)).setText(formatDate(activity.getCreatedAt()));
            layoutActivity.addView(item);
        }
    }

    private void addEmptyText(LinearLayout container, String message) {
        TextView text = new TextView(requireContext());
        text.setText(message);
        text.setTextColor(Color.parseColor("#676863"));
        text.setPadding(0, 16, 0, 16);
        container.addView(text);
    }

    private String formatDate(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = input.parse(value);
            return date == null ? value : new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
        } catch (Exception ignored) {
            return value;
        }
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}
