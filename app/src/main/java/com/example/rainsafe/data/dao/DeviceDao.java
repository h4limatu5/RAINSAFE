package com.example.rainsafe.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.rainsafe.data.entity.Device;

@Dao
public interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Device device);

    @Update
    void update(Device device);

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    LiveData<Device> getDeviceLiveData(int id);

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    Device getDeviceById(int id);
}