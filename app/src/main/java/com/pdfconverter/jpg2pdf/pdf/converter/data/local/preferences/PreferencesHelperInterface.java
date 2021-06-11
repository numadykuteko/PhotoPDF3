package com.pdfconverter.jpg2pdf.pdf.converter.data.local.preferences;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ViewPdfOption;

public interface PreferencesHelperInterface {
    int getOpenBefore();
    void setOpenBefore(int opened);

    int getRatingUs();
    void setRatingUs(int rated);

    void setShowGuideConvert(int shown);
    int getShowGuideConvert();

    void setShowGuideSelectMulti(int shown);
    int getShowGuideSelectMulti();

    int getTheme();
    void setTheme(int theme);

    int getBackTime();
    void setBackTime(int time);

    ExcelToPDFOptions getExcelToPDFOptions();
    void saveExcelToPDFOptions(ExcelToPDFOptions excelToPDFOptions);

    ImageToPDFOptions getImageToPDFOptions();
    void saveImageToPDFOptions(ImageToPDFOptions imageToPDFOptions);

    TextToPDFOptions getTextToPDFOptions();
    void saveTextToPDFOptions(TextToPDFOptions textToPDFOptions);

    ViewPdfOption getViewPDFOptions();
    void saveViewPDFOptions(ViewPdfOption viewPdfOption);
}
