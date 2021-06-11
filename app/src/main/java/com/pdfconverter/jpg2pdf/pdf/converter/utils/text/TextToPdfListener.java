package com.pdfconverter.jpg2pdf.pdf.converter.utils.text;

public interface TextToPdfListener {
    void onCreateStart();
    void onCreateError();
    void onCreateSuccess(String finalOutput);
    void onUpdateProcess(int progress);
}
