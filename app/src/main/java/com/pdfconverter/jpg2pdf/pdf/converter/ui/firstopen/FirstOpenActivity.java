package com.pdfconverter.jpg2pdf.pdf.converter.ui.firstopen;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ads.control.AppPurchase;
import com.ads.control.funtion.PurchaseListioner;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityFirstOpenBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PurchaseDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.main.MainActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.AnimationUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FirstOpenActivity extends BaseBindingActivity<ActivityFirstOpenBinding, FirstOpenViewModel> implements FirstOpenNavigator {
    private ActivityFirstOpenBinding mActivityFirstOpenBinding;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_MAIN = 1;
    private SweetAlertDialog mRequestPermissionDialog;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_first_open;
    }

    @Override
    public FirstOpenViewModel getViewModel() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityFirstOpenBinding = getViewDataBinding();

        initView();
    }

    @Override
    protected void initView() {
        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, -0.03f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.03f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());

        mActivityFirstOpenBinding.continueButton.startAnimation(mAnimation);

        View shine = findViewById(R.id.shine);
        Animation shineAnimation = AnimationUtils.getAnimation(this, R.anim.left_right);
        shine.startAnimation(shineAnimation);

        showFirstView(0);
    }

    @Override
    public void onBackPressed() {
        Intent imageToPdfIntent = new Intent(FirstOpenActivity.this, MainActivity.class);
        startActivity(imageToPdfIntent);
        finish();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showFirstView(int index) {
        mActivityFirstOpenBinding.firstView.setVisibility(View.VISIBLE);
        mActivityFirstOpenBinding.secondView.setVisibility(View.GONE);

        if (index == 0) {
            mActivityFirstOpenBinding.firstViewImage.setImageDrawable(getDrawable(R.drawable.intro_1));
            mActivityFirstOpenBinding.firstViewTxt.setText(getText(R.string.purchase_introduce_1));
            mActivityFirstOpenBinding.firstViewTxt2.setVisibility(View.VISIBLE);
        } else if (index == 1) {
            mActivityFirstOpenBinding.firstViewImage.setImageDrawable(getDrawable(R.drawable.intro_2));
            mActivityFirstOpenBinding.firstViewTxt.setText(getText(R.string.purchase_introduce_2));
            mActivityFirstOpenBinding.firstViewTxt2.setVisibility(View.GONE);
        } else if (index == 2) {
            mActivityFirstOpenBinding.firstViewImage.setImageDrawable(getDrawable(R.drawable.intro_3));
            mActivityFirstOpenBinding.firstViewTxt.setText(getText(R.string.purchase_introduce_3));
            mActivityFirstOpenBinding.firstViewTxt2.setVisibility(View.GONE);
        }

        mActivityFirstOpenBinding.firstViewContinue.setOnClickListener(v -> {
            if (index < 2) {
                showFirstView(index + 1);
            } else if (checkNeedPurchase()) {
                showIAPSuggest();
            } else {
                showSecondView();
            }
        });
    }

    private void showIAPSuggest() {
        if (!AppPurchase.getInstance().isPurchased(this)) {
            PurchaseDialog purchaseDialog = new PurchaseDialog(FirstOpenActivity.this, new PurchaseDialog.PurchaseListener() {
                @Override
                public void onSelectPurchase(int type) {
                    String subId = type == 0 ? BuildConfig.yearly_purchase_key : BuildConfig.monthly_purchase_key;
                    AppPurchase.getInstance().setPurchaseListioner(new PurchaseListioner() {
                        @Override
                        public void onProductPurchased(String s, String s1) {
                            ToastUtils.showMessageLong(FirstOpenActivity.this, getString(R.string.purchase_success));
                        }

                        @Override
                        public void displayErrorMessage(String s) {

                        }
                    });
                    AppPurchase.getInstance().subscribe(FirstOpenActivity.this, subId);
                }

                @Override
                public void onCancel() {

                }
            });
            try {
                purchaseDialog.show();
            } catch (Exception ignored) {
                showSecondView();
            }

            purchaseDialog.setOnDismissListener(dialog -> showSecondView());
        } else {
            showSecondView();
        }
    }

    private void showSecondView() {
        mActivityFirstOpenBinding.firstView.setVisibility(View.GONE);
        mActivityFirstOpenBinding.secondView.setVisibility(View.VISIBLE);

        mActivityFirstOpenBinding.buttonFirstOpenConverterSelectPhotos.setOnClickListener(view -> {
            checkPermissionOnFirstOpen();
        });

        mActivityFirstOpenBinding.buttonFirstOpenConverterSkip.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(FirstOpenActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkPermissionOnFirstOpen() {
        if (notHaveStoragePermission()) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_MAIN);
            });
            mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                sweetAlertDialog.setContentText(getString(R.string.reject_read_file));
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                    gotoImageToPdfActivity(false);
                    sweetAlertDialog1.dismiss();
                });
                sweetAlertDialog.showCancelButton(false);
                sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
            });
            mRequestPermissionDialog.show();
        } else {
            gotoImageToPdfActivity(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_MAIN:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.thank_you_for_support));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        gotoImageToPdfActivity(true);
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.reject_read_file));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        gotoImageToPdfActivity(false);
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsRequestFullPermission) {
            mIsRequestFullPermission = false;

            if (!notHaveStoragePermission()) {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                mRequestPermissionDialog.setContentText(getString(R.string.thank_you_for_support));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    gotoImageToPdfActivity(true);
                });
            } else {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                mRequestPermissionDialog.setContentText(getString(R.string.reject_read_file));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    gotoImageToPdfActivity(false);
                });
            }
        }
    }

    private void gotoImageToPdfActivity(boolean isOpenCamera) {
        Intent imageToPdfIntent = new Intent(FirstOpenActivity.this, MainActivity.class);
        imageToPdfIntent.putExtra(EXTRA_FROM_FIRST_OPEN, true);
        imageToPdfIntent.putExtra(EXTRA_NEED_SCAN, isOpenCamera);
        startActivity(imageToPdfIntent);
        finish();
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }
}
