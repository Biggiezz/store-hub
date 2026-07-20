package com.example.storehub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.model.Order;

import java.util.ArrayList;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Order.TimelineStep> timelineList;

    public TimelineAdapter(Context context, ArrayList<Order.TimelineStep> timelineList) {
        this.context = context;
        this.timelineList = timelineList != null ? timelineList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order.TimelineStep step = timelineList.get(position);

        holder.tvTimelineTitle.setText(step.getTitle());
        holder.tvTimelineTime.setText(step.getTime());

        if (step.getDescription() != null && !step.getDescription().isEmpty()) {
            holder.tvTimelineDesc.setText(step.getDescription());
            holder.tvTimelineDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvTimelineDesc.setVisibility(View.GONE);
        }

        // Configure node icon
        if (step.isCurrent()) {
            holder.ivTimelineIcon.setImageResource(R.drawable.ic_truck_delivering);
        } else if (step.isCompleted()) {
            holder.ivTimelineIcon.setImageResource(R.drawable.ic_check_circle_green);
        } else {
            holder.ivTimelineIcon.setImageResource(R.drawable.ic_check_circle_green);
            holder.ivTimelineIcon.setAlpha(0.3f);
        }

        // Hide line for last item
        if (position == timelineList.size() - 1) {
            holder.viewTimelineLine.setVisibility(View.GONE);
        } else {
            holder.viewTimelineLine.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return timelineList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTimelineIcon;
        View viewTimelineLine;
        TextView tvTimelineTitle, tvTimelineTime, tvTimelineDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTimelineIcon = itemView.findViewById(R.id.ivTimelineIcon);
            viewTimelineLine = itemView.findViewById(R.id.viewTimelineLine);
            tvTimelineTitle = itemView.findViewById(R.id.tvTimelineTitle);
            tvTimelineTime = itemView.findViewById(R.id.tvTimelineTime);
            tvTimelineDesc = itemView.findViewById(R.id.tvTimelineDesc);
        }
    }
}
