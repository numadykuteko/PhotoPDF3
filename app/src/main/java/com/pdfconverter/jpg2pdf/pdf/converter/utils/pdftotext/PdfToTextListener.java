package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftotext;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextExtractData;

public interface PdfToTextListener {
    void onCreateStart();
    void onCreateFinish(int numberSuccess, int numberError);
    void onUpdateProcess(int numberSuccess, int numberError, TextExtractData output);
}
