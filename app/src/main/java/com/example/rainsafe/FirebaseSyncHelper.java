package com.example.rainsafe;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class FirebaseSyncHelper {
    private static final String TAG = "FirebaseSyncHelper";

    // Ganti dengan URL Realtime Database milikmu
    // Format: https://<project-id>-default-rtdb.asia-southeast1.firebasedatabase.app/
    // atau   https://<project-id>-default-rtdb.firebaseio.com/
    private static final String DATABASE_URL =
            "https://rainsafe-777f2-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private DatabaseReference mDatabase;
    private DatabaseHelper dbHelper;
    private Context context;

    public FirebaseSyncHelper(Context context) {
        this.context = context.getApplicationContext();
        try {
            mDatabase = FirebaseDatabase.getInstance(DATABASE_URL).getReference();
            Log.d(TAG, "Firebase Database initialized: " + DATABASE_URL);
        } catch (Exception e) {
            // Fallback ke instance default jika URL gagal
            mDatabase = FirebaseDatabase.getInstance().getReference();
            Log.w(TAG, "Firebase fallback to default instance: " + e.getMessage());
        }
        dbHelper = new DatabaseHelper(context);
    }

    // ─── CALLBACK INTERFACE ──────────────────────────────────────────────────

    public interface WriteCallback {
        void onSuccess();
        void onFailure(String errorMsg);
    }

    // ─── SYNC ────────────────────────────────────────────────────────────────

    public void syncAllData() {
        syncUsers();
        syncLogs();
        syncSensors();
    }

    public void syncUsers() {
        // Implementation for syncing a specific user
    }

    public void syncUserProfile(String email, String name, String phone, String photoPath) {
        if (email == null || email.isEmpty()) return;
        String userKey = email.replace(".", ",");
        Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("email", email);
        userMap.put("name", name);
        userMap.put("phone", phone);
        userMap.put("photoPath", photoPath);
        userMap.put("lastUpdated", System.currentTimeMillis());

        mDatabase.child("users").child(userKey).setValue(userMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile synced: " + email))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync user profile", e));
    }

    public void syncLogs() {
        List<Map<String, String>> logs = dbHelper.getAllLogs();
        for (Map<String, String> log : logs) {
            String logId = log.get("id");
            if (logId != null) {
                mDatabase.child("logs").child(logId).setValue(log)
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync log " + logId, e));
            }
        }
    }

    public void syncSensors() {
        String[] sensors = {"Sensor Hujan", "Sensor Cahaya", "Sensor Kelembaban"};
        for (String name : sensors) {
            Map<String, String> data = dbHelper.getLatestSensorData(name);
            if (!data.isEmpty()) {
                String firebaseKey = name.replace(" ", "_").toLowerCase();
                mDatabase.child("sensors").child(firebaseKey).setValue(data)
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync sensor " + name, e));
            }
        }
    }

    public void updateLaundryControl(int position) {
        mDatabase.child("controls").child("laundry_pos").setValue(position)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Laundry position updated to " + position))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update laundry position", e));
    }

    public void pushSingleLog(Map<String, String> log) {
        String logId = log.get("id");
        if (logId == null) {
            mDatabase.child("logs").push().setValue(log);
        } else {
            mDatabase.child("logs").child(logId).setValue(log);
        }
    }

    // ─── LISTENERS ───────────────────────────────────────────────────────────

    public interface OnSensorChangeListener {
        void onSensorChanged(String name, String value, String status, String unit);
    }

    public interface OnControlChangeListener {
        void onControlChanged(boolean autoMode, String laundryStatus);
    }

    public void startListeningSensors(OnSensorChangeListener listener) {
        mDatabase.child("sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                for (DataSnapshot sensorSnapshot : dataSnapshot.getChildren()) {
                    String key = sensorSnapshot.getKey();
                    if (key == null) continue;

                    String value  = sensorSnapshot.child("value").getValue(String.class);
                    String status = sensorSnapshot.child("status").getValue(String.class);
                    String unit   = sensorSnapshot.child("unit").getValue(String.class);

                    String sensorName = "";
                    if (key.equals("sensor_hujan"))       sensorName = "Sensor Hujan";
                    else if (key.equals("sensor_cahaya")) sensorName = "Sensor Cahaya";
                    else if (key.equals("sensor_kelembaban")) sensorName = "Sensor Kelembaban";

                    if (!sensorName.isEmpty() && value != null && status != null) {
                        dbHelper.updateSensorData(sensorName, value, status);
                        Log.d(TAG, "Sensor " + sensorName + " updated: " + value + " / " + status);
                        if (listener != null) listener.onSensorChanged(sensorName, value, status, unit);

                        // If Sensor Cahaya (LDR) updated, decide dark/bright using status or numeric value
                        if ("Sensor Cahaya".equals(sensorName)) {
                            android.content.SharedPreferences prefs = context.getSharedPreferences("RainSafePrefs", Context.MODE_PRIVATE);
                            boolean wasDark = prefs.getBoolean("sensor_cahaya_dark", false);
                            boolean isDark = false;
                            try {
                                // Prefer explicit status field if available
                                if (unit != null && !unit.isEmpty() && (status.equalsIgnoreCase("Gelap") || status.equalsIgnoreCase("Terang"))) {
                                    isDark = status.equalsIgnoreCase("Gelap");
                                } else {
                                    int lux = Integer.parseInt(value.replaceAll("[^0-9]", ""));
                                    int threshold = 1000; // match firmware threshold (higher = darker)
                                    isDark = lux >= threshold;
                                }

                                if (isDark && !wasDark) {
                                    // dark started
                                    NotificationHelper.sendNotification(context, 2001,
                                            "Kondisi Gelap Terdeteksi",
                                            "Sensor cahaya mendeteksi kondisi gelap. Periksa jemuran Anda.");
                                    dbHelper.addLog("Kondisi Gelap Terdeteksi", "Sensor cahaya: " + value + " " + unit, "system", "sensor");
                                    prefs.edit().putBoolean("sensor_cahaya_dark", true).apply();
                                } else if (!isDark && wasDark) {
                                    // bright returned
                                    NotificationHelper.sendNotification(context, 2002,
                                            "Cahaya Kembali Terang",
                                            "Sensor cahaya menunjukkan kondisi terang lagi.");
                                    dbHelper.addLog("Cahaya Kembali Terang", "Sensor cahaya: " + value + " " + unit, "system", "sun");
                                    prefs.edit().putBoolean("sensor_cahaya_dark", false).apply();
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Failed to evaluate sensor_cahaya: " + e.getMessage());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Sensor listener cancelled: " + error.getMessage());
            }
        });
    }

    public void startListeningControl(OnControlChangeListener listener) {
        mDatabase.child("control").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.w(TAG, "control node belum ada di Firebase");
                    // Buat node awal jika belum ada
                    initControlNode();
                    return;
                }

                Boolean autoModeObj = dataSnapshot.child("auto_mode").getValue(Boolean.class);
                String laundryStatus = dataSnapshot.child("laundry_status").getValue(String.class);

                boolean autoMode = autoModeObj != null ? autoModeObj : true;
                if (laundryStatus == null) laundryStatus = "out";

                Log.d(TAG, "Control from Firebase: autoMode=" + autoMode + ", laundry=" + laundryStatus);
                if (listener != null) listener.onControlChanged(autoMode, laundryStatus);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Control listener cancelled: " + error.getMessage());
                Toast.makeText(context, "Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── WRITE COMMANDS ──────────────────────────────────────────────────────

    /**
     * Update status jemuran di Firebase.
     * @param status "in" atau "out"
     * @param callback opsional untuk mengetahui hasil write
     */
    public void updateLaundryStatus(String status, WriteCallback callback) {
        Log.d(TAG, "Writing laundry_status = " + status);
        // Write to '/control/laundry_status' for original ESP32
        mDatabase.child("control").child("laundry_status").setValue(status);
        // Write to '/controls/laundry_pos' for new remote code compatibility
        mDatabase.child("controls").child("laundry_pos").setValue("in".equals(status) ? 0 : 1);
        
        mDatabase.child("control").child("last_command_by").setValue("manual_app")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "laundry_status updated to: " + status);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update laundry_status", e);
                    String msg = "Gagal update Firebase: " + e.getMessage();
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /** Versi tanpa callback (backward compat) */
    public void updateLaundryStatus(String status) {
        updateLaundryStatus(status, null);
    }

    /**
     * Update auto_mode di Firebase.
     * @param autoMode true = auto, false = manual
     * @param callback opsional untuk mengetahui hasil write
     */
    public void updateAutoMode(boolean autoMode, WriteCallback callback) {
        Log.d(TAG, "Writing auto_mode = " + autoMode);
        // Write to '/control/auto_mode' for original ESP32
        mDatabase.child("control").child("auto_mode").setValue(autoMode);
        // Write to '/controls/auto_mode' for new remote code compatibility
        mDatabase.child("controls").child("auto_mode").setValue(autoMode ? 1 : 0);

        mDatabase.child("control").child("last_command_by").setValue("manual_app")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "auto_mode updated to: " + autoMode);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update auto_mode", e);
                    String msg = "Gagal update Firebase: " + e.getMessage();
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /** Versi tanpa callback (backward compat) */
    public void updateAutoMode(boolean autoMode) {
        updateAutoMode(autoMode, null);
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────

    /**
     * Buat node "control" awal di Firebase jika belum ada.
     */
    private void initControlNode() {
        Map<String, Object> initial = new java.util.HashMap<>();
        initial.put("auto_mode", true);
        initial.put("laundry_status", "out");
        initial.put("last_command_by", "app_init");
        mDatabase.child("control").setValue(initial)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Control node initialized"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Cannot init control node - check Firebase rules!", e);
                    Toast.makeText(context,
                            "⚠️ Firebase Rules Error: " + e.getMessage() +
                            "\nBuka Firebase Console → Realtime DB → Rules → set ke test mode",
                            Toast.LENGTH_LONG).show();
                });
    }
}
