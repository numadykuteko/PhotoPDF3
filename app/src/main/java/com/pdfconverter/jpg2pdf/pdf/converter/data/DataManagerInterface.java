package com.pdfconverter.jpg2pdf.pdf.converter.data;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ViewPdfOption;

import java.util.List;

import io.reactivex.Observable;

public interface DataManagerInterface {
    boolean isOpenBefore();
    void setOpenBefore();

    // rate :
    void setRatingUsDone();
    boolean checkRatingUsDone();

    void setShowGuideConvertDone();
    boolean getShowGuideConvert();

    void setShowGuideSelectMultiDone();
    boolean getShowGuideSelectMulti();

    int getTheme();
    void setTheme(int theme);

    int getBackTime();
    void increaseBackTime();

    ExcelToPDFOptions getExcelToPDFOptions();
    void saveExcelToPDFOptions(ExcelToPDFOptions excelToPDFOptions);

    ImageToPDFOptions getImageToPDFOptions();
    void saveImageToPDFOptions(ImageToPDFOptions imageToPDFOptions);

    TextToPDFOptions getTextToPDFOptions();
    void saveTextToPDFOptions(TextToPDFOptions textToPDFOptions);

    Observable<Boolean> saveRecent(RecentData recentData);
    Observable<List<RecentData>> getListRecent();
    Observable<Boolean> saveRecent(String filePath, String type);
    Observable<Boolean> clearRecent(String filePath);
    Observable<RecentData> getRecentByPath(String filePath);

    Observable<Boolean> saveBookmark(BookmarkData bookmarkData);
    Observable<Boolean> saveBookmark(String filePath);
    Observable<List<BookmarkData>> getListBookmark();
    Observable<BookmarkData> getBookmarkByPath(String path);
    Observable<Boolean> clearBookmarkByPath(String path);
    Observable<Boolean> clearAllBookmark();

    ViewPdfOption getViewPDFOptions();
    void saveViewPDFOptions(ViewPdfOption viewPdfOption);
}
