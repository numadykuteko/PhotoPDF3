package com.pdfconverter.jpg2pdf.pdf.converter.ui.firstopen;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityFirstOpenBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.main.MainActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;

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
                    gotoImageToPdfActivity();
                    sweetAlertDialog1.dismiss();
                });
                sweetAlertDialog.showCancelButton(false);
                sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
            });
            mRequestPermissionDialog.show();
        } else {
            gotoImageToPdfActivity();
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
                        gotoImageToPdfActivity();
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.reject_read_file));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        gotoImageToPdfActivity();
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
                    gotoImageToPdfActivity();
                });
            } else {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                mRequestPermissionDialog.setContentText(getString(R.string.reject_read_file));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    gotoImageToPdfActivity();
                });
            }
        }
    }

    private void gotoImageToPdfActivity() {
        Intent imageToPdfIntent = new Intent(FirstOpenActivity.this, MainActivity.class);
        imageToPdfIntent.putExtra(EXTRA_FROM_FIRST_OPEN, true);
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
