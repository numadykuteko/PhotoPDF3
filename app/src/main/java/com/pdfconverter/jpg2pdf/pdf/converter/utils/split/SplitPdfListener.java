package com.pdfconverter.jpg2pdf.pdf.converter.utils.split;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitFileData;

public interface SplitPdfListener {
    void onCreateStart();
    void onCreateFinish(int numberSuccess, int numberError);
    void onUpdateProcess(int numberSuccess, int numberError, SplitFileData output);
}
