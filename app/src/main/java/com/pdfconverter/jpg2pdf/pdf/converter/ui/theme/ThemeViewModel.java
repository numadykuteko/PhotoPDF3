package com.pdfconverter.jpg2pdf.pdf.converter.ui.theme;

import android.app.Application;

import androidx.annotation.NonNull;

import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

public class ThemeViewModel extends BaseViewModel<ThemeNavigator> {
    public ThemeViewModel(@NonNull Application application) {
        super(application);
    }

    public int getSelectedTheme() {
        return getDataManager().getTheme();
    }

    public void setSelectedTheme(int theme) {
        getDataManager().setTheme(theme);
    }
}
