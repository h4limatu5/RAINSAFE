package com.example.rainsafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    // Settings Elements
    private SwitchCompat swRainSensor, swLightSensor, swNotifRain, swNotifLaundry, swNotifError, swDarkMode;
    private RelativeLayout rlChangePassword;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RainSafePrefs";

    private FirebaseSyncHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        firebaseHelper = new FirebaseSyncHelper(this);
        // Edge-to-edge handling
        EdgeToEdge.enable(this);
// Removed duplicate setContentView call (handled earlier)
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
        swRainSensor = findViewById(R.id.swRainSensor);
        swLightSensor = findViewById(R.id.swLightSensor);
        swNotifRain = findViewById(R.id.swNotifRain);
        swNotifLaundry = findViewById(R.id.swNotifLaundry);
        swNotifError = findViewById(R.id.swNotifError);
        swDarkMode = findViewById(R.id.swDarkMode);
        rlChangePassword = findViewById(R.id.rlChangePassword);
    }

    private void loadSettings() {
        swRainSensor.setChecked(sharedPreferences.getBoolean("rain_sensor", true));
        swLightSensor.setChecked(sharedPreferences.getBoolean("light_sensor", true));
        swNotifRain.setChecked(sharedPreferences.getBoolean("notif_rain", true));
        swNotifLaundry.setChecked(sharedPreferences.getBoolean("notif_laundry", true));
        swNotifError.setChecked(sharedPreferences.getBoolean("notif_error", true));
        swDarkMode.setChecked(sharedPreferences.getBoolean("dark_mode", false));
    }

    private void setupSettingsListeners() {
        swRainSensor.setOnCheckedChangeListener((v, isChecked) -> saveSetting("rain_sensor", isChecked));
        swLightSensor.setOnCheckedChangeListener((v, isChecked) -> saveSetting("light_sensor", isChecked));
        swNotifRain.setOnCheckedChangeListener((v, isChecked) -> saveSetting("notif_rain", isChecked));
        swNotifLaundry.setOnCheckedChangeListener((v, isChecked) -> saveSetting("notif_laundry", isChecked));
        swNotifError.setOnCheckedChangeListener((v, isChecked) -> saveSetting("notif_error", isChecked));

        swDarkMode.setOnCheckedChangeListener((v, isChecked) -> {
            if (v.isPressed()) {
                saveSetting("dark_mode", isChecked);
                v.postDelayed(() -> {
                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                }, 150);
            }
        });

        rlChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
        });
    }

    private void saveSetting(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
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