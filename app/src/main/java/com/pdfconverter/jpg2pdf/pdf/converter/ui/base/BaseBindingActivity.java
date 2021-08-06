package com.pdfconverter.jpg2pdf.pdf.converter.ui.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;

import androidx.annotation.IntegerRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.ads.control.Admod;
import com.ads.control.AppPurchase;
import com.ads.control.funtion.AdCallback;
import com.ads.control.funtion.PurchaseListioner;
import com.google.android.gms.ads.InterstitialAd;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.AddWaterMarkActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ConverterSelectDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PurchaseDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RemovePasswordFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SetPasswordFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf.EditPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.exceltopdf.ExcelToPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.ImageToPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.main.MainActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.mergepdf.MergePdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.protectpdf.ProtectPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.protectpdf.done.ProtectPdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.search.SearchActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.splash.SplashActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.texttopdf.TextToPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.theme.ThemeAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf.UnlockPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf.done.UnlockPdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.viewpdf.ViewPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CountryCodeHelper;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseRemoteUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.NetworkUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ads.AdsDoneManager;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;
import com.rate.control.OnCallback;
import com.rate.control.funtion.RateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseBindingActivity<T extends ViewDataBinding, V extends BaseViewModel>
        extends AppCompatActivity implements BaseFragment.Callback {

    protected static final int PICK_IMAGE_FOR_ADD_IMAGE_REQUEST = 2370;
    protected static final int PREVIEW_FILE_REQUEST = 2369;
    protected static final int PERMISSION_WRITE = 2368;
    protected static final int PICK_IMAGE_REQUEST = 2367;
    protected static final int CAMERA_REQUEST = 2366;
    protected static final int TAKE_FILE_REQUEST = 2365;
    protected static final int ADD_FILE_REQUEST = 2364;
    protected static final int SCAN_REQUEST = 2363;
    protected static final int CREATE_PDF_FROM_SELECT_FILE = 2362;
    protected static final int SCAN_DOCUMENT_AFTER_TAKEN_REQUEST = 2361;

    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    public static final String EXTRA_FILE_EXTENSION = "EXTRA_FILE_EXTENSION";
    public static final String EXTRA_FILE_TYPE = "EXTRA_FILE_TYPE";
    protected static final String EXTRA_PASSWORD = "EXTRA_PASSWORD";
    protected static final String EXTRA_IS_PREVIEW = "EXTRA_IS_PREVIEW";
    protected static final String EXTRA_NEED_SCAN = "EXTRA_NEED_SCAN";
    protected static final String EXTRA_DATA_CREATE_PDF = "EXTRA_DATA_CREATE_PDF";
    protected static final String EXTRA_FROM_FIRST_OPEN = "EXTRA_FROM_FIRST_OPEN";
    protected static final String EXTRA_FROM_SPLASH = "EXTRA_FROM_SPLASH";

    protected static final int RESULT_FILE_DELETED = -1111;
    public static final int RESULT_NEED_FINISH = -1112;

    protected boolean isNeedToSetTheme = true;

    private static final String TAG = "BaseBindingActivity";
    private V mViewModel;
    private T mViewDataBinding;

    protected String mCurrentPhotoPath;

    private SweetAlertDialog mDownloadFromGgDriveDialog;
    protected SweetAlertDialog mLoadFromLocalDialog;

    private InterstitialAd mHomeInterstitialAd;
    private InterstitialAd mMyPdfInterstitialAd;

    protected boolean mIsRequestFullPermission = false;
    protected int mRequestFullPermissionCode = -1000;
    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();

    @Override
    public void onFragmentAttached() {
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNeedToSetTheme) {
            setForTheme();
        }

        setNoActionBar();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        performDataBinding();
    }

    public void preloadHomeAdsIfInit() {
        mHomeInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.full_home_id);
    }

    public void preloadTapFunctionAdsIfInit() {
    }

    protected void preloadDoneAdsIfInit() {
        if (!checkNeedPurchase() && !isPurchased()) {
            AdsDoneManager.getInstance(this);
        }
    }

    public void preloadMyPdfAdsIfInit() {
        mMyPdfInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.full_my_pdf_id);
    }

    public void preloadViewPdfAdsIfInit() {
    }

    public void showTapFunctionAdsBeforeAction(Runnable callback) {

    }

    public void showOnePerTwoTapFunctionAdsBeforeAction(Runnable callback) {
        callback.run();
    }

    public void showHomeAdsBeforeAction(Runnable callback) {
        Admod.getInstance().forceShowInterstitial(this, mHomeInterstitialAd, new AdCallback() {
            @Override
            public void onAdClosed() {
                callback.run();
            }
        });
    }

    public boolean checkNeedPurchase() {
        FirebaseRemoteUtils firebaseRemoteUtils = new FirebaseRemoteUtils();
        firebaseRemoteUtils.fetchRemoteConfig(this, () -> {});

        ArrayList<String> freeCountry = firebaseRemoteUtils.getFreeCountryList(this);
        String countryCode = CountryCodeHelper.getDeviceCountryCode(this);

        if (countryCode != null && countryCode.length() > 0) {
            DataManager.getInstance(this).setLastKnownCountryCode(countryCode);
            return !freeCountry.contains(countryCode);
        }

        return true;
    }

    public boolean isPurchased() {
        return AppPurchase.getInstance().isPurchased(this);
    }

    public void showIAPDialog(Runnable callback){
        PurchaseDialog purchaseDialog = new PurchaseDialog(BaseBindingActivity.this, new PurchaseDialog.PurchaseListener() {
            @Override
            public void onSelectPurchase(int type) {

                String subId = type == 0 ? BuildConfig.yearly_purchase_key : BuildConfig.monthly_purchase_key;
                FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "User start purchase", subId);

                AppPurchase.getInstance().setPurchaseListioner(new PurchaseListioner() {
                    @Override
                    public void onProductPurchased(String s, String s1) {
                        FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "User purchase success", subId);

                        ToastUtils.showMessageLong(BaseBindingActivity.this, getString(R.string.purchase_success));

                        if (callback != null) {
                            callback.run();
                        }
                    }

                    @Override
                    public void displayErrorMessage(String s) {
                        FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "User purchase fail", subId);
                        ToastUtils.showMessageLong(BaseBindingActivity.this, getString(R.string.purchase_error));
                    }
                });
                AppPurchase.getInstance().subscribe(BaseBindingActivity.this, subId);
            }

            @Override
            public void onCancel() {
                FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "User cancel purchase", "Cancel");
                ToastUtils.showMessageLong(BaseBindingActivity.this, getString(R.string.purchase_cancel));
            }
        });
        try {
            purchaseDialog.show();
            FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "Show purchase box", "Show");
        } catch (Exception e) {
            ToastUtils.showMessageLong(BaseBindingActivity.this, getString(R.string.purchase_error));
        }
    }

    public void checkIAPDoneBeforeAction(Runnable callback) {
        if (isPurchased()) {
            callback.run();
        } else {
            if (!checkNeedPurchase()) {
                Admod.getInstance().forceShowInterstitial(this, AdsDoneManager.getInstance(this).getDoneInterstitialAd(), new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        callback.run();
                    }
                });
            } else {
                showIAPDialog(callback);
            }
        }
    }

    public void showMyPdfAdsBeforeAction(Runnable callback) {
        Admod.getInstance().forceShowInterstitial(this, mMyPdfInterstitialAd, new AdCallback() {
            @Override
            public void onAdClosed() {
                callback.run();
            }
        });
    }

    public void showViewPdfAdsBeforeAction(Runnable callback) {
        callback.run();
    }

    public void showBackHomeAdsBeforeAction(Runnable callback) {
        callback.run();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * create view component
     */
    protected abstract void initView();

    /**
     * set on-click listener for view component
     */
    protected abstract void setClick();

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    private void performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        this.mViewModel = mViewModel == null ? getViewModel() : mViewModel;
        mViewDataBinding.setLifecycleOwner(this);
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void requestReadStoragePermissionsSafely(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            mIsRequestFullPermission = true;
            mRequestFullPermissionCode = requestCode;

            startActivity(intent);
        }
    }

    public void requestFullStoragePermission() {

    }

    public boolean notHaveStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
        } else {
            return (!Environment.isExternalStorageManager());
        }
    }

    @SuppressLint("RestrictedApi")
    public void setNoActionBar() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.hide();
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setActionBar(String title, boolean isShowBackButton) {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.show();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);

            actionBar.setHomeButtonEnabled(isShowBackButton);
            actionBar.setDisplayHomeAsUpEnabled(isShowBackButton);
        }
    }

    public void setForTheme() {
        DataManager dataManager = DataManager.getInstance(this);
        int selectedTheme = dataManager.getTheme();
        if (selectedTheme == ThemeAdapter.ADS_INDEX) selectedTheme = 0;
        // TODO for hide theme setting
        selectedTheme = 0;

        int[] THEME_STYLE_LIST = {R.style.OrangeAppTheme, -1, R.style.BlueAppTheme, R.style.JadeAppTheme, R.style.VioletAppTheme};
        int selectedStyleTheme = THEME_STYLE_LIST[selectedTheme];
        setTheme(selectedStyleTheme);
    }

    public void restartApp(boolean isFromSplash) {
        Intent intent;
        if (isFromSplash) {
            intent = new Intent(BaseBindingActivity.this, SplashActivity.class);
        } else {
            intent = new Intent(BaseBindingActivity.this, MainActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
    }

    public boolean isNetworkConnected() {
        return NetworkUtils.isNetworkConnected(getApplicationContext());
    }

    public int getIntegerByResource(@IntegerRes int integer) {
        return getResources().getInteger(integer);
    }

    /**
     * for set full screen without action bar and navigation bar()
     */
    protected void setActivityFullScreen() {

    }

    protected void setActivityWithActionBar() {

    }

    /**
     * Show popup download from Google drive
     */

    protected void updateFilePathFromGGDrive(Uri uri, String filePath) {
        try {
            if (mDownloadFromGgDriveDialog != null && mDownloadFromGgDriveDialog.isShowing()) {
                mDownloadFromGgDriveDialog.dismiss();
            }
        } catch (Exception e) {
            // donothing
        }
    }

    protected void startDownloadFromGoogleDrive(Uri uri) {
        mDownloadFromGgDriveDialog = DialogFactory.getDialogProgress(this, getString(R.string.downloading_from_gg_drive_text));
        mDownloadFromGgDriveDialog.show();

        AsyncTask.execute(() -> {
            try {
                @SuppressLint("Recycle")
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String originalName = (returnCursor.getString(nameIndex));
                String size = (Long.toString(returnCursor.getLong(sizeIndex)));

                if (originalName == null) {
                    originalName = getString(R.string.prefix_for_google_drive) + DateTimeUtils.currentTimeToNaming();
                }

                File file = new File(DirectoryUtils.getDefaultStorageLocation(), originalName);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read = 0;
                int maxBufferSize = 1024 * 1024;
                int bytesAvailable = inputStream.available();

                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                final byte[] buffers = new byte[bufferSize];
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);
                }
                inputStream.close();
                outputStream.close();

                runOnUiThread(() -> {
                    updateFilePathFromGGDrive(uri, file.getPath());
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    updateFilePathFromGGDrive(uri, null);
                });
            }
        });
    }

    protected void updateFilePathFromGGDriveList(int index, ArrayList<Uri> uriList, String filePath) {
        if (mDownloadFromGgDriveDialog != null) {
            if (index == uriList.size() - 1)
                mDownloadFromGgDriveDialog.dismiss();
        }
    }

    protected void startDownloadFromGoogleDriveList(ArrayList<Uri> uriList) {
        runOnUiThread(() -> {
            mDownloadFromGgDriveDialog = DialogFactory.getDialogProgress(this, getString(R.string.downloading_from_gg_drive_text));
            mDownloadFromGgDriveDialog.show();
        });

        for (int i = 0; i < uriList.size(); i++) {
            int finalIndex = i;
            Uri uri = uriList.get(i);
            if (uri == null) {
                runOnUiThread(() -> {
                    updateFilePathFromGGDriveList(finalIndex, uriList, null);
                });
                return;
            }

            try {
                @SuppressLint("Recycle")
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String originalName = (returnCursor.getString(nameIndex));
                String size = (Long.toString(returnCursor.getLong(sizeIndex)));

                if (originalName == null) {
                    originalName = getString(R.string.prefix_for_google_drive) + DateTimeUtils.currentTimeToNaming();
                }

                File file = new File(DirectoryUtils.getDefaultStorageLocation(), originalName);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read = 0;
                int maxBufferSize = 1024 * 1024;
                int bytesAvailable = inputStream.available();

                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                final byte[] buffers = new byte[bufferSize];
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);
                }
                inputStream.close();
                outputStream.close();

                runOnUiThread(() -> {
                    updateFilePathFromGGDriveList(finalIndex, uriList, file.getPath());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    updateFilePathFromGGDriveList(finalIndex, uriList, null);
                });
            }
        }
    }

    /**
     * for activity move - all activity moving should put here - ads coverage
     */

    public void gotoActivityWithFlag(int flag) {
        Intent intent;

        switch (flag) {

            case AppConstants.FLAG_SEARCH_PDF:
                intent = new Intent(BaseBindingActivity.this, SearchActivity.class);
                startActivity(intent);
                FirebaseUtils.sendEventFunctionUsed(this, "Search PDF", "From home");

                break;
            case AppConstants.FLAG_WORD_TO_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent wordIntent = new Intent(BaseBindingActivity.this, TextToPdfActivity.class);
                    startActivity(wordIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Text to PDF", "From home");
                });

                break;
            case AppConstants.FLAG_PROTECT_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent protectIntent = new Intent(BaseBindingActivity.this, ProtectPdfActivity.class);
                    startActivity(protectIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Protect PDF", "From home");
                });

                break;

            case AppConstants.FLAG_NEW_PDF:

                break;
            case AppConstants.FLAG_UNLOCK_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent unlockIntent = new Intent(BaseBindingActivity.this, UnlockPdfActivity.class);
                    startActivity(unlockIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Unlock PDF", "From home");
                });

                break;
            case AppConstants.FLAG_EXCEL_TO_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent excelIntent = new Intent(BaseBindingActivity.this, ExcelToPdfActivity.class);
                    startActivity(excelIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Excel To PDF", "From home");
                });

                break;
            case AppConstants.FLAG_ADD_WATERMARK:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent addWatermarkIntent = new Intent(BaseBindingActivity.this, AddWaterMarkActivity.class);
                    startActivity(addWatermarkIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Add WaterMark PDF", "From home");
                });

                break;
            case AppConstants.FLAG_MERGE_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent mergePdfIntent = new Intent(BaseBindingActivity.this, MergePdfActivity.class);
                    startActivity(mergePdfIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Merge PDF", "From home");
                });
                break;
            case AppConstants.FLAG_SPLIT_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent splitPdfIntent = new Intent(BaseBindingActivity.this, SplitPdfActivity.class);
                    startActivity(splitPdfIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Split PDF", "From home");
                });

                break;
            case AppConstants.FLAG_PDF_TO_IMAGE:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent pdfToImageIntent = new Intent(BaseBindingActivity.this, PdfToImageActivity.class);
                    startActivity(pdfToImageIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Pdf To image", "From home");
                });

                break;
            case AppConstants.FLAG_PDF_TO_TEXT:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent pdfToTextIntent = new Intent(BaseBindingActivity.this, PdfToTextActivity.class);
                    startActivity(pdfToTextIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Pdf To Text", "From home");
                });

                break;
            case AppConstants.FLAG_EDIT_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent editPdfIntent = new Intent(BaseBindingActivity.this, EditPdfActivity.class);
                    startActivity(editPdfIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Edit Pdf", "From home");
                });
                break;
            case AppConstants.FLAG_VIEW_PDF:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent viewPdfIntent = new Intent(BaseBindingActivity.this, ViewPdfActivity.class);
                    startActivity(viewPdfIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "View Pdf", "From home");
                });

                break;
            case AppConstants.FLAG_IMAGE_TO_PDF_FROM_HOME:
                Intent imagePdfIntent = new Intent(BaseBindingActivity.this, ImageToPdfActivity.class);
                startActivity(imagePdfIntent);

                FirebaseUtils.sendEventFunctionUsed(this, "Image To Pdf", "From home");
                break;
            case AppConstants.FLAG_IMAGE_TO_PDF:
            default:
                showOnePerTwoTapFunctionAdsBeforeAction(() -> {
                    Intent imageToPdfIntent = new Intent(BaseBindingActivity.this, ImageToPdfActivity.class);
                    startActivity(imageToPdfIntent);

                    FirebaseUtils.sendEventFunctionUsed(this, "Image To Pdf", "From home");
                });
        }
    }

    public void gotoProtectPasswordActivity(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        SetPasswordFileDialog setPasswordFileDialog = new SetPasswordFileDialog(this, new SetPasswordFileDialog.SetPasswordFileListener() {
            @Override
            public void onSubmitPassword(String password) {
                checkIAPDoneBeforeAction(() -> {
                    Intent intent = new Intent(BaseBindingActivity.this, ProtectPdfDoneActivity.class);
                    intent.putExtra(EXTRA_FILE_PATH, filePath);
                    intent.putExtra(EXTRA_PASSWORD, password);
                    startActivity(intent);
                });
            }

            @Override
            public void onCancel() {
                // donothing
            }
        });
        setPasswordFileDialog.show();

        FirebaseUtils.sendEventFunctionUsed(this, "Protect Pdf", "From file option");
    }

    public void gotoUnlockPasswordActivity(String filePath, String password) {

        if (password == null) {
            RemovePasswordFileDialog removePasswordFileDialog = new RemovePasswordFileDialog(this, filePath, new RemovePasswordFileDialog.RemovePasswordFileListener() {
                @Override
                public void onSubmitPassword(String password) {
                    checkIAPDoneBeforeAction(() -> {
                        Intent intent = new Intent(BaseBindingActivity.this, UnlockPdfDoneActivity.class);
                        intent.putExtra(EXTRA_FILE_PATH, filePath);
                        intent.putExtra(EXTRA_PASSWORD, password);
                        startActivity(intent);
                    });
                }

                @Override
                public void onCancel() {

                }
            });
            removePasswordFileDialog.show();
        } else {
            checkIAPDoneBeforeAction(() -> {
                Intent intent = new Intent(BaseBindingActivity.this, UnlockPdfDoneActivity.class);
                intent.putExtra(EXTRA_FILE_PATH, filePath);
                intent.putExtra(EXTRA_PASSWORD, password);
                startActivity(intent);
            });
        }

        FirebaseUtils.sendEventFunctionUsed(this, "Unlock Pdf", "From file option");
    }

    public void gotoImageToPdfWithScan() {
        Intent intent = new Intent(BaseBindingActivity.this, ImageToPdfActivity.class);
        intent.putExtra(EXTRA_NEED_SCAN, true);
        startActivity(intent);
    }

    public void gotoViewByAndroidViewActivity(String filePath, FileUtils.FileType fileType) {
        if (filePath != null) {
            FileUtils.openFile(this, filePath, fileType);
        } else {
            ToastUtils.showMessageShort(this, getString(R.string.can_not_open_file));
        }
    }

    public void gotoPdfFileByAndroidViewActivity(String filePath) {
        gotoViewByAndroidViewActivity(filePath, FileUtils.FileType.type_PDF);
    }

    public void gotoPdfFileViewActivity(String filePath) {
        Intent intent = new Intent(BaseBindingActivity.this, ViewPdfActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(this, "View Pdf", "From function");
    }

    public void gotoPdfFilePreviewActivity(String filePath) {
        Intent intent = new Intent(BaseBindingActivity.this, ViewPdfActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        intent.putExtra(EXTRA_IS_PREVIEW, true);
        startActivityForResult(intent, PREVIEW_FILE_REQUEST);

        FirebaseUtils.sendEventFunctionUsed(this, "View Pdf", "From function"); }

    /**
     * Request rating
     */

    @SuppressLint("ResourceAsColor")
    public void showRatingUsPopup(final  Runnable acceptRunnable, final Runnable rejectRunnable) {
        RateUtils.showRateDialog(this, new OnCallback() {
            @Override
            public void onMaybeLater() {
                rejectRunnable.run();
            }

            @Override
            public void onSubmit(String review) {
                FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "User feedback", review);
                ToastUtils.showMessageShort(BaseBindingActivity.this, getString(R.string.thank_you_for_rate_us));
                DataManager dataManager = DataManager.getInstance(getApplicationContext());
                dataManager.setRatingUsDone();
                rejectRunnable.run();
            }

            @Override
            public void onRate() {
                gotoRateUsActivity();
                acceptRunnable.run();
            }
        });
    }

    public void gotoRateUsActivity() {
        DataManager dataManager = DataManager.getInstance(this);
        dataManager.setRatingUsDone();

        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    protected void showConverterPopup(int currentConvert) {
        ConverterSelectDialog converterSelectDialog = new ConverterSelectDialog(this, currentConvert, new ConverterSelectDialog.ConverterSelectSubmit() {
            @Override
            public void updateSelection(int newSelect) {
                if (currentConvert != newSelect) {
                    if (newSelect == 0) {
                        gotoActivityWithFlag(AppConstants.FLAG_IMAGE_TO_PDF);
                        finish();
                    } else if (newSelect == 1) {
                        gotoActivityWithFlag(AppConstants.FLAG_WORD_TO_PDF);
                        finish();
                    } else {
                        gotoActivityWithFlag(AppConstants.FLAG_EXCEL_TO_PDF);
                        finish();
                    }
                }
            }
        });
        converterSelectDialog.setCanceledOnTouchOutside(true);
        converterSelectDialog.setCancelable(true);
        converterSelectDialog.show();
    }
}
