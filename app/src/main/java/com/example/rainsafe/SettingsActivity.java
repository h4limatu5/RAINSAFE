package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private CardView activeIndicator;
    private View navCurve;
    private ImageView ivActiveIcon;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        // Set Settings as Active (Position 2)
        activeIndicator.post(() -> moveIndicator(2));

        // Navigation Listeners
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        
        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navSettings.setOnClickListener(v -> moveIndicator(2));

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
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

        switch (position) {
            case 2:
                ivActiveIcon.setImageResource(android.R.drawable.ic_menu_preferences);
                ivSettings.setVisibility(View.GONE);
                tvSettings.setTextColor(blue);
                tvSettings.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }
}