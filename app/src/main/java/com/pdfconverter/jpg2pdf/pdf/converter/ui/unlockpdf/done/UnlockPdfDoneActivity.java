package com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf.done;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityCreatePdfDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf.UnlockPdfNavigator;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf.UnlockPdfViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UnlockPdfDoneActivity extends BaseBindingActivity<ActivityCreatePdfDoneBinding, UnlockPdfViewModel> implements UnlockPdfNavigator {
    private static final int STATE_NOT_STARTED = -1;
    private static final int STATE_CREATING = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_FAIL = 2;

    private String mFilePath;
    private String mPassword;

    private int mCreatingStatus = STATE_NOT_STARTED;
    private String mOutputPath = null;

    private UnlockPdfViewModel mUnlockPdfViewModel;
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
    public UnlockPdfViewModel getViewModel() {
        mUnlockPdfViewModel = ViewModelProviders.of(this).get(UnlockPdfViewModel.class);
        return mUnlockPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityCreatePdfDoneBinding = getViewDataBinding();
        mUnlockPdfViewModel.setNavigator(this);

        mCreatingStatus = STATE_NOT_STARTED;

        initView();

        mFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        mPassword = getIntent().getStringExtra(EXTRA_PASSWORD);

        if (mFilePath == null || mPassword == null) {
            SweetAlertDialog errorDialog = DialogFactory.getDialogError(this, getString(R.string.data_not_valid));
            errorDialog.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finish();
            });
            errorDialog.show();
        } else {
            startCreatePdf();
        }
    }

    @Override
    protected void initView() {
        mActivityCreatePdfDoneBinding.createLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mActivityCreatePdfDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.done_create_pdf_title));

        Admod.getInstance().loadSmallNativeFragment(this, BuildConfig.native_id, mActivityCreatePdfDoneBinding.createLayout.nativeAds);

        preloadViewPdfAdsIfInit();

        setForLayoutView();
    }

    @Override
    public void onBackPressed() {
        if (mCreatingStatus == STATE_DONE) {
            setResult(RESULT_NEED_FINISH);
        }
        finish();

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mCreatingStatus == STATE_NOT_STARTED) {
            mActivityCreatePdfDoneBinding.createLayout.contentView.setVisibility(View.GONE);
        } else if (mCreatingStatus == STATE_CREATING) {
            mActivityCreatePdfDoneBinding.createLayout.contentView.setVisibility(View.VISIBLE);

            mActivityCreatePdfDoneBinding.createLayout.btnOpen.setVisibility(View.INVISIBLE);
            mActivityCreatePdfDoneBinding.createLayout.btnShare.setVisibility(View.INVISIBLE);

            mActivityCreatePdfDoneBinding.createLayout.createSuccess.setVisibility(View.GONE);
            mActivityCreatePdfDoneBinding.createLayout.createError.setVisibility(View.GONE);
            mActivityCreatePdfDoneBinding.createLayout.createStatusResult.setVisibility(View.GONE);

            mActivityCreatePdfDoneBinding.createLayout.txtProgress.setVisibility(View.VISIBLE);
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setVisibility(View.VISIBLE);
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_creating_pdf));
        } else {
            mActivityCreatePdfDoneBinding.createLayout.contentView.setVisibility(View.VISIBLE);

            mActivityCreatePdfDoneBinding.createLayout.txtProgress.setVisibility(View.GONE);
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setVisibility(View.VISIBLE);

            if (mCreatingStatus == STATE_DONE) {
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
                        showViewPdfAdsBeforeAction(() -> {
                            gotoPdfFilePreviewActivity(mOutputPath);
                            onBackPressed();
                        });
                    }
                });

                mActivityCreatePdfDoneBinding.createLayout.btnShare.setVisibility(View.VISIBLE);
                mActivityCreatePdfDoneBinding.createLayout.btnShare.setOnClickListener(view -> {
                    if (mOutputPath != null) {
                        FileUtils.shareFile(this, new File(mOutputPath));
                    }
                });

                mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setOnClickListener(view -> {
                    if (mOutputPath != null) {
                        showRenameDialog();
                    }
                });

                mActivityCreatePdfDoneBinding.createLayout.createSuccess.setVisibility(View.VISIBLE);
                mActivityCreatePdfDoneBinding.createLayout.createError.setVisibility(View.GONE);

                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_remove_password_success_pdf));
                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.main_green_stroke_color));
            } else {
                mActivityCreatePdfDoneBinding.createLayout.createStatusResult.setVisibility(View.GONE);

                mActivityCreatePdfDoneBinding.createLayout.btnOpen.setVisibility(View.INVISIBLE);
                mActivityCreatePdfDoneBinding.createLayout.btnShare.setVisibility(View.INVISIBLE);

                mActivityCreatePdfDoneBinding.createLayout.createError.setVisibility(View.VISIBLE);

                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_remove_password_fail_pdf));
                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.redTotally));
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
                    SnackBarUtils.getSnackbar(UnlockPdfDoneActivity.this, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(UnlockPdfDoneActivity.this, getString(R.string.rename_file_success)).show();

                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText(newName);

                    String tempOldDir = mOutputPath;
                    mOutputPath = FileUtils.getLastReplacePath(mOutputPath, fullName, newName);

                    mUnlockPdfViewModel.updateSavedData(tempOldDir, mOutputPath);
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }

    private void startCreatePdf() {
        mCreatingStatus = STATE_CREATING;
        setForLayoutView();
        mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText("1 %");

        AsyncTask.execute(() -> {

            try {
                mOutputPath = PdfUtils.removePassword(this, mFilePath, mPassword);

                runOnUiThread(() -> {
                    if (mOutputPath != null) {
                        mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText("100 %");

                        mUnlockPdfViewModel.saveRecent(mOutputPath, getString(R.string.unlock_pdf));

                        mCreatingStatus = STATE_DONE;
                    } else {
                        mCreatingStatus = STATE_FAIL;
                        ToastUtils.showMessageShort(this, getString(R.string.unlock_pdf_add_password_error));
                    }

                    setForLayoutView();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    mCreatingStatus = STATE_FAIL;
                    setForLayoutView();

                    ToastUtils.showMessageShort(this, getString(R.string.unlock_pdf_add_password_error));
                    FileUtils.deleteFileOnExist(mOutputPath);
                });
            }
        });
    }
}
