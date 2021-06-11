package com.pdfconverter.jpg2pdf.pdf.converter.utils.merge;

public interface MergePdfListener {
    void onCreateStart();
    void onCreateError();
    void onCreateSuccess(String finalOutput);
    void onUpdateProcess(int progress);
}
