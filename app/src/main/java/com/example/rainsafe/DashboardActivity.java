package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.Map;
import java.util.Random;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private CardView activeIndicator;
    private View navCurve;
    private ImageView ivActiveIcon;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    // Real-time Views
    private TextView tvHumidityVal, tvRainProbVal, tvAvgHumVal;
    private TextView tvSensorRainStatus, tvSensorLightStatus, tvSensorHumStatus;
    private ProgressBar pbAvgHum;
    private View notificationDot;
    private boolean isRaining = false;

    private DatabaseHelper dbHelper;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);

        // Initialize Navigation Views
        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
        
        activeIndicator = findViewById(R.id.activeIndicator);
        navCurve = findViewById(R.id.navCurve);
        ivActiveIcon = findViewById(R.id.ivActiveIcon);
        
        ivHome = findViewById(R.id.ivHome);
        ivHistory = findViewById(R.id.ivHistory);
        ivSettings = findViewById(R.id.ivSettings);
        ivProfile = findViewById(R.id.ivProfile);
        
        tvHome = findViewById(R.id.tvHome);
        tvHistory = findViewById(R.id.tvHistory);
        tvSettings = findViewById(R.id.tvSettings);
        tvProfile = findViewById(R.id.tvProfile);

        // Initialize Real-time Views
        tvHumidityVal = findViewById(R.id.tvHumidityVal);
        tvRainProbVal = findViewById(R.id.tvRainProbVal);
        tvAvgHumVal = findViewById(R.id.tvAvgHumVal);
        pbAvgHum = findViewById(R.id.pbAvgHum);
        tvSensorRainStatus = findViewById(R.id.tvSensorRainStatus);
        tvSensorLightStatus = findViewById(R.id.tvSensorLightStatus);
        tvSensorHumStatus = findViewById(R.id.tvSensorHumStatus);
        notificationDot = findViewById(R.id.notificationDot);

        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            notificationDot.setVisibility(View.GONE);
            // Optional: open notification list
        });

        // Set Default Position (Home - 0)
        activeIndicator.post(() -> moveIndicator(0));

        // Navigation Listeners
        navHome.setOnClickListener(v -> moveIndicator(0));
        
        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // Handle user navigation with profile data
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        // Setup Real-time Updates
        setupRealTimeUpdates();
    }

    private void setupRealTimeUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateSensorUI();
                // Schedule next update in 3 seconds
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateRunnable);
    }

    private void updateSensorUI() {
        // Fetch from Database
        Map<String, String> rainData = dbHelper.getLatestSensorData("Sensor Hujan");
        Map<String, String> lightData = dbHelper.getLatestSensorData("Sensor Cahaya");
        Map<String, String> humData = dbHelper.getLatestSensorData("Sensor Kelembaban");

        if (!humData.isEmpty()) {
            String val = humData.get("value") + humData.get("unit");
            tvHumidityVal.setText(val);
            tvAvgHumVal.setText(val);
            tvSensorHumStatus.setText("aktif (" + val + ")");
            try {
                pbAvgHum.setProgress(Integer.parseInt(humData.get("value")));
            } catch (Exception ignored) {}
        }

        if (!rainData.isEmpty()) {
            tvRainProbVal.setText(rainData.get("value") + rainData.get("unit"));
            String currentStatus = rainData.get("status");
            tvSensorRainStatus.setText(currentStatus);
            
            // Real-time Notification logic
            if (currentStatus.equalsIgnoreCase("Hujan") && !isRaining) {
                isRaining = true;
                showNotificationAlert("Peringatan: Hujan Terdeteksi!", "Jemuran ditarik otomatis.");
            } else if (currentStatus.equalsIgnoreCase("Aman") || currentStatus.equalsIgnoreCase("Cerah")) {
                isRaining = false;
            }
        }

        if (!lightData.isEmpty()) {
            tvSensorLightStatus.setText(lightData.get("status"));
        }
    }

    private void showNotificationAlert(String title, String message) {
        // Show Red Dot
        notificationDot.setVisibility(View.VISIBLE);
        
        // Android System Notification (Optional but good for real-time feel)
        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, "RAINSAFE_NOTIF")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        
        // Create channel for Android O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel("RAINSAFE_NOTIF", "RainSafe Alerts", android.app.NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        
        notificationManager.notify(1, builder.build());
        
        // Toast for immediate feedback
        android.widget.Toast.makeText(this, title + "\n" + message, android.widget.Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }

    private void moveIndicator(int position) {
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float itemWidth = screenWidth / 4;
        
        float targetXIndicator = (itemWidth * position) + (itemWidth / 2) - (activeIndicator.getWidth() / 2f);
        float targetXCurve = (itemWidth * position) + (itemWidth / 2) - (navCurve.getWidth() / 2f);

        activeIndicator.animate()
                .translationX(targetXIndicator)
                .setDuration(300)
                .start();

        navCurve.animate()
                .translationX(targetXCurve)
                .setDuration(300)
                .start();

        updateNavUI(position);
    }

    private void updateNavUI(int position) {
        int grey = ContextCompat.getColor(this, R.color.text_grey);
        int blue = ContextCompat.getColor(this, R.color.button_blue);

        // Reset
        ivHome.setVisibility(View.VISIBLE);
        ivHistory.setVisibility(View.VISIBLE);
        ivSettings.setVisibility(View.VISIBLE);
        ivProfile.setVisibility(View.VISIBLE);
        
        ivHome.setColorFilter(grey);
        ivHistory.setColorFilter(grey);
        ivSettings.setColorFilter(grey);
        ivProfile.setColorFilter(grey);
        
        tvHome.setTextColor(grey);
        tvHistory.setTextColor(grey);
        tvSettings.setTextColor(grey);
        tvProfile.setTextColor(grey);
        
        tvHome.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvHistory.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvSettings.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvProfile.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Active
        switch (position) {
            case 0:
                ivActiveIcon.setImageResource(R.drawable.ic_home);
                ivHome.setVisibility(View.GONE);
                tvHome.setTextColor(blue);
                tvHome.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 1:
                ivActiveIcon.setImageResource(R.drawable.ic_history);
                ivHistory.setVisibility(View.GONE);
                tvHistory.setTextColor(blue);
                tvHistory.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 2:
                ivActiveIcon.setImageResource(R.drawable.ic_settings);
                ivSettings.setVisibility(View.GONE);
                tvSettings.setTextColor(blue);
                tvSettings.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 3:
                ivActiveIcon.setImageResource(R.drawable.ic_person);
                ivProfile.setVisibility(View.GONE);
                tvProfile.setTextColor(blue);
                tvProfile.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }
}