package com.example.rainsafe;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmpty, btnClearAll;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        dbHelper = new DatabaseHelper(this);
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnClearAll = findViewById(R.id.btnClearAll);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>(), position -> {
            NotificationModel notification = adapter.getNotifications().get(position);
            dbHelper.deleteLog(notification.getId());
            loadNotifications();
        });
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> {
            dbHelper.clearAllLogs();
            loadNotifications();
        });

        startRealtimeUpdate();
    }

    private void startRealtimeUpdate() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadNotifications();
                handler.postDelayed(this, 3000); // Refresh every 3 seconds
            }
        };
        handler.post(refreshRunnable);
    }

    private void loadNotifications() {
        List<Map<String, String>> logs = dbHelper.getAllLogs();
        List<NotificationModel> notifications = new ArrayList<>();

        for (Map<String, String> log : logs) {
            notifications.add(new NotificationModel(
                    Integer.parseInt(log.get("id")),
                    log.get("title"),
                    log.get("desc"),
                    log.get("time"),
                    log.get("type"),
                    log.get("icon")
            ));
        }

        if (notifications.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(notifications);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }
}