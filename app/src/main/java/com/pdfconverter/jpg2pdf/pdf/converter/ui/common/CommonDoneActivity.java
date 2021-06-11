package com.pdfconverter.jpg2pdf.pdf.converter.ui.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityCreatePdfDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.File;
import static com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants.AUTHORITY_APP;

public class CommonDoneActivity extends BaseBindingActivity<ActivityCreatePdfDoneBinding, CommonViewModel> implements CommonNavigator {
    private String mOutputPath = null;
    private String mFileExtension = ".pdf";
    private String mFileType = DataConstants.FILE_TYPE_PDF;

    private CommonViewModel mCommonViewModel;
    private ActivityCreatePdfDoneBinding mActivityCreatePdfDoneBinding;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_pdf_done;
    }

    @Override
    public CommonViewModel getViewModel() {
        mCommonViewModel = ViewModelProviders.of(this).get(CommonViewModel.class);
        return mCommonViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityCreatePdfDoneBinding = getViewDataBinding();
        mCommonViewModel.setNavigator(this);

        mOutputPath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        mFileExtension = getIntent().getStringExtra(EXTRA_FILE_EXTENSION);
        mFileType = getIntent().getStringExtra(EXTRA_FILE_TYPE);

        if (mOutputPath == null || mOutputPath.length() == 0 || !FileUtils.checkFileExist(mOutputPath)) {
            finish();
            return;
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivityCreatePdfDoneBinding.createLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mActivityCreatePdfDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.done_create_pdf_title));

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        preloadViewPdfAdsIfInit();

        setForLayoutView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PREVIEW_FILE_REQUEST) {
            if (resultCode == RESULT_FILE_DELETED) {
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_NEED_FINISH);
        super.onBackPressed();
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        mActivityCreatePdfDoneBinding.createLayout.contentView.setVisibility(View.VISIBLE);

        mActivityCreatePdfDoneBinding.createLayout.txtProgress.setVisibility(View.GONE);
        mActivityCreatePdfDoneBinding.createLayout.createStatusText.setVisibility(View.VISIBLE);

        mActivityCreatePdfDoneBinding.createLayout.createStatusResult.setVisibility(View.VISIBLE);

        if (mOutputPath != null) {
            mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText(FileUtils.getFileName(mOutputPath));
            mActivityCreatePdfDoneBinding.createLayout.convertSuccessLocation.setText("Location: " + FileUtils.getFileDirectoryPath(mOutputPath));
        } else {
            mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText("No name");
            mActivityCreatePdfDoneBinding.createLayout.convertSuccessLocation.setText("Location: NA");
        }

        mActivityCreatePdfDoneBinding.createLayout.btnOpen.setVisibility(View.VISIBLE);
        mActivityCreatePdfDoneBinding.createLayout.btnOpen.setOnClickListener(view -> {
            if (mOutputPath != null) {
                if (mFileType.equals(DataConstants.FILE_TYPE_PDF)) {
                    showViewPdfAdsBeforeAction(() -> {
                        gotoPdfFilePreviewActivity(mOutputPath);
                        onBackPressed();
                    });
                } else {
                    gotoViewByAndroidViewActivity(mOutputPath, FileUtils.FileType.type_TXT);
                }

            }
        });

        mActivityCreatePdfDoneBinding.createLayout.btnShare.setVisibility(View.VISIBLE);
        mActivityCreatePdfDoneBinding.createLayout.btnShare.setOnClickListener(view -> {
            if (mOutputPath != null) {
                if (mFileType.equals(DataConstants.FILE_TYPE_PDF)) {
                    FileUtils.shareFile(this, new File(mOutputPath));
                } else {
                     try {
                         Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                         sharingIntent.setType("text/*");
                         sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(CommonDoneActivity.this, AUTHORITY_APP, new File(mOutputPath)));
                         sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         startActivity(Intent.createChooser(sharingIntent, "Share file with"));
                     } catch (Exception e) {
                         ToastUtils.showMessageShort(getApplicationContext(), "Can not share file now.");
                     }
                }
            }
        });

        mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setOnClickListener(view -> {
            if (mOutputPath != null) {
                showRenameDialog();
            }
        });

        mActivityCreatePdfDoneBinding.createLayout.createSuccess.setVisibility(View.VISIBLE);
        mActivityCreatePdfDoneBinding.createLayout.createError.setVisibility(View.GONE);

        if (mFileType.equals(DataConstants.FILE_TYPE_PDF)) {
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_edit_success_pdf));
        } else {
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.create_txt_success));
        }
        mActivityCreatePdfDoneBinding.createLayout.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.main_green_stroke_color));
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
                String newName = name + mFileExtension;
                FileData fileData = new FileData(fullName, mOutputPath, null, 0, 0, mFileType);
                int result = FileUtils.renameFile(fileData, newName);

                if (result == -2 || result == 0) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.can_not_edit_video_name));
                } else if (result == -1) {
                    SnackBarUtils.getSnackbar(CommonDoneActivity.this, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(CommonDoneActivity.this, getString(R.string.rename_file_success)).show();
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText(newName);

                    String mTempDir = mOutputPath;
                    mOutputPath = FileUtils.getLastReplacePath(mOutputPath, fullName, newName);

                    if (mFileType.equals(DataConstants.FILE_TYPE_PDF)) {
                        mCommonViewModel.updateSavedData(mTempDir, mOutputPath);
                    }
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }
}
