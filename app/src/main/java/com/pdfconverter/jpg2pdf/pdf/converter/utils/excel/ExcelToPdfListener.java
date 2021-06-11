package com.pdfconverter.jpg2pdf.pdf.converter.utils.excel;

public interface ExcelToPdfListener {
    void onCreateStart();
    void onCreateError();
    void onCreateSuccess(String finalOutput);
    void onUpdateProcess(int progress);
}
