package com.example.rainsafe;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rainsafe.data.AppDatabase;
import com.example.rainsafe.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotInput;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = AppDatabase.getInstance(this);

        etForgotInput = findViewById(R.id.etForgotInput);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etForgotInput.getText().toString().trim();

                if (input.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Harap isi email atau nomor telepon", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Simulasi pengiriman instruksi reset password
                // Dalam aplikasi nyata, ini akan mengirim email/SMS via API
                Toast.makeText(ForgotPasswordActivity.this, "Instruksi reset telah dikirim ke " + input, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}