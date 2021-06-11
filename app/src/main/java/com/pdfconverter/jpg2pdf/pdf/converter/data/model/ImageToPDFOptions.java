package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ImageToPDFOptions extends PDFOptions implements Serializable {

    private String mQualityString;
    private ArrayList<String> mImagesUri;
    private int mMarginTop = 0;
    private int mMarginBottom = 0;
    private int mMarginRight = 0;
    private int mMarginLeft = 0;
    private String mImageScaleType;
    private String mPageNumStyle;
    private String mMasterPwd;
    private String mCompress;

    public ImageToPDFOptions() {
        super();
        setPasswordProtected(false);
        setWatermarkAdded(false);
        setBorderWidth(0);
    }

    public ImageToPDFOptions(String mFileName, String mPageSize, boolean mPasswordProtected,
                             String mPassword, String mQualityString, int mBorderWidth,
                             String masterPwd, ArrayList<String> mImagesUri,
                             boolean mWatermarkAdded, String mWatermark, int pageColor, String compress) {
        super(mFileName, mPageSize, mPasswordProtected, mPassword, mBorderWidth, mWatermarkAdded, mWatermark,
                pageColor);
        this.mQualityString = mQualityString;
        this.mImagesUri = mImagesUri;
        this.mMasterPwd = masterPwd;
        this.mCompress = compress;
    }

    public String getCompress() {
        return mCompress;
    }

    public void setCompress(String mCompress) {
        this.mCompress = mCompress;
    }

    public String getQualityString() {
        return mQualityString;
    }

    public ArrayList<String> getImagesUri() {
        return mImagesUri;
    }

    public void setQualityString(String mQualityString) {
        this.mQualityString = mQualityString;
    }

    public void setImagesUri(ArrayList<String> mImagesUri) {
        this.mImagesUri = mImagesUri;
    }

    public void setMargins(int top, int bottom, int right, int left) {
        mMarginTop = top;
        mMarginBottom = bottom;
        mMarginRight = right;
        mMarginLeft = left;
    }

    public void setMasterPwd(String pwd) {
        this.mMasterPwd = pwd;
    }

    public int getMarginTop() {
        return mMarginTop;
    }

    public int getMarginBottom() {
        return mMarginBottom;
    }

    public int getMarginRight() {
        return mMarginRight;
    }

    public int getMarginLeft() {
        return mMarginLeft;
    }

    public String getImageScaleType() {
        return mImageScaleType;
    }

    public void setImageScaleType(String mImageScaleType) {
        this.mImageScaleType = mImageScaleType;
    }

    public String getPageNumStyle() {
        return mPageNumStyle;
    }

    public void setPageNumStyle(String mPageNumStyle) {
        this.mPageNumStyle = mPageNumStyle;
    }

    public String getMasterPwd() {
        return mMasterPwd;
    }

    public void setMarginTop(int mMarginTop) {
        this.mMarginTop = mMarginTop;
    }

    public void setMarginBottom(int mMarginBottom) {
        this.mMarginBottom = mMarginBottom;
    }

    public void setMarginRight(int mMarginRight) {
        this.mMarginRight = mMarginRight;
    }

    public void setMarginLeft(int mMarginLeft) {
        this.mMarginLeft = mMarginLeft;
    }
}
