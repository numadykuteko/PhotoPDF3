package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import android.graphics.Bitmap;

public class PageData {
    private int sourcePage;
    private Bitmap bitmap;

    public PageData(int sourcePage, Bitmap bitmap) {
        this.sourcePage = sourcePage;
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(int sourcePage) {
        this.sourcePage = sourcePage;
    }
}
