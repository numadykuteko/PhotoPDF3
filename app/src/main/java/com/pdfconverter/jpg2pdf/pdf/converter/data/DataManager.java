package com.pdfconverter.jpg2pdf.pdf.converter.data;

import android.content.Context;

import com.pdfconverter.jpg2pdf.pdf.converter.data.local.database.DatabaseHelper;
import com.pdfconverter.jpg2pdf.pdf.converter.data.local.preferences.PreferencesHelper;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ViewPdfOption;
import com.pdfconverter.jpg2pdf.pdf.converter.data.remote.ApiHelper;

import java.util.List;

import io.reactivex.Observable;

public class DataManager implements DataManagerInterface {
    private static DataManager mInstance;

    private final ApiHelper mApiHelper;
    private final DatabaseHelper mDatabaseHelper;
    private final PreferencesHelper mPreferencesHelper;

    private DataManager(Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
        mPreferencesHelper = PreferencesHelper.getInstance(context);
        mApiHelper = ApiHelper.getInstance();
    }

    public static DataManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DataManager(context);
        }
        return mInstance;
    }

    @Override
    public boolean isOpenBefore() {
        return mPreferencesHelper.getOpenBefore() != 0;
    }

    @Override
    public void setOpenBefore() {
        mPreferencesHelper.setOpenBefore(1);
    }

    @Override
    public boolean checkRatingUsDone() {
        return mPreferencesHelper.getRatingUs() != 0;
    }

    @Override
    public void setRatingUsDone() {
        mPreferencesHelper.setRatingUs(1);
    }

    @Override
    public void setShowGuideConvertDone() {
        mPreferencesHelper.setShowGuideConvert(1);
    }

    @Override
    public boolean getShowGuideConvert() {
        return mPreferencesHelper.getShowGuideConvert() != 0;
    }

    @Override
    public void setShowGuideSelectMultiDone() {
        mPreferencesHelper.setShowGuideSelectMulti(1);
    }

    @Override
    public boolean getShowGuideSelectMulti() {
        return mPreferencesHelper.getShowGuideSelectMulti() == 0;
    }

    @Override
    public int getTheme() {
        return mPreferencesHelper.getTheme();
    }

    @Override
    public void setTheme(int theme) {
        mPreferencesHelper.setTheme(theme);
    }

    @Override
    public int getBackTime() {
        return mPreferencesHelper.getBackTime();
    }

    @Override
    public void increaseBackTime() {
        int time = mPreferencesHelper.getBackTime() + 1;
        mPreferencesHelper.setBackTime(time);
    }

    @Override
    public ExcelToPDFOptions getExcelToPDFOptions() {
        return mPreferencesHelper.getExcelToPDFOptions();
    }

    @Override
    public void saveExcelToPDFOptions(ExcelToPDFOptions excelToPDFOptions) {
        mPreferencesHelper.saveExcelToPDFOptions(excelToPDFOptions);
    }

    @Override
    public ImageToPDFOptions getImageToPDFOptions() {
        return mPreferencesHelper.getImageToPDFOptions();
    }

    @Override
    public void saveImageToPDFOptions(ImageToPDFOptions imageToPDFOptions) {
        mPreferencesHelper.saveImageToPDFOptions(imageToPDFOptions);
    }

    @Override
    public TextToPDFOptions getTextToPDFOptions() {
        return mPreferencesHelper.getTextToPDFOptions();
    }

    @Override
    public void saveTextToPDFOptions(TextToPDFOptions textToPDFOptions) {
        mPreferencesHelper.saveTextToPDFOptions(textToPDFOptions);
    }

    @Override
    public ViewPdfOption getViewPDFOptions() {
        return mPreferencesHelper.getViewPDFOptions();
    }

    @Override
    public void saveViewPDFOptions(ViewPdfOption viewPdfOption) {
        mPreferencesHelper.saveViewPDFOptions(viewPdfOption);
    }

    @Override
    public Observable<Boolean> saveRecent(RecentData recentData) {
        return mDatabaseHelper.saveRecent(recentData);
    }

    @Override
    public Observable<Boolean> saveRecent(String filePath, String type) {
        RecentData recentData = new RecentData();
        recentData.setFilePath(filePath);
        recentData.setActionType(type);
        return mDatabaseHelper.saveRecent(recentData);
    }

    @Override
    public Observable<Boolean> clearRecent(String filePath) {
        return mDatabaseHelper.clearRecent(filePath);
    }

    @Override
    public Observable<RecentData> getRecentByPath(String filePath) {
        return mDatabaseHelper.getRecentByPath(filePath);
    }

    @Override
    public Observable<List<RecentData>> getListRecent() {
        return mDatabaseHelper.getListRecent();
    }

    @Override
    public Observable<Boolean> saveBookmark(BookmarkData bookmarkData) {
        return mDatabaseHelper.saveBookmark(bookmarkData);
    }

    @Override
    public Observable<Boolean> saveBookmark(String filePath) {
        BookmarkData bookmarkData = new BookmarkData();
        bookmarkData.setFilePath(filePath);
        return mDatabaseHelper.saveBookmark(bookmarkData);
    }

    @Override
    public Observable<List<BookmarkData>> getListBookmark() {
        return mDatabaseHelper.getListBookmark();
    }

    @Override
    public Observable<BookmarkData> getBookmarkByPath(String path) {
        return mDatabaseHelper.getBookmarkByPath(path);
    }

    @Override
    public Observable<Boolean> clearBookmarkByPath(String path) {
        return mDatabaseHelper.clearBookmarkByPath(path);
    }

    @Override
    public Observable<Boolean> clearAllBookmark() {
        return mDatabaseHelper.clearAllBookmark();
    }
}
