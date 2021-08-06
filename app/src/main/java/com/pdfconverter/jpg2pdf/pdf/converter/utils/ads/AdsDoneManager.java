package com.pdfconverter.jpg2pdf.pdf.converter.utils.ads;

import android.content.Context;

import com.ads.control.Admod;
import com.google.android.gms.ads.InterstitialAd;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;

public class AdsDoneManager {
    private InterstitialAd mDoneInterstitialAd;
    private static AdsDoneManager mInstance;
    private Context mContext;

    private AdsDoneManager(Context context) {
        mContext = context;
        mDoneInterstitialAd = Admod.getInstance().getInterstitalAds(mContext, BuildConfig.full_done_id);
    }

    public static AdsDoneManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AdsDoneManager(context);
        }

        return mInstance;
    }

    public InterstitialAd getDoneInterstitialAd() {
        if (mDoneInterstitialAd == null) {
            mDoneInterstitialAd = Admod.getInstance().getInterstitalAds(mContext, BuildConfig.full_done_id);
        }

        return mDoneInterstitialAd;
    }

}
