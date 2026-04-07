package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private int currentPage = 0;
    private final int MAX_PAGES = 3;

    private ImageView ivFeatureIcon;
    private TextView tvFeatureTitle;
    private TextView tvFeatureDesc;
    private View indicator1, indicator2, indicator3, indicator4;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ivFeatureIcon = findViewById(R.id.ivFeatureIcon);
        tvFeatureTitle = findViewById(R.id.tvFeatureTitle);
        tvFeatureDesc = findViewById(R.id.tvFeatureDesc);
        
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);
        indicator4 = findViewById(R.id.indicator4);

        Button btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < MAX_PAGES) {
                    currentPage++;
                    updatePageContent();
                } else {
                    navigateToLogin();
                }
            }
        });
    }

    private void updatePageContent() {
        View[] viewsToAnimate = {ivFeatureIcon, tvFeatureTitle, tvFeatureDesc};
        for (View v : viewsToAnimate) {
            v.setAlpha(0f);
        }

        switch (currentPage) {
            case 0:
                ivFeatureIcon.setImageResource(R.drawable.download_removebg_preview);
                tvFeatureTitle.setText("Deteksi Hujan Cerdas");
                tvFeatureDesc.setText("Sensor otomatis masukkan jemuran saat hujan terdeteksi");
                updateIndicators(indicator1);
                break;
            case 1:
                ivFeatureIcon.setImageResource(R.drawable.download__3__removebg_preview);
                tvFeatureTitle.setText("Kontrol Jarak Jauh");
                tvFeatureDesc.setText("Kendalikan jemuran Anda dari mana saja melalui smartphone");
                updateIndicators(indicator2);
                break;
            case 2:
                ivFeatureIcon.setImageResource(R.drawable.download__4__removebg_preview);
                tvFeatureTitle.setText("Notifikasi Real-time");
                tvFeatureDesc.setText("Dapatkan pemberitahuan instan saat cuaca berubah");
                updateIndicators(indicator3);
                break;
            case 3:
                ivFeatureIcon.setImageResource(R.drawable.screenshot_2026_03_21_201332_removebg_preview);
                tvFeatureTitle.setText("Mulai Sekarang");
                tvFeatureDesc.setText("Bergabunglah dengan RainSafe untuk laundry yang lebih cerdas");
                btnNext.setText("Get Started");
                updateIndicators(indicator4);
                break;
        }

        for (View v : viewsToAnimate) {
            v.animate().alpha(1f).setDuration(300).start();
        }
    }

    private void updateIndicators(View activeIndicator) {
        View[] indicators = {indicator1, indicator2, indicator3, indicator4};
        
        for (View view : indicators) {
            android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
            if (view == activeIndicator) {
                view.setBackgroundResource(R.drawable.indicator_active);
                view.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6AB7E2")));
                params.width = (int) (24 * getResources().getDisplayMetrics().density);
            } else {
                view.setBackgroundResource(R.drawable.indicator_inactive);
                view.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D1E4F3")));
                params.width = (int) (6 * getResources().getDisplayMetrics().density);
            }
            view.setLayoutParams(params);
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        finish();
    }
}
