package com.pdfconverter.jpg2pdf.pdf.converter.data.local.database;

import android.content.Context;

import androidx.room.Room;

import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;

import java.util.List;

import io.reactivex.Observable;

public class DatabaseHelper implements DatabaseHelperInterface {
    private static DatabaseHelper mInstance;
    private final ApplicationDatabase mApplicationDatabase;

    private DatabaseHelper(Context context) {
        mApplicationDatabase = Room.databaseBuilder(context, ApplicationDatabase.class, DataConstants.DATABASE_NAME).fallbackToDestructiveMigration()
                .build();
    }

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            return new DatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public Observable<Boolean> saveRecent(RecentData recentData) {
        return Observable.fromCallable(() -> {
            recentData.setTimeAdded(System.currentTimeMillis() / 1000L);
            mApplicationDatabase.recentDataDao().insert(recentData);
            return true;
        });
    }

    @Override
    public Observable<List<RecentData>> getListRecent() {
        return mApplicationDatabase.recentDataDao().loadAll().toObservable();
    }

    @Override
    public Observable<Boolean> clearRecent(String filePath) {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.recentDataDao().deleteByPath(filePath);
            return true;
        });
    }

    @Override
    public Observable<RecentData> getRecentByPath(String filePath) {
        return mApplicationDatabase.recentDataDao().findByPath(filePath).toObservable();
    }

    @Override
    public Observable<Boolean> saveBookmark(BookmarkData bookmarkData) {
        return Observable.fromCallable(() -> {
            bookmarkData.setTimeAdded(System.currentTimeMillis() / 1000L);
            mApplicationDatabase.bookmarkDataDao().insert(bookmarkData);
            return true;
        });
    }

    @Override
    public Observable<List<BookmarkData>> getListBookmark() {
        return mApplicationDatabase.bookmarkDataDao().loadAll().toObservable();
    }

    @Override
    public Observable<BookmarkData>  getBookmarkByPath(String path) {
        return mApplicationDatabase.bookmarkDataDao().findByPath(path).toObservable();
    }

    @Override
    public Observable<Boolean> clearBookmarkByPath(String path) {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.bookmarkDataDao().deleteByPath(path);
            return true;
        });
    }

    @Override
    public Observable<Boolean> clearAllBookmark() {
        return Observable.fromCallable(() -> {
            mApplicationDatabase.bookmarkDataDao().deleteAll();
            return true;
        });
    }
}
