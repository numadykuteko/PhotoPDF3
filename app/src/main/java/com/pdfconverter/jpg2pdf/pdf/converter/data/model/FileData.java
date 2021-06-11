package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import android.net.Uri;

public class FileData {
    private String displayName;
    private Uri fileUri;
    private int dateAdded;
    private int size;
    private String fileType;
    private String filePath;
    private FileData parentFile;

    public FileData() {

    }

    public FileData(String displayName, String filePath, Uri fileUri, int dateAdded, int size, String fileType) {
        this.displayName = displayName;
        this.fileUri = fileUri;
        this.dateAdded = dateAdded;
        this.fileType = fileType;
        this.size = size;
        this.filePath = filePath;
    }

    public FileData(FileData copy) {
        this.displayName = copy.displayName;
        this.fileUri = copy.fileUri;
        this.dateAdded = copy.dateAdded;
        this.fileType = copy.fileType;
        this.size = copy.size;
        this.filePath = copy.filePath;
        this.parentFile = copy.parentFile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public int getTimeAdded() {
        return dateAdded;
    }

    public void setDateAdded(int dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FileData getParentFile() {
        return parentFile;
    }

    public void setParentFile(FileData parentFile) {
        this.parentFile = parentFile;
    }
}
