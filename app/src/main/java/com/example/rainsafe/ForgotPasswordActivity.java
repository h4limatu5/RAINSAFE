package com.example.rainsafe;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnReset;
    private ImageView ivBack;
    private TextView tvBackToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);
        ivBack = findViewById(R.id.ivBack);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Hide password-related views programmatically
        findViewById(R.id.tvLabelNewPassword).setVisibility(View.GONE);
        findViewById(R.id.flNewPassword).setVisibility(View.GONE);
        findViewById(R.id.tvLabelConfirmPassword).setVisibility(View.GONE);
        findViewById(R.id.flConfirmPassword).setVisibility(View.GONE);

        ivBack.setOnClickListener(v -> finish());
        tvBackToLogin.setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            btnReset.setEnabled(false);
            Toast.makeText(this, "Mengirim email reset password...", Toast.LENGTH_SHORT).show();

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        btnReset.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Tautan reset password berhasil dikirim ke email Anda!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Gagal mengirim email reset";
                            Toast.makeText(this, "Gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
