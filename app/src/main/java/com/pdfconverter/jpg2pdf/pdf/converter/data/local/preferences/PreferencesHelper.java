package com.pdfconverter.jpg2pdf.pdf.converter.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ViewPdfOption;

public class PreferencesHelper implements PreferencesHelperInterface {
    private static PreferencesHelper mInstance;
    private final SharedPreferences mPrefs;

    private PreferencesHelper(Context context) {
        mPrefs = context.getSharedPreferences(DataConstants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferencesHelper getInstance(Context context) {
        if (mInstance == null) {
            return new PreferencesHelper(context);
        }
        return mInstance;
    }

    @Override
    public void setRatingUs(int rated) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_RATING_US, rated).apply();
    }

    @Override
    public int getRatingUs() {
        return mPrefs.getInt(DataConstants.PREF_NAME_RATING_US, 0);
    }

    @Override
    public void setShowGuideConvert(int shown) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_SHOW_GUIDE_CONVERT, shown).apply();
    }

    @Override
    public int getShowGuideConvert() {
        return mPrefs.getInt(DataConstants.PREF_NAME_SHOW_GUIDE_CONVERT, 0);
    }

    @Override
    public int getOpenBefore() {
        return mPrefs.getInt(DataConstants.PREF_NAME_OPEN_BEFORE, 0);
    }

    @Override
    public void setOpenBefore(int opened) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_OPEN_BEFORE, opened).apply();
    }

    @Override
    public void setShowGuideSelectMulti(int shown) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_SHOW_GUIDE, shown).apply();
    }

    @Override
    public int getShowGuideSelectMulti() {
        return mPrefs.getInt(DataConstants.PREF_NAME_SHOW_GUIDE, 0);
    }

    @Override
    public int getTheme() {
        return mPrefs.getInt(DataConstants.PREF_NAME_THEME, OptionConstants.DEFAULT_THEME);
    }

    @Override
    public void setTheme(int theme) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_THEME, theme).apply();
    }

    @Override
    public int getBackTime() {
        return mPrefs.getInt(DataConstants.PREF_NAME_BACK_TIME, 0);
    }

    @Override
    public void setBackTime(int time) {
        mPrefs.edit().putInt(DataConstants.PREF_NAME_BACK_TIME, time).apply();
    }

    @Override
    public ExcelToPDFOptions getExcelToPDFOptions() {
        try {
            Gson gson = new Gson();
            String json = mPrefs.getString(DataConstants.PREF_NAME_OPTION_EXCEL, "");

            return gson.fromJson(json, ExcelToPDFOptions.class);
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void saveExcelToPDFOptions(ExcelToPDFOptions excelToPDFOptions) {
        Gson gson = new Gson();
        String json = gson.toJson(excelToPDFOptions);
        mPrefs.edit().putString(DataConstants.PREF_NAME_OPTION_EXCEL, json).apply();
    }

    @Override
    public ImageToPDFOptions getImageToPDFOptions() {
        try {
            Gson gson = new Gson();
            String json = mPrefs.getString(DataConstants.PREF_NAME_OPTION_IMAGE, "");

            return gson.fromJson(json, ImageToPDFOptions.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveImageToPDFOptions(ImageToPDFOptions imageToPDFOptions) {
        Gson gson = new Gson();
        String json = gson.toJson(imageToPDFOptions);
        mPrefs.edit().putString(DataConstants.PREF_NAME_OPTION_IMAGE, json).apply();
    }

    @Override
    public TextToPDFOptions getTextToPDFOptions() {
        try {
            Gson gson = new Gson();
            String json = mPrefs.getString(DataConstants.PREF_NAME_OPTION_TEXT, "");

            return gson.fromJson(json, TextToPDFOptions.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveTextToPDFOptions(TextToPDFOptions textToPDFOptions) {
        Gson gson = new Gson();
        String json = gson.toJson(textToPDFOptions);
        mPrefs.edit().putString(DataConstants.PREF_NAME_OPTION_TEXT, json).apply();
    }

    @Override
    public ViewPdfOption getViewPDFOptions() {
        try {
            Gson gson = new Gson();
            String json = mPrefs.getString(DataConstants.PREF_NAME_OPTION_VIEW_PDF, "");

            return gson.fromJson(json, ViewPdfOption.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveViewPDFOptions(ViewPdfOption viewPdfOption) {
        Gson gson = new Gson();
        String json = gson.toJson(viewPdfOption);
        mPrefs.edit().putString(DataConstants.PREF_NAME_OPTION_VIEW_PDF, json).apply();
    }
}
