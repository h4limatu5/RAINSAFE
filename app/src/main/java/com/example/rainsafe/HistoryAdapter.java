package com.example.rainsafe;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

/**
 * Adapter for the History RecyclerView.
 * Supports two view types:
 *   TYPE_HEADER – a date-section header row (e.g. "HARI INI")
 *   TYPE_ITEM   – an activity-log card row
 *
 * Items in the data list are either Map<String,String> (a log entry)
 * or String (a header label).
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    // Each element is either a String (header) or Map<String,String> (log item)
    private List<Object> data;

    public HistoryAdapter(List<Object> data) {
        this.data = data;
    }

    public void setData(List<Object> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (data.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_history_header, parent, false);
            return new HeaderHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_history, parent, false);
            return new ItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((String) data.get(position));
        } else {
            //noinspection unchecked
            ((ItemHolder) holder).bind((Map<String, String>) data.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    // ──────────────────────────────────────────
    // ViewHolder: Header
    // ──────────────────────────────────────────
    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }

        void bind(String title) {
            tvHeaderTitle.setText(title);
        }
    }

    // ──────────────────────────────────────────
    // ViewHolder: Item
    // ──────────────────────────────────────────
    static class ItemHolder extends RecyclerView.ViewHolder {
        CardView  cvIcon;
        ImageView ivIcon;
        TextView  tvTime, tvTitle, tvDesc, tvBadge;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            cvIcon  = itemView.findViewById(R.id.cvIcon);
            ivIcon  = itemView.findViewById(R.id.ivIcon);
            tvTime  = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc  = itemView.findViewById(R.id.tvDesc);
            tvBadge = itemView.findViewById(R.id.tvBadge);
        }

        void bind(Map<String, String> log) {
            tvTitle.setText(log.get("title"));
            tvDesc.setText(log.get("desc"));
            tvTime.setText(log.get("time") != null ? log.get("time") : "");

            String icon = log.get("icon");
            String type = log.get("type");
            if (icon == null) icon = "";
            if (type == null) type = "";

            // Icon image and card background colour
            switch (icon) {
                case "out":
                    ivIcon.setImageResource(R.drawable.ic_arrow_upward);
                    ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
                    cvIcon.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                    break;
                case "in":
                    ivIcon.setImageResource(R.drawable.ic_hanger);
                    ivIcon.setColorFilter(Color.parseColor("#F44336"));
                    cvIcon.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                    break;
                case "rain":
                    ivIcon.setImageResource(R.drawable.ic_rainy);
                    ivIcon.setColorFilter(Color.parseColor("#FF9800"));
                    cvIcon.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                    break;
                default: // sensor / others
                    ivIcon.setImageResource(R.drawable.ic_settings);
                    ivIcon.setColorFilter(Color.parseColor("#2196F3"));
                    cvIcon.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                    break;
            }

            // Badge label and colour
            switch (type) {
                case "auto":
                    tvBadge.setText("Otomatis");
                    break;
                case "manual":
                    tvBadge.setText("Manual");
                    break;
                default:
                    tvBadge.setText("Sistem");
                    break;
            }
        }
    }
}
