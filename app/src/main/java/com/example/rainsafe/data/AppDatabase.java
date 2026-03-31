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

import java.util.concurrent.Executors;

@Database(entities = {User.class, Device.class, History.class}, version = 2)
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
                                    // Inisialisasi data perangkat default saat pertama kali db dibuat
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        getInstance(context).deviceDao().insert(new Device());
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}