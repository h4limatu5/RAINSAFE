package com.example.rainsafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.os.LocaleListCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    // Settings Elements
    private SwitchCompat swAutoMode, swNightMode, swRainSensor, swLightSensor, swNotifRain, swNotifLaundry, swNotifError, swDarkMode;
    private RelativeLayout rlChangeWifi, rlResetDevice, rlChangePassword, rlLogoutAll, rlRainSensitivity, rlResponseDelay, rlCalibration, rlLanguage;
    private TextView tvRainSensitivity, tvResponseDelay, tvLanguage;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RainSafePrefs";

    private ImageView ivAutoSectionIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

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

        // Settings Initialization
        initSettingsViews();
        loadSettings();
        setupSettingsListeners();

        // btnBack is just the settings icon in this new layout, but we can make it go back
        findViewById(R.id.headerLayout).setOnClickListener(v -> finish());

        // Set Settings as Active (Position 2)
        updateNavUI(2);

        // Navigation Listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        
        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navSettings.setOnClickListener(v -> updateNavUI(2));

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void initSettingsViews() {
        swAutoMode = findViewById(R.id.swAutoMode);
        swNightMode = findViewById(R.id.swNightMode);
        swRainSensor = findViewById(R.id.swRainSensor);
        swLightSensor = findViewById(R.id.swLightSensor);
        swNotifRain = findViewById(R.id.swNotifRain);
        swNotifLaundry = findViewById(R.id.swNotifLaundry);
        swNotifError = findViewById(R.id.swNotifError);
        swDarkMode = findViewById(R.id.swDarkMode);

        rlChangeWifi = findViewById(R.id.rlChangeWifi);
        rlResetDevice = findViewById(R.id.rlResetDevice);
        rlChangePassword = findViewById(R.id.rlChangePassword);
        rlLogoutAll = findViewById(R.id.rlLogoutAll);
        rlRainSensitivity = findViewById(R.id.rlRainSensitivity);
        rlResponseDelay = findViewById(R.id.rlResponseDelay);
        rlCalibration = findViewById(R.id.rlCalibration);
        rlLanguage = findViewById(R.id.rlLanguage);

        tvRainSensitivity = findViewById(R.id.tvRainSensitivity);
        tvResponseDelay = findViewById(R.id.tvResponseDelay);
        tvLanguage = findViewById(R.id.tvLanguage);
        ivAutoSectionIcon = findViewById(R.id.ivAutoSectionIcon);
    }

    private void loadSettings() {
        boolean isAutoMode = sharedPreferences.getBoolean("auto_mode", true);
        swAutoMode.setChecked(isAutoMode);
        updateAutoIconState(isAutoMode);
        
        swNightMode.setChecked(sharedPreferences.getBoolean("night_mode", false));
        swRainSensor.setChecked(sharedPreferences.getBoolean("rain_sensor", true));
        swLightSensor.setChecked(sharedPreferences.getBoolean("light_sensor", true));
        swNotifRain.setChecked(sharedPreferences.getBoolean("notif_rain", true));
        swNotifLaundry.setChecked(sharedPreferences.getBoolean("notif_laundry", true));
        swNotifError.setChecked(sharedPreferences.getBoolean("notif_error", true));
        swDarkMode.setChecked(sharedPreferences.getBoolean("dark_mode", false));

        tvRainSensitivity.setText(sharedPreferences.getString("rain_sensitivity", "Medium"));
        tvResponseDelay.setText(sharedPreferences.getString("response_delay", "5 detik"));
        tvLanguage.setText(sharedPreferences.getString("language", "Indonesia"));
    }

    private void setupSettingsListeners() {
        swAutoMode.setOnCheckedChangeListener((v, isChecked) -> {
            saveSetting("auto_mode", isChecked);
            updateAutoIconState(isChecked);
        });
        swNightMode.setOnCheckedChangeListener((v, isChecked) -> saveSetting("night_mode", isChecked));
        swRainSensor.setOnCheckedChangeListener((v, isChecked) -> saveSetting("rain_sensor", isChecked));
        swLightSensor.setOnCheckedChangeListener((v, isChecked) -> saveSetting("light_sensor", isChecked));
        swNotifRain.setOnCheckedChangeListener((v, isChecked) -> saveSetting("notif_rain", isChecked));
        swNotifLaundry.setOnCheckedChangeListener((v, isChecked) -> saveSetting("notif_laundry", isChecked));
        swNotifError.setOnCheckedChangeListener((v, isChecked) -> saveSetting("notif_error", isChecked));
        
        swDarkMode.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                saveSetting("dark_mode", isChecked);
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

        rlChangeWifi.setOnClickListener(v -> Toast.makeText(this, "Fitur Ganti WiFi akan segera hadir", Toast.LENGTH_SHORT).show());
        rlResetDevice.setOnClickListener(v -> Toast.makeText(this, "Mereset perangkat...", Toast.LENGTH_SHORT).show());
        rlChangePassword.setOnClickListener(v -> Toast.makeText(this, "Fitur Ganti Password akan segera hadir", Toast.LENGTH_SHORT).show());
        
        rlLogoutAll.setOnClickListener(v -> {
            Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        rlRainSensitivity.setOnClickListener(v -> Toast.makeText(this, "Pengaturan Sensitivitas", Toast.LENGTH_SHORT).show());
        rlResponseDelay.setOnClickListener(v -> Toast.makeText(this, "Pengaturan Delay", Toast.LENGTH_SHORT).show());
        rlCalibration.setOnClickListener(v -> Toast.makeText(this, "Memulai Kalibrasi...", Toast.LENGTH_SHORT).show());
        rlLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        String[] languages = {"Indonesia", "English"};
        String[] languageCodes = {"in", "en"};
        int checkedItem = 0; // Default to Indonesia
        
        String currentLang = sharedPreferences.getString("language_code", "in");
        if ("en".equals(currentLang)) {
            checkedItem = 1;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pilih Bahasa")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLanguage = languages[which];
                    String selectedCode = languageCodes[which];
                    
                    tvLanguage.setText(selectedLanguage);
                    saveSetting("language", selectedLanguage);
                    saveSetting("language_code", selectedCode);
                    
                    // Apply language change using AppCompatDelegate
                    LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(selectedCode);
                    AppCompatDelegate.setApplicationLocales(appLocale);

                    Toast.makeText(this, "Bahasa diubah ke " + selectedLanguage, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void saveSetting(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    private void saveSetting(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    private void updateAutoIconState(boolean isActive) {
        if (ivAutoSectionIcon != null) {
            if (isActive) {
                ivAutoSectionIcon.setColorFilter(ContextCompat.getColor(this, R.color.button_blue));
            } else {
                ivAutoSectionIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_grey));
            }
        }
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