package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

public class ImageData {

    private String mImagePath;
    private String mImageName;
    private long mTimeCreate;
    private boolean mModified = false;
    private long mId;

    public ImageData(String mImagePath, String mImageName, long mTimeCreate, long id) {
        this.mImagePath = mImagePath;
        this.mImageName = mImageName;
        this.mTimeCreate = mTimeCreate;
        this.mId = id;
    }

    public ImageData() {

    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public String getImageName() {
        return mImageName;
    }

    public void setImageName(String mImageName) {
        this.mImageName = mImageName;
    }

    public long getTimeCreate() {
        return mTimeCreate;
    }

    public void setTimeCreate(long mTimeCreate) {
        this.mTimeCreate = mTimeCreate;
    }

    public boolean isModified() {
        return mModified;
    }

    public void setModified(boolean mModified) {
        this.mModified = mModified;
    }
}
