package com.example.rainsafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RainSafe.db";
    private static final int DATABASE_VERSION = 5;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULLNAME = "fullname";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHOTO = "profile_photo"; // Path to image file or base64

    // Table Activity Logs
    public static final String TABLE_LOGS = "activity_logs";
    public static final String COLUMN_LOG_ID = "log_id";
    public static final String COLUMN_LOG_TITLE = "title";
    public static final String COLUMN_LOG_DESC = "description";
    public static final String COLUMN_LOG_TIME = "timestamp";
    public static final String COLUMN_LOG_TYPE = "type"; // auto, manual, system
    public static final String COLUMN_LOG_ICON = "icon_type"; // in, out, rain, sensor

    // Table Sensor Data (NEW)
    public static final String TABLE_SENSORS = "sensor_data";
    public static final String COLUMN_SENSOR_ID = "s_id";
    public static final String COLUMN_SENSOR_NAME = "s_name"; // Rain, Light, Humidity
    public static final String COLUMN_SENSOR_VALUE = "s_value";
    public static final String COLUMN_SENSOR_UNIT = "s_unit"; // %, lux, etc
    public static final String COLUMN_SENSOR_STATUS = "s_status"; // Aktif, Mati
    public static final String COLUMN_SENSOR_TIME = "s_timestamp";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FULLNAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_PHONE + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_PHOTO + " TEXT);";

    private static final String TABLE_CREATE_LOGS =
            "CREATE TABLE " + TABLE_LOGS + " (" +
                    COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LOG_TITLE + " TEXT, " +
                    COLUMN_LOG_DESC + " TEXT, " +
                    COLUMN_LOG_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COLUMN_LOG_TYPE + " TEXT, " +
                    COLUMN_LOG_ICON + " TEXT);";

    private static final String TABLE_CREATE_SENSORS =
            "CREATE TABLE " + TABLE_SENSORS + " (" +
                    COLUMN_SENSOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SENSOR_NAME + " TEXT, " +
                    COLUMN_SENSOR_VALUE + " TEXT, " +
                    COLUMN_SENSOR_UNIT + " TEXT, " +
                    COLUMN_SENSOR_STATUS + " TEXT, " +
                    COLUMN_SENSOR_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_LOGS);
        db.execSQL(TABLE_CREATE_SENSORS);
        
        insertInitialLogs(db);
        insertInitialSensorData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(TABLE_CREATE_LOGS);
            insertInitialLogs(db);
        }
        if (oldVersion < 3) {
            db.execSQL(TABLE_CREATE_SENSORS);
            insertInitialSensorData(db);
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHOTO + " TEXT");
            } catch (Exception e) {
                // Column might already exist
            }
        }
        if (oldVersion < 5) {
            addSensorData(db, "Sensor Suhu", "27", "°C", "Aktif");
        }
    }

    private void insertInitialLogs(SQLiteDatabase db) {
        addLog(db, "Jemuran Dikeluarkan", "Status berubah dari 'Dalam' ke 'Luar'. Otomatis", "auto", "out");
        addLog(db, "Mode Otomatisasi Aktif!", "Sistem aktivitas otomatis diaktifkan oleh pengguna", "manual", "sensor");
        addLog(db, "Hujan Terdeteksi", "Sensor hujan mendeteksi cuaca. Intensitas sedang (52%)", "system", "rain");
        addLog(db, "Jemuran Masuk Otomatis", "Jemuran ditarik ke dalam karena terdeteksi hujan", "auto", "in");
        addLog(db, "Sensor Cahaya: Terik", "Intensitas cahaya tinggi dideteksi (1200 lux)", "system", "sensor");
        addLog(db, "Sensor Kelembaban Tinggi", "Kelembaban udara mencapai 85%", "system", "sensor");
        addLog(db, "Koneksi Wi-Fi Stabil", "Perangkat terhubung ke jaringan 'RainSafe_Home'", "system", "sensor");
        addLog(db, "Baterai Backup Terisi", "Daya cadangan sistem dalam kondisi 100%", "system", "sensor");
        addLog(db, "Pemeliharaan Sistem Selesai", "Pengecekan rutin komponen mekanik berhasil", "system", "sensor");
        addLog(db, "Update Firmware Tersedia", "Versi 1.2.5 tersedia untuk diunduh", "system", "sensor");
        addLog(db, "Jemuran Dikeluarkan (Pagi)", "Sistem mengeluarkan jemuran sesuai jadwal pagi", "auto", "out");
        addLog(db, "Sensor Hujan: Gerimis", "Terdeteksi rintik hujan ringan (10%)", "system", "rain");
        addLog(db, "Jemuran Masuk (Manual)", "Pengguna menarik jemuran melalui aplikasi", "manual", "in");
        addLog(db, "Mode Malam Aktif", "Sensor cahaya mendeteksi kondisi gelap, sistem standby", "auto", "sensor");
        addLog(db, "Deteksi Angin Kencang", "Kecepatan angin melebihi ambang batas aman", "system", "sensor");
        addLog(db, "Jemuran Aman Terlindungi", "Posisi jemuran berada di area tertutup", "system", "in");
        addLog(db, "Kalibrasi Sensor Selesai", "Sensor hujan dan cahaya telah dikalibrasi ulang", "system", "sensor");
        addLog(db, "Akun Berhasil Diperbarui", "Informasi profil pengguna telah diubah", "manual", "sensor");
        addLog(db, "Login Berhasil", "Sesi baru dimulai pada perangkat Android", "manual", "sensor");
        addLog(db, "Selamat Datang di RainSafe", "Terima kasih telah menggunakan sistem jemuran pintar kami", "system", "sensor");
    }

    private void insertInitialSensorData(SQLiteDatabase db) {
        addSensorData(db, "Sensor Hujan", "5", "%", "Aktif");
        addSensorData(db, "Sensor Cahaya", "800", "lux", "Aktif");
        addSensorData(db, "Sensor Kelembaban", "65", "%", "Aktif");
        addSensorData(db, "Sensor Suhu", "27", "°C", "Aktif");
    }

    public void addLog(String title, String desc, String type, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_TITLE, title);
        values.put(COLUMN_LOG_DESC, desc);
        values.put(COLUMN_LOG_TYPE, type);
        values.put(COLUMN_LOG_ICON, icon);
        values.put(COLUMN_LOG_TIME, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_LOGS, null, values);
    }

    private void addLog(SQLiteDatabase db, String title, String desc, String type, String icon) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_TITLE, title);
        values.put(COLUMN_LOG_DESC, desc);
        values.put(COLUMN_LOG_TYPE, type);
        values.put(COLUMN_LOG_ICON, icon);
        values.put(COLUMN_LOG_TIME, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_LOGS, null, values);
    }

    private void addSensorData(SQLiteDatabase db, String name, String value, String unit, String status) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENSOR_NAME, name);
        values.put(COLUMN_SENSOR_VALUE, value);
        values.put(COLUMN_SENSOR_UNIT, unit);
        values.put(COLUMN_SENSOR_STATUS, status);
        db.insert(TABLE_SENSORS, null, values);
    }

    // Method to insert a new user
    public boolean insertUser(String fullname, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FULLNAME, fullname);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    // Method to check if email already exists
    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Method to check login
    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Method to check login and return user data
    public Map<String, String> getUserData(String email) {
        Map<String, String> userData = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?", new String[]{email});

        if (cursor.moveToFirst()) {
            userData.put("fullname", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));
            userData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            userData.put("phone", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
            userData.put("photo", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)));
        }
        cursor.close();
        return userData;
    }

    // Method for phone login
    public Map<String, String> getUserDataByPhone(String phone) {
        Map<String, String> userData = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_PHONE + "=?", new String[]{phone});

        if (cursor.moveToFirst()) {
            userData.put("fullname", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));
            userData.put("email", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            userData.put("phone", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
            userData.put("photo", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)));
        }
        cursor.close();
        return userData;
    }

    // Method to update user profile
    public boolean updateUserProfile(String oldEmail, String fullname, String newEmail, String phone, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULLNAME, fullname);
        values.put(COLUMN_EMAIL, newEmail);
        values.put(COLUMN_PHONE, phone);
        if (photoPath != null) {
            values.put(COLUMN_PHOTO, photoPath);
        }

        int result = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{oldEmail});
        return result > 0;
    }

    // Method to update password (Forgot Password)
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PASSWORD, newPassword);

        int result = db.update(TABLE_USERS, contentValues, COLUMN_EMAIL + " = ?", new String[]{email});
        return result > 0;
    }

    public void updateSensorData(String name, String value, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENSOR_VALUE, value);
        values.put(COLUMN_SENSOR_STATUS, status);
        values.put(COLUMN_SENSOR_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_SENSORS, values, COLUMN_SENSOR_NAME + " = ?", new String[]{name});
    }

    // Method to get all logs
    public List<Map<String, String>> getAllLogs() {
        List<Map<String, String>> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LOGS + " ORDER BY " + COLUMN_LOG_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Map<String, String> log = new HashMap<>();
                log.put("id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID)));
                log.put("title", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_TITLE)));
                log.put("desc", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_DESC)));
                log.put("time", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_TIME)));
                log.put("type", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_TYPE)));
                log.put("icon", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_ICON)));
                logs.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return logs;
    }

    // Clear all logs
    public void clearAllLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGS, null, null);
    }

    // Method to get latest sensor readings
    public Map<String, String> getLatestSensorData(String sensorName) {
        Map<String, String> data = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SENSORS + " WHERE " + COLUMN_SENSOR_NAME + "=? ORDER BY " + COLUMN_SENSOR_ID + " DESC LIMIT 1", new String[]{sensorName});

        if (cursor.moveToFirst()) {
            data.put("value", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_VALUE)));
            data.put("unit", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_UNIT)));
            data.put("status", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_STATUS)));
            data.put("time", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENSOR_TIME)));
        }
        cursor.close();
        return data;
    }
}