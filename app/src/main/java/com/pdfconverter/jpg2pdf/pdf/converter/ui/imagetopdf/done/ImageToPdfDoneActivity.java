package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.done;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityImageToPdfDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.File;

public class ImageToPdfDoneActivity extends BaseBindingActivity {

    private static final String TAG = "CreatePdfActivityTAG";
    private ImageToPdfDoneViewModel mImageToPdfDoneViewModel;
    private ActivityImageToPdfDoneBinding mActivityImageToPdfDoneBinding;
    public static final String INTENT_PDF_OPTION = "pdfOption";
    private ImageToPDFOptions mImageToPDFOptions;
    private String mOutputPath;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_image_to_pdf_done;
    }

    @Override
    public BaseViewModel getViewModel() {
        mImageToPdfDoneViewModel = ViewModelProviders.of(this).get(ImageToPdfDoneViewModel.class);
        return mImageToPdfDoneViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityImageToPdfDoneBinding = (ActivityImageToPdfDoneBinding) getViewDataBinding();
        String json = getIntent().getStringExtra(INTENT_PDF_OPTION);
        mImageToPDFOptions = new Gson().fromJson(json, ImageToPDFOptions.class);
        mImageToPdfDoneViewModel.setImageToPDFOptions(mImageToPDFOptions);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PREVIEW_FILE_REQUEST && resultCode == RESULT_FILE_DELETED) {
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void initView() {
        mActivityImageToPdfDoneBinding.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mActivityImageToPdfDoneBinding.toolbar.toolbarNameTv.setText(getString(R.string.done_create_pdf_title));

        Admod.getInstance().loadSmallNativeFragment(this, BuildConfig.native_id, mActivityImageToPdfDoneBinding.nativeAds);

        preloadViewPdfAdsIfInit();

        mImageToPdfDoneViewModel.getStatusCreatePDF().setValue(0);
        mImageToPdfDoneViewModel.createPdf();
        
        setForLiveData();
    }

    @Override
    public void onBackPressed() {
        if (mImageToPdfDoneViewModel.getStatusCreatePDF().getValue() == ImageToPdfDoneViewModel.CREATE_PDF_SUCCESS) {
            setResult(RESULT_NEED_FINISH);
        }
        super.onBackPressed();
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }
    
    private void setForLiveData() {
        mImageToPdfDoneViewModel.getStatusCreatePDF().observe(this, this::updateStatusCreatePdf);
        mImageToPdfDoneViewModel.getStatusPercent().observe(this, this::updatePercent);
    }

    @SuppressLint("SetTextI18n")
    private void updateStatusCreatePdf(int creatingStatus) {
        if (creatingStatus == ImageToPdfDoneViewModel.CREATING_PDF_FILE) {
            mActivityImageToPdfDoneBinding.contentView.setVisibility(View.VISIBLE);

            mActivityImageToPdfDoneBinding.btnOpen.setVisibility(View.INVISIBLE);
            mActivityImageToPdfDoneBinding.btnShare.setVisibility(View.INVISIBLE);

            mActivityImageToPdfDoneBinding.createSuccess.setVisibility(View.GONE);
            mActivityImageToPdfDoneBinding.createError.setVisibility(View.GONE);
            mActivityImageToPdfDoneBinding.createStatusResult.setVisibility(View.GONE);

            mActivityImageToPdfDoneBinding.txtProgress.setVisibility(View.VISIBLE);
            mActivityImageToPdfDoneBinding.createStatusText.setVisibility(View.VISIBLE);
            mActivityImageToPdfDoneBinding.createStatusText.setText(getString(R.string.add_watermark_creating_pdf));
        } else {
            mActivityImageToPdfDoneBinding.contentView.setVisibility(View.VISIBLE);

            mActivityImageToPdfDoneBinding.txtProgress.setVisibility(View.GONE);
            mActivityImageToPdfDoneBinding.createStatusText.setVisibility(View.VISIBLE);

            mActivityImageToPdfDoneBinding.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

            if (creatingStatus == ImageToPdfDoneViewModel.CREATE_PDF_SUCCESS) {
                mOutputPath = mImageToPdfDoneViewModel.getOutputFile().getAbsolutePath();

                mActivityImageToPdfDoneBinding.createStatusResult.setVisibility(View.VISIBLE);

                if (mImageToPdfDoneViewModel.getOutputFile() != null) {
                    mActivityImageToPdfDoneBinding.convertSuccessEditName.setText(FileUtils.getFileName(mOutputPath));
                    mActivityImageToPdfDoneBinding.convertSuccessLocation.setText("Location: " + FileUtils.getFileDirectoryPath(mOutputPath));
                } else {
                    mActivityImageToPdfDoneBinding.convertSuccessEditName.setText("No name");
                    mActivityImageToPdfDoneBinding.convertSuccessLocation.setText("Location: NA");
                }

                mActivityImageToPdfDoneBinding.btnOpen.setVisibility(View.VISIBLE);
                mActivityImageToPdfDoneBinding.btnOpen.setOnClickListener(view -> {
                    if (mOutputPath != null) {
                        showViewPdfAdsBeforeAction(() -> {
                            gotoPdfFilePreviewActivity(mOutputPath);
                            onBackPressed();
                        });
                    }
                });

                mActivityImageToPdfDoneBinding.btnShare.setVisibility(View.VISIBLE);
                mActivityImageToPdfDoneBinding.btnShare.setOnClickListener(view -> {
                    if (mOutputPath != null) {
                        FileUtils.shareFile(this, new File(mOutputPath));
                    }
                });

                mActivityImageToPdfDoneBinding.convertSuccessEditName.setOnClickListener(view -> {
                    if (mOutputPath != null) {
                        showRenameDialog();
                    }
                });

                mActivityImageToPdfDoneBinding.createSuccess.setVisibility(View.VISIBLE);
                mActivityImageToPdfDoneBinding.createError.setVisibility(View.GONE);

                mActivityImageToPdfDoneBinding.createStatusText.setText(getString(R.string.add_watermark_converting_success_pdf));
                mActivityImageToPdfDoneBinding.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.main_green_stroke_color));

            } else {
                mActivityImageToPdfDoneBinding.createStatusResult.setVisibility(View.GONE);

                mActivityImageToPdfDoneBinding.btnOpen.setVisibility(View.INVISIBLE);
                mActivityImageToPdfDoneBinding.btnShare.setVisibility(View.INVISIBLE);

                mActivityImageToPdfDoneBinding.createError.setVisibility(View.VISIBLE);

                mActivityImageToPdfDoneBinding.createStatusText.setText(getString(R.string.add_watermark_creating_fail_pdf));
                mActivityImageToPdfDoneBinding.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.redTotally));
            }
        }
    }

    private void showRenameDialog() {
        String fullName = FileUtils.getFileName(mOutputPath);
        String displayName = fullName;
        try {
            displayName = fullName.substring(0, fullName.lastIndexOf("."));
        } catch (Exception ignored) {
        }

        RenameFileDialog renameFileDialog = new RenameFileDialog(this, displayName, new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String name) {
                String newName = name + ".pdf";
                FileData fileData = new FileData(fullName, mOutputPath, null, 0, 0, DataConstants.FILE_TYPE_PDF);
                int result = FileUtils.renameFile(fileData, newName);

                if (result == -2 || result == 0) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.can_not_edit_video_name));
                } else if (result == -1) {
                    SnackBarUtils.getSnackbar(ImageToPdfDoneActivity.this, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(ImageToPdfDoneActivity.this, getString(R.string.rename_file_success)).show();
                    mActivityImageToPdfDoneBinding.convertSuccessEditName.setText(newName);

                    String tempOldDir = mOutputPath;
                    mOutputPath = FileUtils.getLastReplacePath(mOutputPath, fullName, newName);

                    mImageToPdfDoneViewModel.updateSavedData(tempOldDir, mOutputPath);
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }
    
    @SuppressLint("SetTextI18n")
    private void updatePercent(int percent) {
        mActivityImageToPdfDoneBinding.txtProgress.setText(percent + " %");
    }
}
