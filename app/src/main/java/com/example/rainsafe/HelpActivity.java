package com.example.rainsafe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEmailSupport).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@rainsafe.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "RainSafe Support Request");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Tidak ada aplikasi email ditemukan", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnPhoneSupport).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+6281234567890"));
            startActivity(intent);
        });
    }
}