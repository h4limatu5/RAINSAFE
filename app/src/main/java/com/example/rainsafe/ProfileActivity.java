package com.example.rainsafe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private LinearLayout navHome, navHistory, navSettings, navProfile;
    private ImageView ivHome, ivHistory, ivSettings, ivProfile, ivProfilePicture;
    private TextView tvHome, tvHistory, tvSettings, tvProfile;
    private TextView tvProfileName, tvProfileEmail, tvProfilePhone;
    private EditText etFormName, etFormEmail, etFormPhone;
    private DatabaseHelper dbHelper;
    private FirebaseSyncHelper firebaseHelper;
    private String currentPhotoPath = null;
    private String userEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);
        firebaseHelper = new FirebaseSyncHelper(this);

        // Initialize Profile Views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        // Initialize Form Views
        etFormName = findViewById(R.id.etFormName);
        etFormEmail = findViewById(R.id.etFormEmail);
        etFormPhone = findViewById(R.id.etFormPhone);

        // Load User Data
        loadUserData();

        // Change Profile Handlers
        if (ivProfilePicture.getParent() instanceof View) {
            ((View) ivProfilePicture.getParent()).setOnClickListener(v -> openGallery());
        }
        findViewById(R.id.btnChangePhotoVisible).setOnClickListener(v -> openGallery());
        
        findViewById(R.id.btnSaveForm).setOnClickListener(v -> saveProfileChanges());

        // Initialize Navigation Views
        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
        
        ivHome = findViewById(R.id.ivHome);
        ivHistory = findViewById(R.id.ivHistory);
        ivSettings = findViewById(R.id.ivSettings);
        ivProfile = findViewById(R.id.ivProfile);
        
        tvHome = findViewById(R.id.tvHome);
        tvHistory = findViewById(R.id.tvHistory);
        tvSettings = findViewById(R.id.tvSettings);
        tvProfile = findViewById(R.id.tvProfile);

        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
        });

        // Set Profile as Active (Position 3)
        updateNavUI(3);

        // Navigation Listeners
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
        
        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("USER_IDENTIFIER", getIntent().getStringExtra("USER_IDENTIFIER"));
            intent.putExtra("LOGIN_TYPE", getIntent().getStringExtra("LOGIN_TYPE"));
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        navProfile.setOnClickListener(v -> updateNavUI(3));
        
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void loadUserData() {
        String identifier = getIntent().getStringExtra("USER_IDENTIFIER");
        String loginType = getIntent().getStringExtra("LOGIN_TYPE");

        if (identifier != null && loginType != null) {
            Map<String, String> userData;
            if (loginType.equals("email")) {
                userData = dbHelper.getUserData(identifier);
            } else {
                userData = dbHelper.getUserDataByPhone(identifier);
            }

            if (!userData.isEmpty()) {
                userEmail = userData.get("email"); // Store current email for updates
                tvProfileName.setText(userData.get("fullname"));
                tvProfileEmail.setText(userData.get("email"));
                if (tvProfilePhone != null) {
                    tvProfilePhone.setText(userData.get("phone"));
                }
                
                String photoPath = userData.get("photo");
                if (photoPath != null && !photoPath.isEmpty()) {
                    try {
                        Uri photoUri = Uri.parse(photoPath);
                        ivProfilePicture.setImageURI(photoUri);
                        ivProfilePicture.setColorFilter(null);
                        ivProfilePicture.setImageTintList(null);
                        currentPhotoPath = photoPath;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Populate Form
                etFormName.setText(userData.get("fullname"));
                etFormEmail.setText(userData.get("email"));
                etFormPhone.setText(userData.get("phone"));
            }
        }
    }

    private void saveProfileChanges() {
        String newName = etFormName.getText().toString().trim();
        String newEmail = etFormEmail.getText().toString().trim();
        String newPhone = etFormPhone.getText().toString().trim();

        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateUserProfile(userEmail, newName, newEmail, newPhone, currentPhotoPath);
        if (success) {
            // Sync to Firebase
            firebaseHelper.syncUserProfile(newEmail, newName, newPhone, currentPhotoPath);
            
            userEmail = newEmail; // Update local reference
            tvProfileName.setText(newName);
            tvProfileEmail.setText(newEmail);
            tvProfilePhone.setText(newPhone);
            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1003);
                return;
            }
        } else {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1003);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Request permanent access to the URI
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException se) {
                        se.printStackTrace();
                    }

                    currentPhotoPath = imageUri.toString();
                    ivProfilePicture.setImageURI(imageUri);
                    ivProfilePicture.setColorFilter(null);
                    ivProfilePicture.setImageTintList(null);

                    // Save to DB immediately if we have a valid identifier
                    if (userEmail != null && !userEmail.isEmpty()) {
                        String name = etFormName.getText().toString();
                        String phone = etFormPhone.getText().toString();
                        boolean success = dbHelper.updateUserProfile(userEmail, name, userEmail, phone, currentPhotoPath);
                        if (success) {
                            firebaseHelper.syncUserProfile(userEmail, name, phone, currentPhotoPath);
                            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal menyimpan foto ke database", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Foto dipilih, klik Simpan untuk memperbarui", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
                    
                    // Fallback
                    ivProfilePicture.setImageURI(imageUri);
                    ivProfilePicture.setColorFilter(null);
                    currentPhotoPath = imageUri.toString();
                }
            }
        }
    }

    private void showEditDialog(String title, String currentValue, String field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubah " + title);

        final EditText input = new EditText(this);
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newValue = input.getText().toString();
            if (newValue.isEmpty()) {
                Toast.makeText(this, "Tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = false;
            if (field.equals("fullname")) {
                success = dbHelper.updateUserProfile(userEmail, newValue, userEmail, tvProfilePhone.getText().toString(), currentPhotoPath);
                if (success) tvProfileName.setText(newValue);
            } else if (field.equals("phone")) {
                success = dbHelper.updateUserProfile(userEmail, tvProfileName.getText().toString(), userEmail, newValue, currentPhotoPath);
                if (success) tvProfilePhone.setText(newValue);
            }

            if (success) {
                Toast.makeText(this, title + " diperbarui", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateNavUI(int position) {
        int grey = ContextCompat.getColor(this, R.color.text_grey);
        int blue = ContextCompat.getColor(this, R.color.button_blue);

        // Reset all to grey/normal
        ivHome.setColorFilter(grey);
        ivHistory.setColorFilter(grey);
        ivSettings.setColorFilter(grey);
        ivProfile.setColorFilter(grey);
        
        tvHome.setTextColor(grey);
        tvHistory.setTextColor(grey);
        tvSettings.setTextColor(grey);
        tvProfile.setTextColor(grey);
        
        tvHome.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvHistory.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvSettings.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvProfile.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Set active item to blue/bold
        switch (position) {
            case 0:
                ivHome.setColorFilter(blue);
                tvHome.setTextColor(blue);
                tvHome.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 1:
                ivHistory.setColorFilter(blue);
                tvHistory.setTextColor(blue);
                tvHistory.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 2:
                ivSettings.setColorFilter(blue);
                tvSettings.setTextColor(blue);
                tvSettings.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 3:
                ivProfile.setColorFilter(blue);
                tvProfile.setTextColor(blue);
                tvProfile.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }
}