package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rainsafe.data.AppDatabase;
import com.example.rainsafe.data.entity.Device;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvDeviceName, tvStatus, tvLocation, tvFirmware;
    private TextView tvTotalUsage, tvLastUsed, tvLastStatus;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getInstance(this);
        initViews();
        setupNavigation();
        observeRealtimeData();
    }

    private void initViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvDeviceName = findViewById(R.id.tvDeviceName); // Saya perlu menambahkan ID ini di XML
        tvStatus = findViewById(R.id.tvStatus); // Saya perlu menambahkan ID ini di XML
        tvLocation = findViewById(R.id.tvLocation); // Saya perlu menambahkan ID ini di XML
        tvFirmware = findViewById(R.id.tvFirmware); // Saya perlu menambahkan ID ini di XML
        
        tvTotalUsage = findViewById(R.id.tvTotalUsage); // Saya perlu menambahkan ID ini di XML
        tvLastUsed = findViewById(R.id.tvLastUsed); // Saya perlu menambahkan ID ini di XML
        tvLastStatus = findViewById(R.id.tvLastStatus); // Saya perlu menambahkan ID ini di XML
    }

    private void observeRealtimeData() {
        // Observe Device data (Status Online, Nama, Firmware)
        db.deviceDao().getDeviceLiveData(1).observe(this, device -> {
            if (device != null) {
                updateDeviceUI(device);
            }
        });

        // Simulasi User data (karena kita belum simpan ID user yang login)
        // Dalam real-app, gunakan SharedPreferences atau Session Manager untuk ambil ID user
        executorService().execute(() -> {
            // Kita ambil user pertama untuk contoh
            // User user = db.userDao().getUserById(1); 
            // runOnUiThread(() -> { if(user != null) updateUserUI(user); });
        });
    }

    private void updateDeviceUI(Device device) {
        if (tvDeviceName != null) tvDeviceName.setText(device.getName());
        if (tvStatus != null) tvStatus.setText("• " + device.getStatus());
        if (tvLocation != null) tvLocation.setText(device.getLocation());
        if (tvFirmware != null) tvFirmware.setText(device.getFirmware());
        
        if (tvLastStatus != null) {
            tvLastStatus.setText(device.isClosed() ? "Jemuran Di Dalam" : "Jemuran Di Luar");
        }
    }

    private void setupNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        findViewById(R.id.navHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            finish();
        });
        findViewById(R.id.navSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });
    }
    
    private java.util.concurrent.Executor executorService() {
        return java.util.concurrent.Executors.newSingleThreadExecutor();
    }
}