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
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    // Real-time Views
    private TextView tvRainProbVal, tvCurrentDuration, tvStatsTotalDuration;
    private TextView tvSensorRainStatus, tvSensorLightStatus, tvSensorTempStatus;
    private View notificationDot;
    private ImageView ivAutoIcon;
    private CardView cvAutoIcon;
    private boolean isRaining = false;
    private boolean isLaundryOut = true; // Initial state based on XML
    private long laundryStartTime = System.currentTimeMillis() - (20 * 60 * 1000); // Simulated 20 mins ago

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
        
        ivHome = findViewById(R.id.ivHome);
        ivHistory = findViewById(R.id.ivHistory);
        ivSettings = findViewById(R.id.ivSettings);
        ivProfile = findViewById(R.id.ivProfile);
        
        tvHome = findViewById(R.id.tvHome);
        tvHistory = findViewById(R.id.tvHistory);
        tvSettings = findViewById(R.id.tvSettings);
        tvProfile = findViewById(R.id.tvProfile);

        // Initialize Real-time Views
        tvRainProbVal = findViewById(R.id.tvRainProbVal);
        tvCurrentDuration = findViewById(R.id.tvCurrentDuration);
        tvStatsTotalDuration = findViewById(R.id.tvStatsTotalDuration);
        tvSensorRainStatus = findViewById(R.id.tvSensorRainStatus);
        tvSensorLightStatus = findViewById(R.id.tvSensorLightStatus);
        tvSensorTempStatus = findViewById(R.id.tvSensorTempStatus);
        notificationDot = findViewById(R.id.notificationDot);
        ivAutoIcon = findViewById(R.id.ivAutoIcon);
        cvAutoIcon = findViewById(R.id.cvAutoIcon);

        findViewById(R.id.btnRefresh).setOnClickListener(v -> updateSensorUI());

        findViewById(R.id.btnPullIn).setOnClickListener(v -> {
            isLaundryOut = false;
            android.widget.Toast.makeText(this, "Jemuran dimasukkan", android.widget.Toast.LENGTH_SHORT).show();
            dbHelper.addLog("Manual", "Jemuran dimasukkan secara manual", "user", "action");
        });

        findViewById(R.id.btnPullOut).setOnClickListener(v -> {
            if (!isLaundryOut) {
                laundryStartTime = System.currentTimeMillis();
            }
            isLaundryOut = true;
            android.widget.Toast.makeText(this, "Jemuran dikeluarkan", android.widget.Toast.LENGTH_SHORT).show();
            dbHelper.addLog("Manual", "Jemuran dikeluarkan secara manual", "user", "action");
        });

        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
        });

        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            notificationDot.setVisibility(View.GONE);
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        // Set Default Position (Home - 0)
        updateNavUI(0);

        // Navigation Listeners
        navHome.setOnClickListener(v -> updateNavUI(0));
        
        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
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
                simulateSensorChanges();
                updateSensorUI();
                // Schedule next update in 3 seconds
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateRunnable);
    }

    private void simulateSensorChanges() {
        Random random = new Random();
        
        // Simulate Rain Sensor (0-100%)
        int rainVal = random.nextInt(100);
        String rainStatus = rainVal > 50 ? "Hujan" : "Aman";
        dbHelper.updateSensorData("Sensor Hujan", String.valueOf(rainVal), rainStatus);
        
        // Simulate Light Sensor (0-2000 lux)
        int lightVal = random.nextInt(2000);
        String lightStatus = lightVal > 1000 ? "Terik" : (lightVal > 300 ? "Cerah" : "Mendung");
        dbHelper.updateSensorData("Sensor Cahaya", String.valueOf(lightVal), lightStatus);

        // Simulate Temperature Sensor (20-40 °C)
        int tempVal = 20 + random.nextInt(20);
        String tempStatus = tempVal > 30 ? "Panas" : (tempVal > 25 ? "Hangat" : "Sejuk");
        dbHelper.updateSensorData("Sensor Suhu", String.valueOf(tempVal), tempStatus);

        // Update Dashboard Main Temperature/Weather (Optional Simulation)
        TextView tvTemp = findViewById(R.id.tvBigTemp);
        TextView tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        if (tvTemp != null) {
            int temp = 25 + random.nextInt(10);
            tvTemp.setText(temp + " °C");
        }
        if (tvWeatherDesc != null) {
            tvWeatherDesc.setText(rainStatus.equals("Hujan") ? "Hujan Berawan" : "Cerah Berawan");
        }
    }

    private void updateSensorUI() {
        // Update Auto Mode Icon and Background based on Settings
        android.content.SharedPreferences prefs = getSharedPreferences("RainSafePrefs", MODE_PRIVATE);
        boolean isAutoMode = prefs.getBoolean("auto_mode", true);
        if (ivAutoIcon != null && cvAutoIcon != null) {
            if (isAutoMode) {
                cvAutoIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#BDE2F9"));
                ivAutoIcon.setColorFilter(ContextCompat.getColor(this, R.color.black));
            } else {
                cvAutoIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"));
                ivAutoIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_grey));
            }
        }

        // Update Duration
        if (isLaundryOut) {
            long elapsedMillis = System.currentTimeMillis() - laundryStartTime;
            long minutes = (elapsedMillis / (1000 * 60)) % 60;
            long hours = (elapsedMillis / (1000 * 60 * 60));
            tvCurrentDuration.setText(hours + " jam " + minutes + " menit");
            
            // Update total stats (simulated addition)
            tvStatsTotalDuration.setText((6 + hours) + " jam " + (10 + minutes) + " menit");
        }

        // Fetch from Database
        Map<String, String> rainData = dbHelper.getLatestSensorData("Sensor Hujan");
        Map<String, String> lightData = dbHelper.getLatestSensorData("Sensor Cahaya");
        Map<String, String> tempData = dbHelper.getLatestSensorData("Sensor Suhu");

        if (!rainData.isEmpty()) {
            tvRainProbVal.setText(rainData.get("value") + rainData.get("unit"));
            String currentStatus = rainData.get("status");
            tvSensorRainStatus.setText(currentStatus);
            
            // Real-time Notification logic
            if (currentStatus.equalsIgnoreCase("Hujan") && !isRaining) {
                isRaining = true;
                dbHelper.addLog("Peringatan Hujan!", "Sensor mendeteksi hujan. Jemuran ditarik otomatis.", "system", "rain");
                showNotificationAlert("Peringatan: Hujan Terdeteksi!", "Jemuran ditarik otomatis.");
                notificationDot.setVisibility(View.VISIBLE);
                sendEmailNotification("RainSafe: Peringatan Hujan!", 
                    "Halo, sistem RainSafe mendeteksi hujan. Jemuran Anda telah ditarik secara otomatis untuk keamanan.");
            } else if (currentStatus.equalsIgnoreCase("Aman") || currentStatus.equalsIgnoreCase("Cerah")) {
                isRaining = false;
            }
        }

        if (!lightData.isEmpty()) {
            tvSensorLightStatus.setText(lightData.get("status"));
        }

        if (!tempData.isEmpty()) {
            tvSensorTempStatus.setText(tempData.get("status") + " (" + tempData.get("value") + "°C)");
        }
    }

    private void sendEmailNotification(String title, String message) {
        String identifier = getIntent().getStringExtra("USER_IDENTIFIER");
        String loginType = getIntent().getStringExtra("LOGIN_TYPE");
        String targetEmail = "";

        if ("email".equals(loginType)) {
            targetEmail = identifier;
        } else if (identifier != null) {
            // Jika login via HP, ambil email dari database
            Map<String, String> userData = dbHelper.getUserDataByPhone(identifier);
            if (userData != null && userData.containsKey("email")) {
                targetEmail = userData.get("email");
            }
        }

        if (targetEmail != null && !targetEmail.isEmpty()) {
            android.util.Log.d("RainSafe_Email", "Mengirim email ke: " + targetEmail);
            // Toast sebagai indikasi email terkirim melalui background service simulasi
            android.widget.Toast.makeText(this, "Email dikirim ke: " + targetEmail, android.widget.Toast.LENGTH_SHORT).show();
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

    private void updateNavUI(int position) {
        int grey = ContextCompat.getColor(this, R.color.text_grey);
        int blue = ContextCompat.getColor(this, R.color.button_blue);

        // Reset all to grey/normal
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

        // Set active item to blue/bold
        switch (position) {
            case 0:
                ivHome.setColorFilter(blue);
                tvHome.setTextColor(blue);
                tvHome.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 1:
                ivHistory.setColorFilter(blue);
                tvHistory.setTextColor(blue);
                tvHistory.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 2:
                ivSettings.setColorFilter(blue);
                tvSettings.setTextColor(blue);
                tvSettings.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 3:
                ivProfile.setColorFilter(blue);
                tvProfile.setTextColor(blue);
                tvProfile.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }
}