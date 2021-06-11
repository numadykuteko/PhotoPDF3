package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;

public class DocumentData implements Serializable {
    private String displayName;
    private String filePath;

    private transient Uri fileUri;

    public DocumentData(String displayName, Uri fileUri, String filePath) {
        this.displayName = displayName;
        this.fileUri = fileUri;
        this.filePath = filePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Uri getFileUri() {
        if (fileUri == null) {
            try {
                return Uri.fromFile(new File(filePath));
            } catch (Exception e) {
                return null;
            }
        }
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
