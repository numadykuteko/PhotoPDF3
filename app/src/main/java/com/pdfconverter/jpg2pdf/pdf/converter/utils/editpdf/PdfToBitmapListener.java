package com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;

import java.util.ArrayList;

public interface PdfToBitmapListener {
    void onReadSuccess(ArrayList<PageData> bitmaps);
    void onReadFail();
    void onReadStart();
}
