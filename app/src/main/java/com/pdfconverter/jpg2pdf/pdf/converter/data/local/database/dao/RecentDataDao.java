package com.pdfconverter.jpg2pdf.pdf.converter.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface RecentDataDao {

    @Delete
    void delete(RecentData recentData);

    @Query("DELETE FROM recent_data WHERE filePath = :path")
    void deleteByPath(String path);

    @Query("DELETE FROM recent_data")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecentData historyData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecentData> recentDataList);

    @Query("SELECT * FROM recent_data ORDER BY timeAdded DESC LIMIT 20")
    Single<List<RecentData>> loadAll();

    @Query("SELECT * FROM recent_data WHERE filePath = :path LIMIT 1")
    Single<RecentData> findByPath(String path);
}
