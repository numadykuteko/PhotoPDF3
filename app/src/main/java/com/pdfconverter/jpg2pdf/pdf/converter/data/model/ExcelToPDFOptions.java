package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import java.io.Serializable;
import java.util.List;

public class ExcelToPDFOptions implements Serializable {
    private List<DocumentData> pathList;
    private String outFileName;
    private boolean isPasswordProtected;
    private String password;
    private int pageSize;
    private boolean isOneSheetOnePage;
    private String pageOrientation;

    public ExcelToPDFOptions() {

    }

    public ExcelToPDFOptions(List<DocumentData> pathList, String outFileName, boolean isPasswordProtected, String password, int pageSize, String pageOrientation, boolean isOneSheetOnePage) {
        this.pathList = pathList;
        this.outFileName = outFileName;
        this.isPasswordProtected = isPasswordProtected;
        this.password = password;
        this.pageSize = pageSize;
        this.isOneSheetOnePage = isOneSheetOnePage;
        this.pageOrientation = pageOrientation;
    }

    public List<DocumentData> getPathList() {
        return pathList;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public boolean isPasswordProtected() {
        return isPasswordProtected;
    }

    public String getPassword() {
        return password;
    }

    public void setPathList(List<DocumentData> pathList) {
        this.pathList = pathList;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        isPasswordProtected = passwordProtected;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int mPageSize) {
        this.pageSize = mPageSize;
    }

    public boolean isOneSheetOnePage() {
        return isOneSheetOnePage;
    }

    public void setOneSheetOnePage(boolean oneSheetOnePage) {
        isOneSheetOnePage = oneSheetOnePage;
    }

    public String getPageOrientation() {
        return pageOrientation;
    }

    public void setPageOrientation(String pageOrientation) {
        this.pageOrientation = pageOrientation;
    }
}
