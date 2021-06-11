package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import java.util.ArrayList;

public class SplitFileData {
    String fileName;
    String filePath;
    ArrayList<Integer> pageList;
    boolean isCreated;

    public SplitFileData(String fileName, String filePath, int startPage, int endPage, boolean isCreated) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isCreated = isCreated;

        this.pageList = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            this.pageList.add(i);
        }
    }

    public SplitFileData(String fileName, String filePath, ArrayList<Integer> pageList, boolean isCreated) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isCreated = isCreated;
        this.pageList = new ArrayList<>();
        this.pageList.addAll(pageList);
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

    public boolean isCreated() {
        return isCreated;
    }

    public void setCreated(boolean created) {
        isCreated = created;
    }

    public ArrayList<Integer> getPageList() {
        return pageList;
    }

    public void setPageList(ArrayList<Integer> pageList) {
        this.pageList = pageList;
    }
}
