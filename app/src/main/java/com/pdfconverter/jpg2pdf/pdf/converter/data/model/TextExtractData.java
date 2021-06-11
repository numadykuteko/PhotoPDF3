package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

public class TextExtractData {
    String textContent;
    int page;

    public TextExtractData(String textContent, int page) {
        this.page = page;
        this.textContent = textContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
