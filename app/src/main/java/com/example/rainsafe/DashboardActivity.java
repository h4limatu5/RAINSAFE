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
import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.view.View;

import org.json.JSONObject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    // Real-time Views
    private TextView tvRainProbVal, tvCurrentDuration;
    private TextView tvSensorRainStatus, tvSensorLightStatus;
    private View notificationDot;
    private ImageView ivAutoIcon;
    private CardView cvAutoIcon;
    private CardView btnLaundryAction;
    private ImageView ivLaundryActionIcon;
    private TextView tvLaundryActionLabel;
    private TextView tvAutoStatus;           // Teks "Aktif / Non-aktif" di card otomatisasi
    private TextView tvLaundryPositionBadge; // Badge Di Luar / Di Dalam
    private TextView tvLaundryStartTime;    // Waktu mulai jemuran dikeluarkan
    private androidx.appcompat.widget.SwitchCompat swAutoMode;
    private boolean isAutoModeSwitchUpdating = false; // Guard to prevent echo loop
    private boolean isManualCommand = false;           // Guard: block Firebase echo after manual action
    private boolean isRaining = false;
    private boolean isDryNotified = false;
    private boolean isLaundryOut = true; // Initial state based on XML
    private long laundryStartTime = System.currentTimeMillis() - (20 * 60 * 1000); // Simulated 20 mins ago

    // Weather (Open-Meteo)
    private TextView tvBigTemp, tvWeatherDesc;
    private static final String OPEN_METEO_URL =
        "https://api.open-meteo.com/v1/forecast?latitude=-3.7667&longitude=114.7667" +
        "&current=temperature_2m,relative_humidity_2m,rain" +
        "&hourly=precipitation_probability,rain&timezone=Asia/Jakarta";
    private ExecutorService weatherExecutor = Executors.newSingleThreadExecutor();

    // Connection status views
    private View ivStatusDot;
    private TextView tvStatusText;
    private ValueEventListener connectivityListener;

    private DatabaseHelper dbHelper;
    private FirebaseSyncHelper firebaseHelper;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        // Apply window insets to bottom navigation to avoid overlap with system navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            View bottomNav = findViewById(R.id.bottomNavContainer);
            if (bottomNav != null) {
                int bottomInset = systemBars.bottom;
                bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), bottomInset);
                android.view.ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
                int baseHeight = (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 75, v.getResources().getDisplayMetrics());
                params.height = baseHeight + bottomInset;
                bottomNav.setLayoutParams(params);
            }
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        firebaseHelper = new FirebaseSyncHelper(this);

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
        tvSensorRainStatus = findViewById(R.id.tvSensorRainStatus);
        tvSensorLightStatus = findViewById(R.id.tvSensorLightStatus);
        notificationDot = findViewById(R.id.notificationDot);
        ivAutoIcon = findViewById(R.id.ivAutoIcon);
        cvAutoIcon = findViewById(R.id.cvAutoIcon);
        swAutoMode = findViewById(R.id.swAutoMode);
        tvAutoStatus = findViewById(R.id.tvAutoStatus);
        btnLaundryAction = findViewById(R.id.btnLaundryAction);
        ivLaundryActionIcon = findViewById(R.id.ivLaundryActionIcon);
        tvLaundryActionLabel = findViewById(R.id.tvLaundryActionLabel);
        tvLaundryPositionBadge = findViewById(R.id.tvLaundryPositionBadge);
        tvLaundryStartTime = findViewById(R.id.tvLaundryStartTime);

        // Weather Views
        tvBigTemp = findViewById(R.id.tvBigTemp);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);

        // Connection Status Views
        ivStatusDot = findViewById(R.id.ivStatusDot);
        tvStatusText = findViewById(R.id.tvStatusText);
        startListeningConnection();

        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            updateSensorUI();
            fetchWeather(); // Refresh cuaca
        });

        // Wire up the Auto Mode toggle in the automation card
        if (swAutoMode != null) {
            swAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isAutoModeSwitchUpdating) return;
                // Guard: block Firebase echo untuk 2 detik
                isManualCommand = true;
                handler.postDelayed(() -> isManualCommand = false, 2000);
                getSharedPreferences("RainSafePrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("auto_mode", isChecked)
                        .apply();
                firebaseHelper.updateAutoMode(isChecked);
                updateSensorUI();
                android.widget.Toast.makeText(this,
                        isChecked ? "Mode Otomatis Aktif" : "Mode Manual Aktif",
                        android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        btnLaundryAction.setOnClickListener(v -> {
            // Guard: block Firebase echo untuk 2 detik
            isManualCommand = true;
            handler.postDelayed(() -> isManualCommand = false, 2000);

            if (isLaundryOut) {
                // Jemuran di luar → Masukan
                isLaundryOut = false;
                android.widget.Toast.makeText(this, "Jemuran dimasukkan", android.widget.Toast.LENGTH_SHORT).show();
                dbHelper.addLog("Manual", "Jemuran dimasukkan secara manual", "user", "action");
                firebaseHelper.updateLaundryStatus("in");
            } else {
                // Jemuran di dalam → Keluarkan
                laundryStartTime = System.currentTimeMillis();
                isLaundryOut = true;
                android.widget.Toast.makeText(this, "Jemuran dikeluarkan", android.widget.Toast.LENGTH_SHORT).show();
                dbHelper.addLog("Manual", "Jemuran dikeluarkan secara manual", "user", "action");
                firebaseHelper.updateLaundryStatus("out");
            }
            updateLaundryButton();
            updateLaundryStatusSection();
            firebaseHelper.syncLogs();
        });

        // Inisialisasi tampilan button & status section
        updateLaundryButton();
        updateLaundryStatusSection();

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

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        setupRealTimeUpdates();

        // Ambil data cuaca saat pertama kali buka
        fetchWeather();

        // Start Media Playback Service
        Intent mediaIntent = new Intent(this, MediaPlaybackService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(mediaIntent);
        } else {
            startService(mediaIntent);
        }
    }

    // ─── OPEN-METEO WEATHER ──────────────────────────────────────────────────

    /**
     * Fetch cuaca dari Open-Meteo API secara background thread,
     * lalu update UI di main thread.
     */
    private void fetchWeather() {
        weatherExecutor.execute(() -> {
            try {
                URL url = new URL(OPEN_METEO_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject root = new JSONObject(sb.toString());
                    JSONObject current = root.getJSONObject("current");

                    double temp = current.getDouble("temperature_2m");
                    double humidity = current.getDouble("relative_humidity_2m");
                    double rain = current.getDouble("rain");

                    // Ambil peluang hujan jam pertama dari hourly
                    int precipProb = 0;
                    JSONObject hourly = root.getJSONObject("hourly");
                    if (hourly.has("precipitation_probability")) {
                        precipProb = hourly.getJSONArray("precipitation_probability").getInt(0);
                    }

                    final double finalTemp = temp;
                    final double finalRain = rain;
                    final int finalPrecipProb = precipProb;

                    handler.post(() -> updateWeatherUI(finalTemp, finalRain, finalPrecipProb));
                }
                conn.disconnect();
            } catch (Exception e) {
                android.util.Log.e("RainSafe_Weather", "Gagal fetch cuaca: " + e.getMessage());
            }
        });
    }

    /**
     * Perbarui tampilan cuaca di UI berdasarkan data Open-Meteo.
     */
    private void updateWeatherUI(double temp, double rainMm, int precipProb) {
        if (tvBigTemp != null) {
            tvBigTemp.setText(String.format(Locale.getDefault(), "%.0f °C", temp));
        }
        if (tvWeatherDesc != null) {
            String desc;
            if (rainMm > 0) {
                desc = "Sedang Hujan";
                isRaining = true;
            } else if (precipProb >= 70) {
                desc = "Berpotensi Hujan";
            } else if (precipProb >= 40) {
                desc = "Agak Mendung";
            } else {
                desc = "Cerah Berawan";
                isRaining = false;
            }
            tvWeatherDesc.setText(desc);
        }
        // Update rain prob di mini card
        if (tvRainProbVal != null) {
            tvRainProbVal.setText(precipProb + "%");
        }
    }

    // ─── REAL-TIME SETUP ─────────────────────────────────────────────────────

    private void setupRealTimeUpdates() {
        firebaseHelper.startListeningSensors((name, value, status, unit) -> {
            runOnUiThread(this::updateSensorUI);
        });

        firebaseHelper.startListeningControl((autoMode, laundryStatusVal) -> {
            runOnUiThread(() -> {
                // Abaikan echo balik jika baru saja ada perintah manual
                if (isManualCommand) return;

                getSharedPreferences("RainSafePrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("auto_mode", autoMode)
                        .apply();

                if (swAutoMode != null && swAutoMode.isChecked() != autoMode) {
                    isAutoModeSwitchUpdating = true;
                    swAutoMode.setChecked(autoMode);
                    isAutoModeSwitchUpdating = false;
                }

                boolean newLaundryOut = "out".equalsIgnoreCase(laundryStatusVal);
                if (newLaundryOut != isLaundryOut) {
                    if (newLaundryOut) laundryStartTime = System.currentTimeMillis();
                    isLaundryOut = newLaundryOut;
                    updateLaundryButton();
                    updateLaundryStatusSection();
                }
                updateSensorUI();
            });
        });

        // Refresh UI + cuaca setiap 30 detik
        updateRunnable = new Runnable() {
            private int tickCount = 0;
            @Override
            public void run() {
                updateSensorUI();
                tickCount++;
                // Fetch cuaca setiap 10 tick (30s * 10 = 5 menit)
                if (tickCount % 10 == 0) fetchWeather();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateRunnable);
    }

    // ─── LAUNDRY BUTTON ──────────────────────────────────────────────────────

    /**
     * Update tampilan button aksi jemuran berdasarkan kondisi isLaundryOut.
     */
    private void updateLaundryButton() {
        if (btnLaundryAction == null) return;
        if (isLaundryOut) {
            btnLaundryAction.setCardBackgroundColor(ContextCompat.getColor(this, R.color.button_blue));
            tvLaundryActionLabel.setText("Masukan Jemuran");
            ivLaundryActionIcon.setImageResource(R.drawable.ic_hanger);
            ivLaundryActionIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
        } else {
            btnLaundryAction.setCardBackgroundColor(ContextCompat.getColor(this, R.color.button_orange));
            tvLaundryActionLabel.setText("Keluarkan Jemuran");
            ivLaundryActionIcon.setImageResource(R.drawable.ic_arrow_upward);
            ivLaundryActionIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
        }
    }

    // ─── LAUNDRY STATUS SECTION ───────────────────────────────────────────────

    /**
     * Update bagian status jemuran di dashboard:
     * - Jemuran di luar: badge "Di Luar" + waktu mulai dikeluarkan
     * - Jemuran di dalam: badge "Di Dalam" + pesan "Jemuran sedang di dalam"
     */
    private void updateLaundryStatusSection() {
        if (tvLaundryPositionBadge == null) return;
        if (isLaundryOut) {
            // Badge hijau "Di Luar"
            tvLaundryPositionBadge.setText("Di Luar");
            tvLaundryPositionBadge.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            tvLaundryPositionBadge.setBackgroundResource(R.drawable.status_green_bg);

            // Tampilkan waktu mulai
            if (tvLaundryStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd MMM yyyy", new Locale("id", "ID"));
                String startStr = sdf.format(new Date(laundryStartTime));
                tvLaundryStartTime.setText(startStr);
            }
        } else {
            // Badge abu-abu "Di Dalam"
            tvLaundryPositionBadge.setText("Di Dalam");
            tvLaundryPositionBadge.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
            tvLaundryPositionBadge.setBackgroundResource(R.drawable.status_gray_bg);

            // Sembunyikan waktu mulai dengan strip
            if (tvLaundryStartTime != null) {
                tvLaundryStartTime.setText("—");
            }
        }
    }

    // ─── SENSOR UI ───────────────────────────────────────────────────────────

    private void updateSensorUI() {
        // Update Auto Mode Icon, Background, dan teks status
        android.content.SharedPreferences prefs = getSharedPreferences("RainSafePrefs", MODE_PRIVATE);
        boolean isAutoMode = prefs.getBoolean("auto_mode", true);
        
        if (swAutoMode != null && swAutoMode.isChecked() != isAutoMode) {
            swAutoMode.setChecked(isAutoMode);
        }

        if (ivAutoIcon != null && cvAutoIcon != null) {
            if (isAutoMode) {
                cvAutoIcon.setCardBackgroundColor(ContextCompat.getColor(this, R.color.button_blue));
                ivAutoIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));
            } else {
                cvAutoIcon.setCardBackgroundColor(ContextCompat.getColor(this, R.color.input_bg));
                ivAutoIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_grey));
            }
        }
        // Update teks "Aktif / Non-aktif"
        if (tvAutoStatus != null) {
            if (isAutoMode) {
                tvAutoStatus.setText("Aktif");
                tvAutoStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                tvAutoStatus.setText("Non-aktif");
                tvAutoStatus.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
            }
        }

        // Update Duration (hanya saat jemuran di luar)
        if (isLaundryOut) {
            long elapsedMillis = System.currentTimeMillis() - laundryStartTime;
            long minutes = (elapsedMillis / (1000 * 60)) % 60;
            long hours = (elapsedMillis / (1000 * 60 * 60));
            tvCurrentDuration.setText(hours + " jam " + minutes + " menit");
        } else {
            tvCurrentDuration.setText("—");
        }

        // Fetch from Database
        Map<String, String> rainData = dbHelper.getLatestSensorData("Sensor Hujan");
        Map<String, String> lightData = dbHelper.getLatestSensorData("Sensor Cahaya");

        if (!rainData.isEmpty()) {
            tvSensorRainStatus.setText(rainData.get("status"));
            String currentStatus = rainData.get("status");

            if (currentStatus.equalsIgnoreCase("Hujan") && !isRaining) {
                isRaining = true;
                notificationDot.setVisibility(View.VISIBLE);

                if (!isLaundryOut) {
                    // Jemuran sudah di dalam — hanya beri tahu soal hujan
                    dbHelper.addLog("Peringatan Hujan!", "Sensor mendeteksi hujan. Jemuran sudah di dalam, aman.", "system", "rain");
                    showNotificationAlert("Peringatan: Hujan Terdeteksi!", "Jemuran sudah di dalam, aman.");
                    sendEmailNotification("RainSafe: Peringatan Hujan!",
                        "Halo, sistem RainSafe mendeteksi hujan. Jemuran Anda sudah di dalam, tidak ada tindakan diperlukan.");
                } else {
                    // Jemuran di luar — ESP32 sudah paksa auto mode & menarik jemuran
                    dbHelper.addLog("Peringatan Hujan!", "Sensor mendeteksi hujan. Jemuran sedang ditarik otomatis.", "system", "rain");
                    showNotificationAlert("Peringatan: Hujan Terdeteksi!", "Jemuran sedang ditarik otomatis.");
                    sendEmailNotification("RainSafe: Peringatan Hujan!",
                        "Halo, sistem RainSafe mendeteksi hujan. Jemuran Anda sedang ditarik secara otomatis untuk keamanan.");
                }
            } else if (currentStatus.equalsIgnoreCase("Aman") || currentStatus.equalsIgnoreCase("Cerah")) {
                isRaining = false;
            }
        }

        int lightVal = 0;
        if (!lightData.isEmpty()) {
            tvSensorLightStatus.setText(lightData.get("status"));
            try {
                lightVal = Integer.parseInt(lightData.get("value"));
            } catch (Exception e) {}
        }
        // Dryness Calculation
        try {
            double dryingPower = 15.0 + (lightVal * 0.04);

            if (isLaundryOut && !isRaining) {
                long elapsedMinutes = (System.currentTimeMillis() - laundryStartTime) / (1000 * 60);
                int dryness = (int) (elapsedMinutes * (dryingPower / 50.0));
                if (dryness >= 100) {
                    dryness = 100;
                    if (!isDryNotified) {
                        isDryNotified = true;
                        dbHelper.addLog("Jemuran Kering!", "Pakaian Anda sudah kering 100%. Silakan diambil.", "system", "dry");
                        showNotificationAlert("Jemuran Selesai!", "Pakaian Anda sudah kering 100%.");
                    }
                } else {
                    isDryNotified = false;
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }

        firebaseHelper.syncLogs();
    }

    // ─── UTILITIES ───────────────────────────────────────────────────────────

    private void sendEmailNotification(String title, String message) {
        String identifier = getIntent().getStringExtra("USER_IDENTIFIER");
        String loginType = getIntent().getStringExtra("LOGIN_TYPE");
        String targetEmail = "";

        if ("email".equals(loginType)) {
            targetEmail = identifier;
        } else if (identifier != null) {
            Map<String, String> userData = dbHelper.getUserDataByPhone(identifier);
            if (userData != null && userData.containsKey("email")) {
                targetEmail = userData.get("email");
            }
        }

        if (targetEmail != null && !targetEmail.isEmpty()) {
            android.util.Log.d("RainSafe_Email", "Mengirim email ke: " + targetEmail);
            android.widget.Toast.makeText(this, "Email dikirim ke: " + targetEmail, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotificationAlert(String title, String message) {
        notificationDot.setVisibility(View.VISIBLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1004);
                return;
            }
        }

        android.content.Intent intent = new android.content.Intent(this, NotificationsActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, "RAINSAFE_NOTIF")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "RAINSAFE_NOTIF", "RainSafe Alerts",
                    android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for rain and sensor alerts");
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        android.widget.Toast.makeText(this, title + "\n" + message, android.widget.Toast.LENGTH_LONG).show();
    }

    private void setAutoMode(boolean enabled) {
        // Guard: block Firebase echo untuk 2 detik
        isManualCommand = true;
        handler.postDelayed(() -> isManualCommand = false, 2000);
        getSharedPreferences("RainSafePrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("auto_mode", enabled)
                .apply();
        firebaseHelper.updateAutoMode(enabled);
        if (swAutoMode != null && swAutoMode.isChecked() != enabled) {
            isAutoModeSwitchUpdating = true;
            swAutoMode.setChecked(enabled);
            isAutoModeSwitchUpdating = false;
        }
        updateSensorUI();
    }

    // ─── CONNECTION STATUS ────────────────────────────────────────────────────

    /**
     * Mendengarkan status koneksi Firebase secara real-time via ".info/connected".
     * Jika tersambung: dot hijau + teks "Terhubung".
     * Jika terputus: dot merah + teks "Terputus".
     */
    private void startListeningConnection() {
        connectivityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                runOnUiThread(() -> {
                    if (ivStatusDot != null) {
                        ivStatusDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                connected
                                    ? android.graphics.Color.parseColor("#4CAF50")
                                    : android.graphics.Color.parseColor("#F44336")
                            )
                        );
                    }
                    if (tvStatusText != null) {
                        tvStatusText.setText(connected
                            ? getString(R.string.dash_status_connected)
                            : getString(R.string.dash_status_disconnected));
                        tvStatusText.setTextColor(connected
                            ? ContextCompat.getColor(DashboardActivity.this, R.color.button_blue)
                            : android.graphics.Color.parseColor("#F44336"));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                android.util.Log.e("RainSafe", "Connectivity listener error: " + error.getMessage());
            }
        };

        FirebaseDatabase.getInstance(
            "https://rainsafe-777f2-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference(".info/connected").addValueEventListener(connectivityListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        weatherExecutor.shutdown();
        // Remove connectivity listener
        if (connectivityListener != null) {
            FirebaseDatabase.getInstance(
                "https://rainsafe-777f2-default-rtdb.asia-southeast1.firebasedatabase.app/"
            ).getReference(".info/connected").removeEventListener(connectivityListener);
        }
    }

    private void updateNavUI(int position) {
        int grey = ContextCompat.getColor(this, R.color.text_grey);
        int blue = ContextCompat.getColor(this, R.color.button_blue);

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