package com.pdfconverter.jpg2pdf.pdf.converter.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.pdfconverter.jpg2pdf.pdf.converter.data.local.database.dao.BookmarkDataDao;
import com.pdfconverter.jpg2pdf.pdf.converter.data.local.database.dao.RecentDataDao;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;

@Database(entities = {RecentData.class, BookmarkData.class}, version = 1, exportSchema = false)
public abstract class ApplicationDatabase extends RoomDatabase {
    public abstract RecentDataDao recentDataDao();
    public abstract BookmarkDataDao bookmarkDataDao();
}
