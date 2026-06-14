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
import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private String originalPhotoPath = null;
    // Connection status
    private View ivStatusDot;
    private TextView tvStatusText;
    private ValueEventListener connectivityListener;
    private static final String DB_URL = "https://rainsafe-777f2-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private String userEmail = null;
    private boolean isEditingMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            View bottomNav = findViewById(R.id.bottomNavContainer);
            if (bottomNav != null) {
                int bottomInset = systemBars.bottom;
                bottomNav.setPadding(bottomNav.getPaddingLeft(), bottomNav.getPaddingTop(), bottomNav.getPaddingRight(), bottomInset);
                android.view.ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
                int baseHeight = (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 75, v.getResources().getDisplayMetrics());
                params.height = baseHeight + bottomInset;
                bottomNav.setLayoutParams(params);
            }
            return insets;
        });

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

        // Load User Data and Activity Data
        loadUserData();
        loadActivityData();

        // Change Profile Handlers (Only active when in edit mode)
        if (ivProfilePicture.getParent() instanceof View) {
            ((View) ivProfilePicture.getParent()).setOnClickListener(v -> {
                if (isEditingMode) openGallery();
            });
        }
        findViewById(R.id.btnChangePhotoVisible).setOnClickListener(v -> {
            if (isEditingMode) openGallery();
        });
        
        findViewById(R.id.btnSaveForm).setOnClickListener(v -> saveProfileChanges());
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> setEditingMode(true));
        findViewById(R.id.btnCancelForm).setOnClickListener(v -> setEditingMode(false));

        // Set initial view state (Viewing Mode)
        setEditingMode(false);

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

        // Notification & Refresh buttons
        findViewById(R.id.btnNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
        // Refresh button - reload data + reconnect Firebase
        findViewById(R.id.btnRefresh).setOnClickListener(v -> {
            loadUserData();
            loadActivityData();
            FirebaseDatabase.getInstance(DB_URL).goOnline();
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show();
        });

        // Connection status
        ivStatusDot = findViewById(R.id.ivStatusDot);
        tvStatusText = findViewById(R.id.tvStatusText);
        startListeningConnection();

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
            // Clear local session
            android.content.SharedPreferences prefs = getSharedPreferences("RainSafePrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Sign out from Firebase Auth
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

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
                originalPhotoPath = photoPath;
                currentPhotoPath = photoPath;
                if (photoPath != null && !photoPath.isEmpty()) {
                    try {
                        Uri photoUri = Uri.parse(photoPath);
                        ivProfilePicture.setImageURI(photoUri);
                        ivProfilePicture.setColorFilter(null);
                        ivProfilePicture.setImageTintList(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ivProfilePicture.setImageResource(R.drawable.ic_person);
                    ivProfilePicture.setColorFilter(ContextCompat.getColor(this, R.color.button_blue));
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

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
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
            setEditingMode(false);
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
                    Toast.makeText(this, "Foto dipilih, klik Simpan untuk memperbarui", Toast.LENGTH_SHORT).show();
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

    private void setEditingMode(boolean editing) {
        this.isEditingMode = editing;
 
        View cvEditForm = findViewById(R.id.cvEditForm);
        View btnChangePhotoVisible = findViewById(R.id.btnChangePhotoVisible);
        View sectionDeviceTitle = findViewById(R.id.sectionDeviceTitle);
        View cvDeviceCard = findViewById(R.id.cvDeviceCard);
        View sectionActivityTitle = findViewById(R.id.sectionActivityTitle);
        View cvActivityCard = findViewById(R.id.cvActivityCard);
        View btnLogout = findViewById(R.id.btnLogout);
        View btnEditProfile = findViewById(R.id.btnEditProfile);
 
        if (cvEditForm != null) {
            cvEditForm.setVisibility(editing ? View.VISIBLE : View.GONE);
        }
        if (btnChangePhotoVisible != null) {
            btnChangePhotoVisible.setVisibility(editing ? View.VISIBLE : View.GONE);
        }
 
        int otherVisibility = editing ? View.GONE : View.VISIBLE;
        if (sectionDeviceTitle != null) sectionDeviceTitle.setVisibility(otherVisibility);
        if (cvDeviceCard != null) cvDeviceCard.setVisibility(otherVisibility);
        if (sectionActivityTitle != null) sectionActivityTitle.setVisibility(otherVisibility);
        if (cvActivityCard != null) cvActivityCard.setVisibility(otherVisibility);
        if (btnLogout != null) btnLogout.setVisibility(otherVisibility);
        if (btnEditProfile != null) btnEditProfile.setVisibility(editing ? View.GONE : View.VISIBLE);
 
        if (editing) {
            etFormName.setText(tvProfileName.getText().toString());
            etFormEmail.setText(tvProfileEmail.getText().toString());
            etFormPhone.setText(tvProfilePhone.getText().toString());
        } else {
            loadUserData();
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

    private void loadActivityData() {
        java.util.List<Map<String, String>> logs = dbHelper.getAllLogs();
        int usageCount = 0;
        String lastUsed = "-";
        String lastStatus = "Tidak Diketahui";
        long totalDurationMinutes = 0;
        
        for (Map<String, String> log : logs) {
            String icon = log.get("icon");
            if ("out".equals(icon) || "in".equals(icon)) {
                if (usageCount == 0) {
                    lastUsed = log.get("time");
                    lastStatus = "out".equals(icon) ? "Di Luar" : "Di Dalam";
                }
                if ("out".equals(icon)) {
                    usageCount++;
                }
            }
        }
        // Calculate total duration by pairing out->in events (iterate oldest->newest)
        String lastOutTime = null;
        for (int i = logs.size() - 1; i >= 0; i--) {
            Map<String, String> log = logs.get(i);
            String icon = log.get("icon");
            String timeStr = log.get("time"); // format HH:mm
            if ("out".equals(icon)) {
                lastOutTime = timeStr;
            } else if ("in".equals(icon)) {
                if (lastOutTime != null && timeStr != null) {
                    try {
                        String[] outParts = lastOutTime.split(":");
                        String[] inParts = timeStr.split(":");
                        int outH = Integer.parseInt(outParts[0]);
                        int outM = Integer.parseInt(outParts[1]);
                        int inH = Integer.parseInt(inParts[0]);
                        int inM = Integer.parseInt(inParts[1]);
                        int diffM = (inH * 60 + inM) - (outH * 60 + outM);
                        if (diffM < 0) diffM += 24 * 60;
                        totalDurationMinutes += diffM;
                        lastOutTime = null;
                    } catch (Exception e) {
                        // ignore parse errors
                    }
                }
            }
        }
        
        TextView tvTotalUsage = findViewById(R.id.tvTotalUsage);
        TextView tvLastUsed = findViewById(R.id.tvLastUsed);
        TextView tvLastStatus = findViewById(R.id.tvLastStatus);
        TextView tvTotalDuration = findViewById(R.id.tvTotalDuration);
        
        if (tvTotalUsage != null) tvTotalUsage.setText(usageCount + " kali");
        if (tvLastUsed != null) tvLastUsed.setText(lastUsed);
        if (tvLastStatus != null) tvLastStatus.setText(lastStatus);
        if (tvTotalDuration != null) {
            if (totalDurationMinutes < 60) {
                tvTotalDuration.setText(totalDurationMinutes + " mnt");
            } else {
                long h = totalDurationMinutes / 60;
                long m = totalDurationMinutes % 60;
                tvTotalDuration.setText(h + "j " + m + "m");
            }
        }
    }

    // ─── CONNECTION STATUS ────────────────────────────────────────────────────

    private void startListeningConnection() {
        connectivityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                runOnUiThread(() -> {
                    if (ivStatusDot != null) {
                        ivStatusDot.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                connected ? android.graphics.Color.parseColor("#4CAF50")
                                          : android.graphics.Color.parseColor("#F44336")
                            )
                        );
                    }
                    if (tvStatusText != null) {
                        tvStatusText.setText(connected
                            ? getString(R.string.dash_status_connected)
                            : getString(R.string.dash_status_disconnected));
                        tvStatusText.setTextColor(connected
                            ? ContextCompat.getColor(ProfileActivity.this, R.color.button_blue)
                            : android.graphics.Color.parseColor("#F44336"));
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        FirebaseDatabase.getInstance(DB_URL)
            .getReference(".info/connected").addValueEventListener(connectivityListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityListener != null) {
            FirebaseDatabase.getInstance(DB_URL)
                .getReference(".info/connected").removeEventListener(connectivityListener);
        }
    }
}