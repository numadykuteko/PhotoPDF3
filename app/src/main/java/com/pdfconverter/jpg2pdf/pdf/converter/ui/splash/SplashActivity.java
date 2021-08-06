package com.pdfconverter.jpg2pdf.pdf.converter.ui.splash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivitySplashBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.firstopen.FirstOpenActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.main.MainActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.viewpdf.ViewPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseRemoteUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.NetworkUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseBindingActivity<ActivitySplashBinding, SplashViewModel> implements SplashNavigator {
    private SplashViewModel mSplashViewModel;
    private ActivitySplashBinding mActivitySplashBinding;

    private boolean mIsFromOpenPdf = false;
    private String mFilePdfPath = null;
    private boolean mIsGoAway = false;

    private InterstitialAd mInterstitialAd;
    private Timer mTimer;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public SplashViewModel getViewModel() {
        mSplashViewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isNeedToSetTheme = false;
        super.onCreate(savedInstanceState);

        mActivitySplashBinding = getViewDataBinding();
        mSplashViewModel.setNavigator(this);

        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        firebaseRemoteUtils.fetchRemoteConfig(this, () -> {});

        precheckIntentFilter();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroy();
    }

    private void gotoTargetActivity() {
        if (mIsGoAway) return;

        mIsGoAway = true;

        Intent intent;
        if (!mIsFromOpenPdf) {
            if (DataManager.getInstance(this).isOpenBefore()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, FirstOpenActivity.class);
                DataManager.getInstance(this).setOpenBefore();
            }
        } else {
            FirebaseUtils.sendEventFunctionUsed(this, "Open file from other app", "From splash");

            intent = new Intent(SplashActivity.this, ViewPdfActivity.class);
            if (mFilePdfPath != null) {
                intent.putExtra(EXTRA_FILE_PATH, mFilePdfPath);
                intent.putExtra(EXTRA_FROM_SPLASH, true);
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void precheckIntentFilter() {
        Intent intent = getIntent();

        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            String filepath = null;

            if (Intent.ACTION_VIEW.equals(action) && type != null && type.endsWith("pdf")) {
                Uri fileUri = intent.getData();
                if (fileUri != null) {
                    filepath = RealPathUtil.getInstance().getRealPath(this, fileUri, FileUtils.FileType.type_PDF);
                }

                mIsFromOpenPdf = true;
            } else if (Intent.ACTION_SEND.equals(action) && type != null && type.endsWith("pdf")) {
                Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    filepath = RealPathUtil.getInstance().getRealPath(this, fileUri, FileUtils.FileType.type_PDF);
                }

                mIsFromOpenPdf = true;
            }

            if (filepath != null) {
                mFilePdfPath = filepath;
            }
        } else {
            mIsFromOpenPdf = false;
        }

        prepareShowAds();
    }

    private void prepareShowAds() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    gotoTargetActivity();
                }
            }, 1000);
            return;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    mInterstitialAd.setAdListener(new AdListener());
                    gotoTargetActivity();
                });
            }
        }, 18000);

        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(mIsFromOpenPdf ? BuildConfig.full_view_pdf_from_other_id : BuildConfig.full_splash_id);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                mInterstitialAd.setAdListener(new AdListener());
                gotoTargetActivity();
            }

            @Override
            public void onAdOpened() {
                if (mTimer != null) {
                    mTimer.cancel();
                }
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.setAdListener(new AdListener());
                gotoTargetActivity();
            }

        });
        mInterstitialAd.loadAd(adRequest);
    }
}
