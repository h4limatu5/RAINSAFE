package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rainsafe.data.AppDatabase;
import com.example.rainsafe.data.entity.Device;
import com.example.rainsafe.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvDeviceName, tvStatus, tvLocation, tvFirmware;
    private TextView tvTotalUsage, tvLastUsed, tvLastStatus;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvStatus = findViewById(R.id.tvStatus);
        tvLocation = findViewById(R.id.tvLocation);
        tvFirmware = findViewById(R.id.tvFirmware);
        
        tvTotalUsage = findViewById(R.id.tvTotalUsage);
        tvLastUsed = findViewById(R.id.tvLastUsed);
        tvLastStatus = findViewById(R.id.tvLastStatus);
    }

    private void observeRealtimeData() {
        // Observe Device data (Status Online, Nama, Firmware)
        db.deviceDao().getDeviceLiveData(1).observe(this, device -> {
            if (device != null) {
                updateDeviceUI(device);
            }
        });

        // Load User data
        executorService.execute(() -> {
            // Asumsi user dengan ID 1 adalah user yang sedang login (demo purposes)
            User user = db.userDao().getUserById(1); 
            if (user != null) {
                runOnUiThread(() -> updateUserUI(user));
            }
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

    private void updateUserUI(User user) {
        if (tvProfileName != null) tvProfileName.setText(user.getFullName());
        if (tvProfileEmail != null) tvProfileEmail.setText(user.getEmail());
        if (tvTotalUsage != null) tvTotalUsage.setText(user.getTotalUsageHours() + " jam");
        if (tvLastUsed != null) tvLastUsed.setText(user.getLastUsed());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}