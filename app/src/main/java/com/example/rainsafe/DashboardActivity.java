package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.example.rainsafe.data.AppDatabase;
import com.example.rainsafe.data.entity.Device;
import com.example.rainsafe.data.entity.History;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvTemperature, tvWeatherDesc, tvHumidity, tvWindSpeed, tvRainProbability;
    private TextView tvDeviceStatus, tvCoverStatus, tvDrynessText, tvTotalUsageToday, tvRainCountLarge;
    private TextView tvAvgHumidity;
    private ProgressBar pbDryness, pbAvgHumidity;
    private SwitchCompat switchAutomation;
    private Button btnMasukan, btnKeluarkan;
    
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = AppDatabase.getInstance(this);
        initViews();
        setupNavigation();
        observeDeviceData();

        btnMasukan.setOnClickListener(v -> updateCoverStatus(true));
        btnKeluarkan.setOnClickListener(v -> updateCoverStatus(false));
        
        switchAutomation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { 
                executorService.execute(() -> {
                    Device device = db.deviceDao().getDeviceById(1);
                    if (device != null) {
                        device.setAutomationActive(isChecked);
                        db.deviceDao().update(device);
                    }
                });
            }
        });
    }

    private void initViews() {
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        tvRainProbability = findViewById(R.id.tvRainProbability);
        
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus);
        tvCoverStatus = findViewById(R.id.tvCoverStatus);
        tvDrynessText = findViewById(R.id.tvDrynessText);
        
        tvTotalUsageToday = findViewById(R.id.tvTotalUsageToday);
        tvRainCountLarge = findViewById(R.id.tvRainCountLarge);
        tvAvgHumidity = findViewById(R.id.tvAvgHumidity);
        
        pbDryness = findViewById(R.id.pbDryness);
        pbAvgHumidity = findViewById(R.id.pbAvgHumidity);
        
        switchAutomation = findViewById(R.id.switchAutomation);
        btnMasukan = findViewById(R.id.btnMasukan);
        btnKeluarkan = findViewById(R.id.btnKeluarkan);
    }

    private void observeDeviceData() {
        db.deviceDao().getDeviceLiveData(1).observe(this, device -> {
            if (device != null) {
                updateUI(device);
            }
        });
    }

    private void updateUI(Device device) {
        // Update Cuaca
        tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", device.getTemperature()));
        tvWeatherDesc.setText(getString(R.string.default_weather_desc));
        tvHumidity.setText(getString(R.string.humidity_label, device.getHumidity()));
        tvWindSpeed.setText(getString(R.string.wind_speed_label, device.getWindSpeed()));
        tvRainProbability.setText(getString(R.string.rain_prob_label, device.getRainProbability()));

        // Update Status Atap
        if (device.isClosed()) {
            tvDeviceStatus.setText(getString(R.string.status_safe));
            tvCoverStatus.setText(getString(R.string.location_inside));
            tvCoverStatus.setBackgroundResource(R.drawable.bg_rounded_blue);
            tvCoverStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            tvDeviceStatus.setText(getString(R.string.status_wet));
            tvCoverStatus.setText(getString(R.string.location_outside));
            tvCoverStatus.setBackgroundResource(R.drawable.bg_rounded_light_blue);
            tvCoverStatus.setTextColor(ContextCompat.getColor(this, R.color.green_status));
        }

        // Update Progress Kering
        pbDryness.setProgress(device.getDrynessPercentage());
        tvDrynessText.setText(String.format(Locale.getDefault(), "%d%%", device.getDrynessPercentage()));
        
        // Update Statistik sesuai gambar
        tvTotalUsageToday.setText("6 jam 10 menit"); 
        tvAvgHumidity.setText(String.format(Locale.getDefault(), "%d%%", device.getHumidity() - 3));
        pbAvgHumidity.setProgress(device.getHumidity() - 3);
        tvRainCountLarge.setText(getString(R.string.rain_count_label, 0).replace("Hujan: ", ""));
        
        // Update Toggle
        switchAutomation.setChecked(device.isAutomationActive());
    }

    private void updateCoverStatus(boolean isClosed) {
        executorService.execute(() -> {
            Device device = db.deviceDao().getDeviceById(1);
            if (device != null) {
                device.setClosed(isClosed);
                db.deviceDao().update(device);
                
                String title = isClosed ? "Jemuran Dimasukkan" : "Jemuran Dikeluarkan";
                String desc = isClosed ? "Atap ditutup manual melalui Dashboard." : "Atap dibuka manual melalui Dashboard.";
                saveHistory(title, desc);
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.navHistory).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.navSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void saveHistory(String title, String desc) {
        executorService.execute(() -> {
            String currentTime = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
            History history = new History(title, desc, currentTime, "Manual");
            db.historyDao().insert(history);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}