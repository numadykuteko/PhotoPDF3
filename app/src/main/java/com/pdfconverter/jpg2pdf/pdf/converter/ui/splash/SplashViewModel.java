package com.pdfconverter.jpg2pdf.pdf.converter.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;

import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

public class SplashViewModel extends BaseViewModel<SplashNavigator> {
    private static final String TAG = "SplashViewModel";

    public SplashViewModel(@NonNull Application application) {
        super(application);
    }
}
