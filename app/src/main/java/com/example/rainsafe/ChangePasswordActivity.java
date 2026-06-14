package com.example.rainsafe;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ChangePasswordActivity
 *
 * Flow (matching Activity Diagram):
 *   1. User enters old password, new password, confirm password.
 *   2. Validation:
 *      a. Ensure no fields are empty.
 *      b. Ensure new password length >= 6.
 *      c. Ensure new password == confirm password.
 *   3. If login type is "email":
 *      - Re-authenticate with Firebase using current email + old password.
 *      - If re-auth succeeds → update Firebase password.
 *      - Also update local SQLite password.
 *   4. If login type is "phone" (local only):
 *      - Verify old password against SQLite.
 *      - If verified → update SQLite password.
 *   5. Show success or error feedback.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private ImageView ivToggleOld, ivToggleNew, ivToggleConfirm;
    private Button btnChangePassword;
    private TextView tvErrorMessage, tvSuccessMessage;

    private boolean isOldVisible = false, isNewVisible = false, isConfirmVisible = false;

    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    private String userIdentifier;
    private String loginType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        dbHelper = new DatabaseHelper(this);
        mAuth    = FirebaseAuth.getInstance();

        userIdentifier = getIntent().getStringExtra("USER_IDENTIFIER");
        loginType      = getIntent().getStringExtra("LOGIN_TYPE");
        if (loginType == null) loginType = "email";

        // Views
        etOldPassword      = findViewById(R.id.etOldPassword);
        etNewPassword      = findViewById(R.id.etNewPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        ivToggleOld        = findViewById(R.id.ivToggleOld);
        ivToggleNew        = findViewById(R.id.ivToggleNew);
        ivToggleConfirm    = findViewById(R.id.ivToggleConfirm);
        btnChangePassword  = findViewById(R.id.btnChangePassword);
        tvErrorMessage     = findViewById(R.id.tvErrorMessage);
        tvSuccessMessage   = findViewById(R.id.tvSuccessMessage);

        // Back button
        CardView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Password visibility toggles
        ivToggleOld.setOnClickListener(v -> {
            isOldVisible = !isOldVisible;
            toggleVisibility(etOldPassword, ivToggleOld, isOldVisible);
        });
        ivToggleNew.setOnClickListener(v -> {
            isNewVisible = !isNewVisible;
            toggleVisibility(etNewPassword, ivToggleNew, isNewVisible);
        });
        ivToggleConfirm.setOnClickListener(v -> {
            isConfirmVisible = !isConfirmVisible;
            toggleVisibility(etConfirmPassword, ivToggleConfirm, isConfirmVisible);
        });

        // Submit
        btnChangePassword.setOnClickListener(v -> attemptChangePassword());
    }

    // ─── TOGGLE PASSWORD ─────────────────────────────────────────────────────

    private void toggleVisibility(EditText et, ImageView iv, boolean visible) {
        if (visible) {
            et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            iv.setImageResource(R.drawable.ic_visibility);
        } else {
            et.setTransformationMethod(PasswordTransformationMethod.getInstance());
            iv.setImageResource(R.drawable.ic_visibility_off);
        }
        et.setSelection(et.getText().length());
    }

    // ─── VALIDATION & CHANGE ─────────────────────────────────────────────────

    private void attemptChangePassword() {
        String oldPass     = etOldPassword.getText().toString().trim();
        String newPass     = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        hideMessages();

        // Step 1: Empty check
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showError("Semua kolom harus diisi.");
            return;
        }

        // Step 2: Minimum length
        if (newPass.length() < 6) {
            showError("Password baru minimal 6 karakter.");
            return;
        }

        // Step 3: Match check
        if (!newPass.equals(confirmPass)) {
            showError("Password baru dan konfirmasi tidak cocok.");
            return;
        }

        // Step 4: Disable button while processing
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Memproses...");

        if ("email".equalsIgnoreCase(loginType)) {
            changePasswordFirebase(oldPass, newPass);
        } else {
            changePasswordLocal(oldPass, newPass);
        }
    }

    /**
     * Firebase email-based flow:
     * re-authenticate → update Firebase password → update local SQLite
     */
    private void changePasswordFirebase(String oldPass, String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || userIdentifier == null) {
            showError("Sesi tidak ditemukan. Silakan login ulang.");
            resetButton();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(userIdentifier, oldPass);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Re-auth OK → update Firebase password
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        // Also update local SQLite
                        dbHelper.updatePassword(userIdentifier, newPass);
                        showSuccess("Password berhasil diperbarui!");
                        clearFields();
                    } else {
                        String msg = updateTask.getException() != null
                                ? updateTask.getException().getMessage()
                                : "Gagal memperbarui password.";
                        showError(msg);
                    }
                    resetButton();
                });
            } else {
                showError("Password lama salah. Silakan coba lagi.");
                resetButton();
            }
        });
    }

    /**
     * Local SQLite-only flow (phone login):
     * verify old password in SQLite → update SQLite password
     */
    private void changePasswordLocal(String oldPass, String newPass) {
        if (userIdentifier == null) {
            showError("Identitas pengguna tidak ditemukan.");
            resetButton();
            return;
        }

        // Retrieve user data from SQLite by phone
        java.util.Map<String, String> userData = dbHelper.getUserDataByPhone(userIdentifier);
        if (userData.isEmpty()) {
            // Try by email too
            userData = dbHelper.getUserData(userIdentifier);
        }

        if (userData.isEmpty()) {
            showError("Data pengguna tidak ditemukan di database lokal.");
            resetButton();
            return;
        }

        // Verify old password – checkLogin uses email; for phone-only accounts we check differently
        boolean verified = dbHelper.checkLogin(userIdentifier, oldPass);
        if (!verified) {
            // Try phone-based verification
            verified = dbHelper.checkLoginByPhone(userIdentifier, oldPass);
        }

        if (verified) {
            boolean updated = dbHelper.updatePasswordByPhone(userIdentifier, newPass);
            if (updated) {
                showSuccess("Password berhasil diperbarui!");
                clearFields();
            } else {
                // Fallback: try by email
                dbHelper.updatePassword(userIdentifier, newPass);
                showSuccess("Password berhasil diperbarui!");
                clearFields();
            }
        } else {
            showError("Password lama salah. Silakan coba lagi.");
        }
        resetButton();
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvSuccessMessage.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        tvSuccessMessage.setText(message);
        tvSuccessMessage.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.GONE);
    }

    private void hideMessages() {
        tvErrorMessage.setVisibility(View.GONE);
        tvSuccessMessage.setVisibility(View.GONE);
    }

    private void resetButton() {
        btnChangePassword.setEnabled(true);
        btnChangePassword.setText("Ganti Password");
    }

    private void clearFields() {
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }
}
