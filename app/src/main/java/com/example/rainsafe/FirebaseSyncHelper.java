package com.example.rainsafe;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

public class FirebaseSyncHelper {
    private static final String TAG = "FirebaseSyncHelper";
    private DatabaseReference mDatabase;
    private DatabaseHelper dbHelper;

    public FirebaseSyncHelper(Context context) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Sinkronisasi semua data dari SQLite ke Firebase
     */
    public void syncAllData() {
        syncUsers();
        syncLogs();
        syncSensors();
    }

    public void syncUsers() {
        // Karena kita tidak punya method getAllUsers di DatabaseHelper, 
        // kita bisa menambahkan logic sync saat user login atau update profil saja 
        // untuk menghemat bandwidth.
    }

    public void syncLogs() {
        List<Map<String, String>> logs = dbHelper.getAllLogs();
        for (Map<String, String> log : logs) {
            String logId = log.get("id");
            if (logId != null) {
                mDatabase.child("logs").child(logId).setValue(log)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Log " + logId + " synced"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync log " + logId, e));
            }
        }
    }

    public void syncSensors() {
        String[] sensors = {"Sensor Hujan", "Sensor Cahaya", "Sensor Kelembaban", "Sensor Suhu"};
        for (String name : sensors) {
            Map<String, String> data = dbHelper.getLatestSensorData(name);
            if (!data.isEmpty()) {
                // Bersihkan nama untuk key Firebase (hindari spasi)
                String firebaseKey = name.replace(" ", "_").toLowerCase();
                mDatabase.child("sensors").child(firebaseKey).setValue(data)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Sensor " + name + " synced"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to sync sensor " + name, e));
            }
        }
    }

    /**
     * Push log spesifik ke Firebase
     */
    public void pushSingleLog(Map<String, String> log) {
        String logId = log.get("id");
        if (logId == null) {
            // Jika ID null (misal log baru yang belum masuk DB), gunakan push()
            mDatabase.child("logs").push().setValue(log);
        } else {
            mDatabase.child("logs").child(logId).setValue(log);
        }
    }
}
