package com.example.rainsafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationModel> notifications;

    public NotificationAdapter(List<NotificationModel> notifications) {
        this.notifications = notifications;
    }

    public void setNotifications(List<NotificationModel> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTimestamp());

        String iconType = notification.getIcon();
        if (iconType == null) iconType = "";

        switch (iconType) {
            case "rain":
                holder.ivIcon.setImageResource(R.drawable.ic_rainy);
                holder.ivIcon.setBackgroundResource(R.drawable.circle_blue_light);
                break;
            case "in":
                holder.ivIcon.setImageResource(R.drawable.ic_arrow_back);
                holder.ivIcon.setBackgroundResource(R.drawable.circle_blue_light);
                break;
            case "out":
                holder.ivIcon.setImageResource(R.drawable.ic_arrow_upward);
                holder.ivIcon.setBackgroundResource(R.drawable.circle_blue_light);
                break;
            default:
                holder.ivIcon.setImageResource(R.drawable.ic_settings);
                holder.ivIcon.setBackgroundResource(R.drawable.circle_blue_light);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}