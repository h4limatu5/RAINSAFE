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

public class HistoryActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;
    
    private TextView filterAll, filterLaundry, filterRain, filterSensor;
    private TextView headerToday, headerYesterday, header2DaysAgo;
    private CardView itemLaundry1, itemLaundry2, itemLaundry3, itemLaundry4, itemRain1, itemSensor1;
    private LinearLayout llHiddenHistory;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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

        // Initialize Filter Buttons
        filterAll = findViewById(R.id.filterAll);
        filterLaundry = findViewById(R.id.filterLaundry);
        filterRain = findViewById(R.id.filterRain);
        filterSensor = findViewById(R.id.filterSensor);

        // Initialize Headers
        headerToday = findViewById(R.id.headerToday);
        headerYesterday = findViewById(R.id.headerYesterday);
        header2DaysAgo = findViewById(R.id.header2DaysAgo);

        // Initialize Items
        itemLaundry1 = findViewById(R.id.itemLaundry1);
        itemLaundry2 = findViewById(R.id.itemLaundry2);
        itemLaundry3 = findViewById(R.id.itemLaundry3);
        itemLaundry4 = findViewById(R.id.itemLaundry4);
        itemRain1 = findViewById(R.id.itemRain1);
        itemSensor1 = findViewById(R.id.itemSensor1);

        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
        });

        // Set History as Active (Position 1)
        updateNavUI(1);

        // Navigation Listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        
        navHistory.setOnClickListener(v -> updateNavUI(1));

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

        // Load More Logic
        llHiddenHistory = findViewById(R.id.llHiddenHistory);
        findViewById(R.id.btnLoadMore).setOnClickListener(v -> {
            llHiddenHistory.setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);
            applyFilter();
        });

        // Filter Selection Logic
        View.OnClickListener filterClickListener = v -> {
            // Reset all filters to inactive style
            resetFilterUI(filterAll, filterLaundry, filterRain, filterSensor);
            
            // Set clicked filter to active style
            v.setBackgroundResource(R.drawable.button_background);
            ((TextView) v).setTextColor(ContextCompat.getColor(this, R.color.white));
            ((TextView) v).setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Update current filter state
            int id = v.getId();
            if (id == R.id.filterAll) currentFilter = "all";
            else if (id == R.id.filterLaundry) currentFilter = "laundry";
            else if (id == R.id.filterRain) currentFilter = "rain";
            else if (id == R.id.filterSensor) currentFilter = "sensor";
            
            applyFilter();
        };

        filterAll.setOnClickListener(filterClickListener);
        filterLaundry.setOnClickListener(filterClickListener);
        filterRain.setOnClickListener(filterClickListener);
        filterSensor.setOnClickListener(filterClickListener);

        // Initial filter application to sync visibility
        applyFilter();
    }

    private void applyFilter() {
        boolean showLaundry = currentFilter.equals("all") || currentFilter.equals("laundry");
        boolean showRain = currentFilter.equals("all") || currentFilter.equals("rain");
        boolean showSensor = currentFilter.equals("all") || currentFilter.equals("sensor");

        // Laundry Items
        itemLaundry1.setVisibility(showLaundry ? View.VISIBLE : View.GONE);
        itemLaundry2.setVisibility(showLaundry ? View.VISIBLE : View.GONE);
        itemLaundry3.setVisibility(showLaundry ? View.VISIBLE : View.GONE);
        itemLaundry4.setVisibility(showLaundry ? View.VISIBLE : View.GONE);

        // Rain Items
        itemRain1.setVisibility(showRain ? View.VISIBLE : View.GONE);

        // Sensor Items
        itemSensor1.setVisibility(showSensor ? View.VISIBLE : View.GONE);

        // Update Headers Visibility
        updateHeaderVisibility();
    }

    private void updateHeaderVisibility() {
        // Today header: Only laundry1 is today
        headerToday.setVisibility(itemLaundry1.getVisibility());

        // Yesterday header: Rain1, Laundry2, Laundry3 are yesterday
        boolean yesterdayHasItems = itemRain1.getVisibility() == View.VISIBLE || 
                                   itemLaundry2.getVisibility() == View.VISIBLE || 
                                   itemLaundry3.getVisibility() == View.VISIBLE;
        headerYesterday.setVisibility(yesterdayHasItems ? View.VISIBLE : View.GONE);

        // 2 Days Ago header: Sensor1, Laundry4
        boolean twoDaysAgoHasItems = itemSensor1.getVisibility() == View.VISIBLE || 
                                    itemLaundry4.getVisibility() == View.VISIBLE;
        header2DaysAgo.setVisibility(twoDaysAgoHasItems ? View.VISIBLE : View.GONE);
    }

    private void resetFilterUI(TextView... filters) {
        for (TextView tv : filters) {
            tv.setBackgroundResource(R.drawable.button_white_bg);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
            tv.setTypeface(null, android.graphics.Typeface.NORMAL);
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