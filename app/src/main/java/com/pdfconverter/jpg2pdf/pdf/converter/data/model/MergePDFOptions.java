package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import java.util.List;

public class MergePDFOptions {
    private List<DocumentData> pathList;
    private String outFileName;
    private boolean isPasswordProtected;
    private String password;

    public MergePDFOptions() {

    }

    public MergePDFOptions(List<DocumentData> pathList, String outFileName, boolean isPasswordProtected, String password) {
        this.pathList = pathList;
        this.outFileName = outFileName;
        this.isPasswordProtected = isPasswordProtected;
        this.password = password;
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
}
