package com.example.rainsafe;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword, etPhone, etOTP;
    private Button btnLogin, btnGetOTP;
    private TextView tvForgotPassword, tvRegister, tvTabEmail, tvTabPhone;
    private ImageView ivTabEmail, ivTabPhone, ivShowPassword;
    private View lineTabEmail, lineTabPhone;
    private LinearLayout llEmailForm, llPhoneForm, tabEmail, tabPhone;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;
    private boolean isEmailLogin = true;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        mAuth = FirebaseAuth.getInstance();

        // Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etOTP = findViewById(R.id.etOTP);
        btnLogin = findViewById(R.id.btnLogin);
        btnGetOTP = findViewById(R.id.btnGetOTP);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        
        tabEmail = findViewById(R.id.tabEmail);
        tabPhone = findViewById(R.id.tabPhone);
        tvTabEmail = findViewById(R.id.tvTabEmail);
        tvTabPhone = findViewById(R.id.tvTabPhone);
        ivTabEmail = findViewById(R.id.ivTabEmail);
        ivTabPhone = findViewById(R.id.ivTabPhone);
        lineTabEmail = findViewById(R.id.lineTabEmail);
        lineTabPhone = findViewById(R.id.lineTabPhone);
        llEmailForm = findViewById(R.id.llEmailForm);
        llPhoneForm = findViewById(R.id.llPhoneForm);
        ivShowPassword = findViewById(R.id.ivShowPassword);

        // Tab Listeners
        tabEmail.setOnClickListener(v -> switchTab(true));
        tabPhone.setOnClickListener(v -> switchTab(false));

        // Show/Hide Password
        ivShowPassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivShowPassword.setImageResource(R.drawable.ic_visibility); // Or a hide icon if you have one
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivShowPassword.setImageResource(R.drawable.ic_visibility);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnGetOTP.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Masukkan nomor telepon", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "OTP dikirim ke " + phone, Toast.LENGTH_SHORT).show();
                etOTP.setText("123456"); // Dummy OTP
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmailLogin) {
                    performEmailLogin();
                } else {
                    performPhoneLogin();
                }
            }
        });

        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void switchTab(boolean emailSelected) {
        isEmailLogin = emailSelected;
        int activeColor = getResources().getColor(R.color.tab_active, getTheme());
        int inactiveColor = getResources().getColor(R.color.tab_inactive, getTheme());
        int lineInactiveColor = getResources().getColor(R.color.tab_line_inactive, getTheme());

        if (emailSelected) {
            // Email Tab Active
            tvTabEmail.setTextColor(activeColor);
            ivTabEmail.setImageTintList(ColorStateList.valueOf(activeColor));
            lineTabEmail.setBackgroundColor(activeColor);
            lineTabEmail.getLayoutParams().height = (int) (2 * getResources().getDisplayMetrics().density);

            // Phone Tab Inactive
            tvTabPhone.setTextColor(inactiveColor);
            ivTabPhone.setImageTintList(ColorStateList.valueOf(inactiveColor));
            lineTabPhone.setBackgroundColor(lineInactiveColor);
            lineTabPhone.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);

            llEmailForm.setVisibility(View.VISIBLE);
            llPhoneForm.setVisibility(View.GONE);
            tvForgotPassword.setVisibility(View.VISIBLE);
        } else {
            // Phone Tab Active
            tvTabPhone.setTextColor(activeColor);
            ivTabPhone.setImageTintList(ColorStateList.valueOf(activeColor));
            lineTabPhone.setBackgroundColor(activeColor);
            lineTabPhone.getLayoutParams().height = (int) (2 * getResources().getDisplayMetrics().density);

            // Email Tab Inactive
            tvTabEmail.setTextColor(inactiveColor);
            ivTabEmail.setImageTintList(ColorStateList.valueOf(inactiveColor));
            lineTabEmail.setBackgroundColor(lineInactiveColor);
            lineTabEmail.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);

            llEmailForm.setVisibility(View.GONE);
            llPhoneForm.setVisibility(View.VISIBLE);
            tvForgotPassword.setVisibility(View.GONE);
        }
        lineTabEmail.requestLayout();
        lineTabPhone.requestLayout();
    }

    private void performEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Sedang masuk...", Toast.LENGTH_SHORT).show();
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                        
                        // Sinkronisasi dengan database lokal SQLite
                        if (!dbHelper.checkEmail(email)) {
                            dbHelper.insertUser("User RainSafe", email, "-", password);
                        }
                        
                        navigateToDashboard();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Email atau Password salah";
                        Toast.makeText(LoginActivity.this, "Gagal Login: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void performPhoneLogin() {
        String phone = etPhone.getText().toString().trim();
        String otp = etOTP.getText().toString().trim();

        if (phone.isEmpty() || otp.isEmpty()) {
            Toast.makeText(this, "Nomor HP dan OTP tidak boleh kosong", Toast.LENGTH_SHORT).show();
        } else if (otp.equals("123456")) { // Dummy check
            navigateToDashboard();
        } else {
            Toast.makeText(this, "Kode OTP salah", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDashboard() {
        String identifier = isEmailLogin ? etEmail.getText().toString().trim() : etPhone.getText().toString().trim();
        String loginType = isEmailLogin ? "email" : "phone";

        // Save session to SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("RainSafePrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("user_id", identifier);
        editor.putString("login_type", loginType);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("USER_IDENTIFIER", identifier);
        intent.putExtra("LOGIN_TYPE", loginType);
        startActivity(intent);
        finish();
    }
}
