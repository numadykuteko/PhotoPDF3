package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class WatermarkPageEvent extends PdfPageEventHelper {
    private String mWatermark;
    private Phrase mPhrase;

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();
        float x = (document.getPageSize().getLeft() + document.getPageSize().getRight()) / 2;
        float y = (document.getPageSize().getTop() + document.getPageSize().getBottom()) / 2;
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, mPhrase, x, y, 45);
    }

    public String getWatermark() {
        return mWatermark;
    }

    public void setWatermark(String watermark) {
        this.mWatermark = watermark;
        this.mPhrase = new Phrase(mWatermark,
                new Font(Font.FontFamily.TIMES_ROMAN, 45F, 1, new BaseColor(34, 178, 189)));
    }
}
