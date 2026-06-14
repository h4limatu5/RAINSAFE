package com.example.rainsafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends BaseActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Mendaftarkan...", Toast.LENGTH_SHORT).show();
                    btnRegister.setEnabled(false);

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, task -> {
                                btnRegister.setEnabled(true);
                                if (task.isSuccessful()) {
                                    // Pendaftaran berhasil di Firebase Auth, simpan lokal & rtdb
                                    boolean isInserted = dbHelper.insertUser(fullName, email, phone, password);
                                    if (isInserted) {
                                        FirebaseSyncHelper firebaseHelper = new FirebaseSyncHelper(RegisterActivity.this);
                                        firebaseHelper.syncUserProfile(email, fullName, phone, null);
                                    }

                                    Toast.makeText(RegisterActivity.this, "Pendaftaran Berhasil! Selamat datang, " + fullName, Toast.LENGTH_LONG).show();

                                    // Auto-login: Simpan sesi di SharedPreferences
                                    android.content.SharedPreferences prefs = getSharedPreferences("RainSafePrefs", MODE_PRIVATE);
                                    android.content.SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("is_logged_in", true);
                                    editor.putString("user_id", email);
                                    editor.putString("login_type", "email");
                                    editor.apply();

                                    // Buka Dashboard
                                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                    intent.putExtra("USER_IDENTIFIER", email);
                                    intent.putExtra("LOGIN_TYPE", "email");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Kesalahan tidak diketahui";
                                    Toast.makeText(RegisterActivity.this, "Pendaftaran Gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kembali ke halaman Login
            }
        });
    }
}