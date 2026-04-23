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

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;
    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        // Initialize Profile Views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);

        // Load User Data
        loadUserData();

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

        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
        });

        // Set Profile as Active (Position 3)
        updateNavUI(3);

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

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navProfile.setOnClickListener(v -> updateNavUI(3));
        
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void loadUserData() {
        String identifier = getIntent().getStringExtra("USER_IDENTIFIER");
        String loginType = getIntent().getStringExtra("LOGIN_TYPE");

        if (identifier != null && loginType != null) {
            java.util.Map<String, String> userData;
            if (loginType.equals("email")) {
                userData = dbHelper.getUserData(identifier);
            } else {
                userData = dbHelper.getUserDataByPhone(identifier);
            }

            if (!userData.isEmpty()) {
                tvProfileName.setText(userData.get("fullname"));
                tvProfileEmail.setText(userData.get("email"));
                if (tvProfilePhone != null) {
                    tvProfilePhone.setText(userData.get("phone"));
                }
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