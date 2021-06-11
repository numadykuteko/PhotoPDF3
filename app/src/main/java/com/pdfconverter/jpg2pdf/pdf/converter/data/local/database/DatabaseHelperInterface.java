package com.pdfconverter.jpg2pdf.pdf.converter.data.local.database;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;

import java.util.List;

import io.reactivex.Observable;

public interface DatabaseHelperInterface {
    Observable<Boolean> saveRecent(RecentData recentData);
    Observable<List<RecentData>> getListRecent();
    Observable<Boolean> clearRecent(String filePath);
    Observable<RecentData> getRecentByPath(String filePath);

    Observable<Boolean> saveBookmark(BookmarkData bookmarkData);
    Observable<List<BookmarkData>> getListBookmark();
    Observable<BookmarkData>  getBookmarkByPath(String path);
    Observable<Boolean> clearBookmarkByPath(String path);
    Observable<Boolean> clearAllBookmark();
}
