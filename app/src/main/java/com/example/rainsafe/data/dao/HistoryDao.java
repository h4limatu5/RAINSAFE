package com.example.rainsafe.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.rainsafe.data.entity.History;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History history);

    @Query("SELECT * FROM history ORDER BY id DESC")
    List<History> getAllHistory();

    @Query("DELETE FROM history")
    void clearHistory();
}