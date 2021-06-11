package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

public class ImageExtractData {
    String fileName;
    String filePath;
    int page;

    public ImageExtractData(String fileName, String filePath, int page) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.page = page;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
