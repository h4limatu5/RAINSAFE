package com.example.rainsafe;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends BaseActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmpty, btnClearAll;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnClearAll = findViewById(R.id.btnClearAll);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>(), position -> {
            NotificationModel notification = adapter.getNotifications().get(position);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Notifikasi")
                .setMessage("Hapus notifikasi \"" + notification.getTitle() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    dbHelper.deleteLog(notification.getId());
                    loadNotifications();
                })
                .setNegativeButton("Batal", null)
                .show();
        });
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Semua Notifikasi")
                .setMessage("Anda yakin ingin menghapus semua notifikasi?")
                .setPositiveButton("Hapus Semua", (dialog, which) -> {
                    dbHelper.clearAllLogs();
                    loadNotifications();
                })
                .setNegativeButton("Batal", null)
                .show();
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