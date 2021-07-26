package com.pdfconverter.jpg2pdf.pdf.converter;

import android.app.Application;

import com.ads.control.AppPurchase;
import com.google.android.gms.ads.MobileAds;

public class PdfApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppPurchase.getInstance().initBilling(this);
        AppPurchase.getInstance().addSubcriptionId(BuildConfig.monthly_purchase_key);
        AppPurchase.getInstance().addSubcriptionId(BuildConfig.yearly_purchase_key);

        MobileAds.initialize(
                this,
                initializationStatus -> {});
    }
}
