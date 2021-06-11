package com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf;

public interface EditPdfListener {
    void onEditSuccess(String outputPath);
    void onEditFail();
    void onEditStart();
}
