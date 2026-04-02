package com.example.rainsafe;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rainsafe.data.entity.History;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<History> historyList;

    public HistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = historyList.get(position);
        holder.tvTitle.setText(history.getTitle());
        holder.tvDesc.setText(history.getDescription());
        holder.tvTime.setText(history.getTimestamp());
        holder.tvType.setText(history.getType());

        if (history.getTitle().contains("Masuk")) {
            holder.ivIcon.setImageResource(R.drawable.ic_cloud);
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange_accent)));
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_sun);
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_status)));
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTime, tvType;
        ImageView ivIcon;
        View iconContainer;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvDesc = itemView.findViewById(R.id.tvHistoryDesc);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
            tvType = itemView.findViewById(R.id.tvHistoryType);
            ivIcon = itemView.findViewById(R.id.ivHistoryIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}