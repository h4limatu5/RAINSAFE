package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.rainsafe.data.AppDatabase;
import com.example.rainsafe.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etPhone, etOTP;
    private LinearLayout formEmail, formPhone;
    private View indicatorEmail, indicatorPhone;
    private TextView tvEmailTab, tvPhoneTab;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isEmailLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        initViews();
        setupTabListeners();

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView btnSendOTP = findViewById(R.id.btnSendOTP);

        btnLogin.setOnClickListener(v -> {
            if (isEmailLogin) {
                handleEmailLogin();
            } else {
                handlePhoneLogin();
            }
        });

        btnSendOTP.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Masukkan nomor telepon", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.msg_otp_sent, phone), Toast.LENGTH_SHORT).show();
                etOTP.setText("123456"); // Auto-fill untuk demo
            }
        });

        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etOTP = findViewById(R.id.etOTP);
        
        formEmail = findViewById(R.id.formEmail);
        formPhone = findViewById(R.id.formPhone);
        
        indicatorEmail = findViewById(R.id.indicatorEmail);
        indicatorPhone = findViewById(R.id.indicatorPhone);
        
        tvEmailTab = findViewById(R.id.tvEmailTab);
        tvPhoneTab = findViewById(R.id.tvPhoneTab);
    }

    private void setupTabListeners() {
        findViewById(R.id.tabEmail).setOnClickListener(v -> switchTab(true));
        findViewById(R.id.tabPhone).setOnClickListener(v -> switchTab(false));
    }

    private void switchTab(boolean emailSelected) {
        isEmailLogin = emailSelected;
        if (emailSelected) {
            formEmail.setVisibility(View.VISIBLE);
            formPhone.setVisibility(View.GONE);
            indicatorEmail.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue));
            indicatorPhone.setBackgroundColor(0xFFEEEEEE);
            tvEmailTab.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
            tvPhoneTab.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        } else {
            formEmail.setVisibility(View.GONE);
            formPhone.setVisibility(View.VISIBLE);
            indicatorEmail.setBackgroundColor(0xFFEEEEEE);
            indicatorPhone.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue));
            tvEmailTab.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            tvPhoneTab.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        }
    }

    private void handleEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.err_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User user = db.userDao().login(email, password);
            runOnUiThread(() -> {
                if (user != null) {
                    navigateToDashboard();
                } else {
                    Toast.makeText(this, getString(R.string.err_invalid_login), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void handlePhoneLogin() {
        String phone = etPhone.getText().toString().trim();
        String otp = etOTP.getText().toString().trim();

        if (phone.isEmpty() || otp.isEmpty()) {
            Toast.makeText(this, "Harap isi nomor telepon dan OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (otp.equals("123456")) { // Demo OTP
            navigateToDashboard();
        } else {
            Toast.makeText(this, "Kode OTP salah", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}