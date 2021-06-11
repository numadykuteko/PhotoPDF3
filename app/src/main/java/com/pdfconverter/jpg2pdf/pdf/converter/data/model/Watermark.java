package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import com.itextpdf.text.BaseColor;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;

import java.io.Serializable;

public class Watermark implements Serializable {
    private DocumentData mDocumentData;
    private String mFilePath;
    private String mWatermarkText = "";
    private int mRotationAngle = 0;
    private BaseColor mTextColor = new BaseColor(0, 0, 0);
    private int mTextSize = OptionConstants.DEFAULT_FONT_SIZE;
    private String mFontFamily = OptionConstants.DEFAULT_FONT_FAMILY;
    private int mFontStyle = OptionConstants.DEFAULT_FONT_STYLE;
    private int mPosition = OptionConstants.DEFAULT_FONT_SIZE;

    public String getWatermarkText() {
        return mWatermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.mWatermarkText = watermarkText;
    }

    public int getRotationAngle() {
        return mRotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.mRotationAngle = rotationAngle;
    }

    public BaseColor getTextColor() {
        return mTextColor;
    }

    public void setTextColor(BaseColor textColor) {
        this.mTextColor = textColor;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
    }

    public String getFontFamily() {
        return mFontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.mFontFamily = fontFamily;
    }

    public int getFontStyle() {
        return mFontStyle;
    }

    public void setFontStyle(int fontStyle) {
        this.mFontStyle = fontStyle;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mFontPosition) {
        this.mPosition = mFontPosition;
    }

    public DocumentData getDocumentData() {
        return mDocumentData;
    }

    public void setDocumentData(DocumentData mDocumentData) {
        this.mDocumentData = mDocumentData;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String mFilePath) {
        this.mFilePath = mFilePath;
    }
}
