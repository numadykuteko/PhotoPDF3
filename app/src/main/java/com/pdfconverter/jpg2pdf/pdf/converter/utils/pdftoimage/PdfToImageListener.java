package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftoimage;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageExtractData;

public interface PdfToImageListener {
    void onCreateStart(int type);
    void onCreateFinish(int numberSuccess, int numberError, int type);
    void onUpdateProcess(int numberSuccess, int numberError, ImageExtractData output, int type);
}
