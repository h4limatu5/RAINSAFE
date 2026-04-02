package com.example.rainsafe.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.rainsafe.data.dao.DeviceDao;
import com.example.rainsafe.data.dao.HistoryDao;
import com.example.rainsafe.data.dao.UserDao;
import com.example.rainsafe.data.entity.Device;
import com.example.rainsafe.data.entity.History;
import com.example.rainsafe.data.entity.User;

@Database(entities = {User.class, Device.class, History.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract DeviceDao deviceDao();
    public abstract HistoryDao historyDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "rainsafe_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Seeding data awal menggunakan SQL mentah untuk keamanan saat startup
                                    seedData(db);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedData(SupportSQLiteDatabase db) {
        // 1. Insert User Default
        db.execSQL("INSERT INTO users (fullName, email, phoneNumber, password, profileImage, totalUsageHours, lastUsed) " +
                "VALUES ('Ahmad Ridho', 'andi@gmail.com', '08123456789', '12345678', '', 247, 'Hari Ini')");

        // 2. Insert Device Default
        db.execSQL("INSERT INTO devices (id, name, status, location, firmware, temperature, humidity, windSpeed, rainProbability, drynessPercentage, isClosed, isAutomationActive, rainSensitivity, responseDelay, nightMode, rainSensorActive, lightSensorActive, humiditySensorActive) " +
                "VALUES (1, 'RainSafe Laundry', 'Online', 'Rumah', 'v2.1.0', 27.0, 65, 12.0, 5, 45, 0, 1, 'Medium', 5, 0, 1, 1, 1)");

        // 3. Insert 20 History Data
        for (int i = 1; i <= 20; i++) {
            String title = (i % 2 == 0) ? "Jemuran Dimasukkan" : "Jemuran Dikeluarkan";
            String desc = (i % 2 == 0) ? "Atap ditutup karena hujan terdeteksi." : "Atap dibuka karena cuaca cerah.";
            String time = "01 Apr 2024, " + (10 + (i % 12)) + ":" + (10 + (i * 2 % 50));
            String type = (i % 3 == 0) ? "Sistem" : (i % 3 == 1 ? "Manual" : "Otomatis");
            db.execSQL("INSERT INTO history (title, description, timestamp, type) VALUES ('" + title + "', '" + desc + "', '" + time + "', '" + type + "')");
        }
    }
}