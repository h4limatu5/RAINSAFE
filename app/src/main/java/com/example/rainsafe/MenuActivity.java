package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        String identifier = getIntent().getStringExtra("USER_IDENTIFIER");
        String loginType = getIntent().getStringExtra("LOGIN_TYPE");

        findViewById(R.id.menuHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_IDENTIFIER", identifier);
            intent.putExtra("LOGIN_TYPE", loginType);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.menuHistory).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("USER_IDENTIFIER", identifier);
            intent.putExtra("LOGIN_TYPE", loginType);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.menuSettings).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_IDENTIFIER", identifier);
            intent.putExtra("LOGIN_TYPE", loginType);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.menuProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_IDENTIFIER", identifier);
            intent.putExtra("LOGIN_TYPE", loginType);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.menuLogout).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}