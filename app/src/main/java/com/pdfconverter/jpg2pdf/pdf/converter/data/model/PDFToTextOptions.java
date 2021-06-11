package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

public class PDFToTextOptions {

    private String inputFilePath;
    private int startPage;
    private int endPage;

    public PDFToTextOptions(String inputFilePath, int startPage, int endPage) {
        this.inputFilePath = inputFilePath;
        this.startPage = startPage;
        this.endPage = endPage;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }
}
