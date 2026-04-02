package com.example.rainsafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rainsafe.data.AppDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotInput;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etForgotInput = findViewById(R.id.etForgotInput);
        Button btnResetPassword = findViewById(R.id.btnResetPassword);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(v -> {
            String input = etForgotInput.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.err_forgot_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulasi pengiriman instruksi reset password
            Toast.makeText(ForgotPasswordActivity.this, getString(R.string.msg_reset_sent, input), Toast.LENGTH_LONG).show();
            finish();
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}