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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword, etConfirmPassword;
    private Button btnReset;
    private ImageView ivBack, ivShowPassword;
    private TextView tvBackToLogin;
    private DatabaseHelper dbHelper;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnReset);
        ivBack = findViewById(R.id.ivBack);
        ivShowPassword = findViewById(R.id.ivShowPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        ivBack.setOnClickListener(v -> finish());
        tvBackToLogin.setOnClickListener(v -> finish());

        ivShowPassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            int inputType = isPasswordVisible ? 
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : 
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            
            etNewPassword.setInputType(inputType);
            etConfirmPassword.setInputType(inputType);
            
            ivShowPassword.setImageResource(R.drawable.ic_visibility);
            etNewPassword.setSelection(etNewPassword.getText().length());
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.checkEmail(email)) {
                boolean updated = dbHelper.updatePassword(email, newPassword);
                if (updated) {
                    Toast.makeText(this, "Password berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Gagal memperbarui password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Email tidak terdaftar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
