package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import java.io.Serializable;
import java.util.List;

public class TextToPDFOptions extends PDFOptions implements Serializable {
    private List<DocumentData> mInputFileUri;
    private int mFontSize;
    private String mFontFamily;

    public TextToPDFOptions() {

    }

    public TextToPDFOptions(String mFileName, String mPageSize, boolean mPasswordProtected,
                            String mPassword, List<DocumentData> mInputFileUri, int mFontSize, String mFontFamily) {
        super(mFileName, mPageSize, mPasswordProtected, mPassword, 0, false, null, 0);
        this.mInputFileUri = mInputFileUri;
        this.mFontSize = mFontSize;
        this.mFontFamily = mFontFamily;
    }

    public List<DocumentData> getInputFileUri() {
        return mInputFileUri;
    }

    public void setInputFileUri(List<DocumentData> mInputFileUri) {
        this.mInputFileUri = mInputFileUri;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public void setFontSize(int mFontSize) {
        this.mFontSize = mFontSize;
    }

    public String getFontFamily() {
        return mFontFamily;
    }

    public void setFontFamily(String mFontFamily) {
        this.mFontFamily = mFontFamily;
    }
}
