package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.rainsafe.data.AppDatabase;
import com.example.rainsafe.data.entity.Device;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchModeOtomatis, switchModeMalam, switchSensorHujan, switchSensorCahaya;
    private SwitchCompat switchNotifHujan, switchNotifJemuran, switchNotifError, switchDarkMode;
    private TextView tvSensitivitasHujan, tvDelayRespon, tvDeviceStatus, tvBahasa;
    
    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = AppDatabase.getInstance(this);
        initViews();
        setupNavigation();
        observeDeviceData();
        setupListeners();
    }

    private void initViews() {
        switchModeOtomatis = findViewById(R.id.switchModeOtomatis);
        switchModeMalam = findViewById(R.id.switchModeMalam);
        switchSensorHujan = findViewById(R.id.switchSensorHujan);
        switchSensorCahaya = findViewById(R.id.switchSensorCahaya);
        switchNotifHujan = findViewById(R.id.switchNotifHujan);
        switchNotifJemuran = findViewById(R.id.switchNotifJemuran);
        switchNotifError = findViewById(R.id.switchNotifError);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        tvSensitivitasHujan = findViewById(R.id.tvSensitivitasHujan);
        tvDelayRespon = findViewById(R.id.tvDelayRespon);
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus);
        tvBahasa = findViewById(R.id.tvBahasa);
    }

    private void observeDeviceData() {
        db.deviceDao().getDeviceLiveData(1).observe(this, device -> {
            if (device != null) {
                updateUI(device);
            }
        });
    }

    private void updateUI(Device device) {
        switchModeOtomatis.setChecked(device.isAutomationActive());
        switchModeMalam.setChecked(device.isNightMode());
        switchSensorHujan.setChecked(device.isRainSensorActive());
        switchSensorCahaya.setChecked(device.isLightSensorActive());
        
        tvSensitivitasHujan.setText(device.getRainSensitivity());
        tvDelayRespon.setText(device.getResponseDelay() + " detik");
        tvDeviceStatus.setText("• " + device.getStatus());
    }

    private void setupListeners() {
        switchModeOtomatis.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) updateDeviceField("automationActive", isChecked);
        });
        
        switchModeMalam.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) updateDeviceField("nightMode", isChecked);
        });

        switchSensorHujan.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) updateDeviceField("rainSensorActive", isChecked);
        });

        switchSensorCahaya.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) updateDeviceField("lightSensorActive", isChecked);
        });
    }

    private void updateDeviceField(String field, boolean value) {
        executorService.execute(() -> {
            Device device = db.deviceDao().getDeviceById(1);
            if (device != null) {
                switch (field) {
                    case "automationActive": device.setAutomationActive(value); break;
                    case "nightMode": device.setNightMode(value); break;
                    case "rainSensorActive": device.setRainSensorActive(value); break;
                    case "lightSensorActive": device.setLightSensorActive(value); break;
                }
                db.deviceDao().update(device);
            }
        });
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
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}