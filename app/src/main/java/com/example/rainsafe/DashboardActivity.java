package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

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
    private TextView tvDeviceStatus, tvCoverStatus, tvDrynessText, tvTotalUsageToday, tvRainCount;
    private ProgressBar pbDryness;
    private ImageView ivWeatherIcon, ivStatusIcon;
    private SwitchCompat switchAutomation;
    private Button btnMasukan, btnKeluarkan;
    private LinearLayout navHistory, navSettings, navProfile;
    
    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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
            if (buttonView.isPressed()) { // Hanya trigger jika diubah oleh user
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
        tvRainCount = findViewById(R.id.tvRainCount);
        
        pbDryness = findViewById(R.id.pbDryness);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        // ivStatusIcon di layout baru mungkin sama dengan ivWeatherIcon atau berada di card status
        // Di XML dashboard saya menaruh ivWeatherIcon di bagian cuaca.
        
        switchAutomation = findViewById(R.id.switchAutomation);
        btnMasukan = findViewById(R.id.btnMasukan);
        btnKeluarkan = findViewById(R.id.btnKeluarkan);
        
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
    }

    private void observeDeviceData() {
        // Observe data dari DB secara real-time
        db.deviceDao().getDeviceLiveData(1).observe(this, device -> {
            if (device != null) {
                updateUI(device);
            }
        });
    }

    private void updateUI(Device device) {
        // Update Cuaca
        tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", device.getTemperature()));
        tvHumidity.setText("Kelembaban\n" + device.getHumidity() + "%");
        tvWindSpeed.setText("Angin\n" + device.getWindSpeed() + " km/h");
        tvRainProbability.setText("Hujan\n" + device.getRainProbability() + "%");

        // Update Status Atap
        if (device.isClosed()) {
            tvDeviceStatus.setText("Jemuran Aman");
            tvCoverStatus.setText("DI DALAM");
            tvCoverStatus.setBackgroundResource(R.drawable.bg_rounded_blue); // Gunakan warna biru/merah muda
            tvCoverStatus.setTextColor(getResources().getColor(R.color.white));
        } else {
            tvDeviceStatus.setText("Jemuran Basah");
            tvCoverStatus.setText("DI LUAR");
            tvCoverStatus.setBackgroundColor(0xFFE8F5E9); // Hijau muda
            tvCoverStatus.setTextColor(getResources().getColor(R.color.green_status));
        }

        // Update Progress Kering
        pbDryness.setProgress(device.getDrynessPercentage());
        tvDrynessText.setText(device.getDrynessPercentage() + "%");
        
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
}