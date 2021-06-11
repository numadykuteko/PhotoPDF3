package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

public class PDFToImageOptions {

    private String inputFilePath;
    private DocumentData inputFileData;
    private int startPage;
    private int endPage;
    private int type;
    private int numberPage;

    public PDFToImageOptions(String inputFilePath, DocumentData inputFileData, int startPage, int endPage, int numberPage, int type) {
        this.inputFilePath = inputFilePath;
        this.inputFileData = inputFileData;
        this.startPage = startPage;
        this.endPage = endPage;
        this.type = type;
        this.numberPage = numberPage;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public DocumentData getInputFileData() {
        return inputFileData;
    }

    public void setInputFileData(DocumentData inputFileData) {
        this.inputFileData = inputFileData;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumberPage() {
        return numberPage;
    }

    public void setNumberPage(int numberPage) {
        this.numberPage = numberPage;
    }
}
