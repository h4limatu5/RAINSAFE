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
    private static final int DATABASE_VERSION = 3;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FULLNAME = "fullname";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_PASSWORD = "password";

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
                    COLUMN_PASSWORD + " TEXT);";

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
    }

    private void insertInitialLogs(SQLiteDatabase db) {
        addLog(db, "Jemuran Dikeluarkan", "Status berubah dari 'Dalam' ke 'Luar'. Otomatis", "auto", "out");
        addLog(db, "Mode Otomatisasi Aktif!", "Sistem aktivitas otomatis diaktifkan oleh pengguna", "manual", "sensor");
        addLog(db, "Hujan Terdeteksi", "Sensor hujan mendeteksi cuaca. Intensitas sedang (52%)", "system", "rain");
        addLog(db, "Jemuran Masuk Otomatis", "Jemuran ditarik ke dalam karena terdeteksi hujan", "auto", "in");
    }

    private void insertInitialSensorData(SQLiteDatabase db) {
        addSensorData(db, "Sensor Hujan", "5", "%", "Aktif");
        addSensorData(db, "Sensor Cahaya", "800", "lux", "Aktif");
        addSensorData(db, "Sensor Kelembaban", "65", "%", "Aktif");
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
        }
        cursor.close();
        return userData;
    }

    // Method to update password (Forgot Password)
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PASSWORD, newPassword);

        int result = db.update(TABLE_USERS, contentValues, COLUMN_EMAIL + " = ?", new String[]{email});
        return result > 0;
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