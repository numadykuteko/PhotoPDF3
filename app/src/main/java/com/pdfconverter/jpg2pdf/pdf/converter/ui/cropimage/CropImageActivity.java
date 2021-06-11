package com.pdfconverter.jpg2pdf.pdf.converter.ui.cropimage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityCropImageBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.ImageAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

public class CropImageActivity extends BaseBindingActivity {

    private static final String TAG = "CropImageActivityTag";
    private CropImageViewModel mCropImageViewModel;
    private ActivityCropImageBinding mCropImageBinding;
    private Uri mUriFile;
    private int position = 0;
    private String mImagePathCrop = "";
    private boolean mIsFinished = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_crop_image;
    }

    @Override
    public BaseViewModel getViewModel() {
        mCropImageViewModel = ViewModelProviders.of(this).get(CropImageViewModel.class);
        return mCropImageViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(ImageAdapter.INTENT_DATA_IMAGE);
        if (path == null) return;
        try {
            mUriFile = FileProvider.getUriForFile(this, ImageToPdfConstants.AUTHORITY_APP, new File(path));
            position = getIntent().getIntExtra(ImageAdapter.ADAPTER_POSITION, 0);
        } catch (Exception e) {
            ToastUtils.showSystemIssueToast(this);
            finish();
            return;
        }

        initView();
    }

    @Override
    protected void initView() {
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mCropImageBinding = (ActivityCropImageBinding) getViewDataBinding();

        mCropImageBinding.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mCropImageBinding.toolbarTitle.setText(getString(R.string.crop_image_title));

        setUpCropImageView();
        mCropImageBinding.cropImageView.setImageUriAsync(mUriFile);
        mCropImageBinding.cropBtnRotate.setOnClickListener(v -> mCropImageBinding.cropImageView.rotateImage(90));
        mCropImageBinding.cropBtnCrop.setOnClickListener(v -> cropImage());
        mCropImageBinding.cropBtnFlip.setOnClickListener(view -> mCropImageBinding.cropImageView.flipImageHorizontally());
        mCropImageBinding.cropBtnDone.setOnClickListener(view -> {
            mIsFinished = true;
            cropImage();
        });
        mCropImageBinding.toolbarActionText.setOnClickListener(view -> mCropImageBinding.cropImageView.setImageUriAsync(mUriFile));
    }

    private void cropImage() {
        try {
            File imageFile = getBaseContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File newFile = File.createTempFile(String.valueOf(System.nanoTime()), ".jpg", imageFile);
            mImagePathCrop = newFile.getPath();
            mCropImageBinding.cropImageView.saveCroppedImageAsync(Uri.fromFile(newFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpCropImageView() {
        mCropImageBinding.cropImageView.setOnCropImageCompleteListener(
                (CropImageView view, CropImageView.CropResult result) -> {
                    mCropImageBinding.cropImageView.setImageUriAsync(result.getUri());
                    if (mIsFinished) {
                        Intent finishIntent = new Intent();
                        finishIntent.putExtra(ImageAdapter.INTENT_DATA_IMAGE, mImagePathCrop);
                        finishIntent.putExtra(ImageAdapter.ADAPTER_POSITION, position);
                        setResult(Activity.RESULT_OK, finishIntent);
                        finish();
                    }
                });
    }


    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
