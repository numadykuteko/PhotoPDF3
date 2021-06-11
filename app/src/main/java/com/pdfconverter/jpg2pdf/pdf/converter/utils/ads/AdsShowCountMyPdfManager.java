package com.pdfconverter.jpg2pdf.pdf.converter.utils.ads;

import android.content.Context;

public class AdsShowCountMyPdfManager {
    private static final String LOG = "AdsShowCountMyPdfManager";

    private static AdsShowCountMyPdfManager mInstance;
    private int mCountForClickItem;
    private final int NUMBER_TIME_TO_SHOW_ONCE = 1;

    private AdsShowCountMyPdfManager() {
        mCountForClickItem = 0;
    }

    public static AdsShowCountMyPdfManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AdsShowCountMyPdfManager();
        }

        return mInstance;
    }


    public boolean checkShowAdsForClickItem() {
        return mCountForClickItem == NUMBER_TIME_TO_SHOW_ONCE;
    }

    public void increaseCountForClickItem() {
        if (mCountForClickItem == NUMBER_TIME_TO_SHOW_ONCE) {
            mCountForClickItem = 0;
        } else {
            mCountForClickItem ++;
        }
    }

}
