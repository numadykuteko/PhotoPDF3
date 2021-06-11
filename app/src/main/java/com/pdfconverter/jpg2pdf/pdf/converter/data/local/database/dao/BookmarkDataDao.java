package com.pdfconverter.jpg2pdf.pdf.converter.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface BookmarkDataDao {

    @Delete
    void delete(BookmarkData bookmarkData);

    @Query("DELETE FROM bookmark_data WHERE filePath = :path")
    void deleteByPath(String path);

    @Query("DELETE FROM bookmark_data")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookmarkData bookmarkData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BookmarkData> bookmarkDataList);

    @Query("SELECT * FROM bookmark_data ORDER BY timeAdded DESC")
    Single<List<BookmarkData>> loadAll();

    @Query("SELECT * FROM bookmark_data WHERE filePath = :path LIMIT 1")
    Single<BookmarkData> findByPath(String path);
}
