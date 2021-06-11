package com.pdfconverter.jpg2pdf.pdf.converter.ui.formatpdf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.NewPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityCreatePdfDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FormatPdfActivity extends BaseBindingActivity<ActivityCreatePdfDoneBinding, FormatPdfViewModel> implements FormatPdfNavigator {
    private static final int STATE_NOT_STARTED = -1;
    private static final int STATE_CREATING = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_FAIL = 2;

    private NewPDFOptions mOptions;

    private int mCreatingStatus = STATE_NOT_STARTED;
    private String mOutputPath = null;

    private FormatPdfViewModel mFormatPdfViewModel;
    private ActivityCreatePdfDoneBinding mActivityCreatePdfDoneBinding;

    public static final String INTENT_PDF_OPTION = "pdfOption";

    private FormatPdfTask mFormatPdfTask;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_pdf_done;
    }

    @Override
    public FormatPdfViewModel getViewModel() {
        mFormatPdfViewModel = ViewModelProviders.of(this).get(FormatPdfViewModel.class);
        return mFormatPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityCreatePdfDoneBinding = getViewDataBinding();
        mFormatPdfViewModel.setNavigator(this);

        mCreatingStatus = STATE_NOT_STARTED;

        initView();

        String json = getIntent().getStringExtra(INTENT_PDF_OPTION);
        mOptions = new Gson().fromJson(json, NewPDFOptions.class);

        if (mOptions == null || mOptions.getFileName() == null || mOptions.getFileName().length() == 0 || mOptions.getNumberPage() < 1 || mOptions.getNumberPage() > 999) {
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

    @Override
    public void onBackPressed() {
        if (mCreatingStatus == STATE_DONE) {
            setResult(RESULT_NEED_FINISH);
        }
        super.onBackPressed();
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
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.creating_pdf));
        } else {
            mActivityCreatePdfDoneBinding.createLayout.contentView.setVisibility(View.VISIBLE);

            mActivityCreatePdfDoneBinding.createLayout.txtProgress.setVisibility(View.GONE);
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setVisibility(View.VISIBLE);

            mActivityCreatePdfDoneBinding.createLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

            if (mCreatingStatus == STATE_DONE) {
                mActivityCreatePdfDoneBinding.createLayout.createStatusResult.setVisibility(View.VISIBLE);

                if (mOutputPath != null) {
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText(FileUtils.getFileName(mOutputPath));
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessLocation.setText("Location: " + FileUtils.getFileDirectoryPath(mOutputPath));
                } else {
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText("No name");
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessLocation.setText("Location: " + "NA");
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

                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.new_pdf_success));
                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.main_green_stroke_color));
            } else {
                mActivityCreatePdfDoneBinding.createLayout.createStatusResult.setVisibility(View.GONE);

                mActivityCreatePdfDoneBinding.createLayout.btnOpen.setVisibility(View.INVISIBLE);
                mActivityCreatePdfDoneBinding.createLayout.btnShare.setVisibility(View.INVISIBLE);

                mActivityCreatePdfDoneBinding.createLayout.createError.setVisibility(View.VISIBLE);

                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.creating_fail_pdf));
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
                    SnackBarUtils.getSnackbar(FormatPdfActivity.this, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(FormatPdfActivity.this, getString(R.string.rename_file_success)).show();
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText(newName);

                    String tempOldDir = mOutputPath;
                    mOutputPath = FileUtils.getLastReplacePath(mOutputPath, fullName, newName);

                    mFormatPdfViewModel.updateSavedData(tempOldDir, mOutputPath);
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
        mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText("0 %");

        mFormatPdfTask = new FormatPdfTask(this, mOptions, new FormatPdfTask.OnPDFCreatedInterface() {
            @SuppressLint("SetTextI18n")
            @Override
            public void updateStatus(int percent) {
                runOnUiThread(() -> {
                    mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText(percent + " %");
                });
            }

            @Override
            public void createPdfFalse() {
                runOnUiThread(() -> {
                    mCreatingStatus = STATE_FAIL;
                    setForLayoutView();
                });

            }

            @Override
            public void createPdfSuccess(String outputPath) {
                runOnUiThread(() -> {
                    mCreatingStatus = STATE_DONE;
                    mOutputPath = outputPath;
                    setForLayoutView();
                });

            }
        });

        mFormatPdfTask.execute();
    }
}
