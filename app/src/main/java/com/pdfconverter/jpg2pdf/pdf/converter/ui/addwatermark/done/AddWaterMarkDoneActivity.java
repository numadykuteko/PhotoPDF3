package com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.done;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.Watermark;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityCreatePdfDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.AddWaterMarkNavigator;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.AddWaterMarkViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.io.FileOutputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddWaterMarkDoneActivity extends BaseBindingActivity<ActivityCreatePdfDoneBinding, AddWaterMarkViewModel> implements AddWaterMarkNavigator {
    private static final int STATE_NOT_STARTED = -1;
    private static final int STATE_CREATING = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_FAIL = 2;
    
    private int mCreatingStatus = STATE_NOT_STARTED;
    private String mOutputPath = null;

    private AddWaterMarkViewModel mAddWaterMarkViewModel;
    private ActivityCreatePdfDoneBinding mActivityCreatePdfDoneBinding;

    private Watermark mWatermark;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_pdf_done;
    }

    @Override
    public AddWaterMarkViewModel getViewModel() {
        mAddWaterMarkViewModel = ViewModelProviders.of(this).get(AddWaterMarkViewModel.class);
        return mAddWaterMarkViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityCreatePdfDoneBinding = getViewDataBinding();
        mAddWaterMarkViewModel.setNavigator(this);

        mCreatingStatus = STATE_NOT_STARTED;

        initView();

        String json = getIntent().getStringExtra(EXTRA_DATA_CREATE_PDF);
        mWatermark = new Gson().fromJson(json, Watermark.class);
        if (mWatermark == null || mWatermark.getFilePath() == null || mWatermark.getDocumentData() == null) {
            SweetAlertDialog errorDialog = DialogFactory.getDialogError(this, getString(R.string.data_not_valid));
            errorDialog.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finish();
            });
            errorDialog.show();
        } else {
            startAddWaterMark();
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
            mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_creating_pdf));
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

                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_add_wm_success_pdf));
                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setTextColor(ColorUtils.getColorFromResource(this, R.color.main_green_stroke_color));

            } else {
                mActivityCreatePdfDoneBinding.createLayout.createStatusResult.setVisibility(View.GONE);

                mActivityCreatePdfDoneBinding.createLayout.btnOpen.setVisibility(View.INVISIBLE);
                mActivityCreatePdfDoneBinding.createLayout.btnShare.setVisibility(View.INVISIBLE);

                mActivityCreatePdfDoneBinding.createLayout.createError.setVisibility(View.VISIBLE);

                mActivityCreatePdfDoneBinding.createLayout.createStatusText.setText(getString(R.string.add_watermark_creating_fail_pdf));
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
                    SnackBarUtils.getSnackbar(AddWaterMarkDoneActivity.this, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(AddWaterMarkDoneActivity.this, getString(R.string.rename_file_success)).show();
                    mActivityCreatePdfDoneBinding.createLayout.convertSuccessEditName.setText(newName);

                    String tempOldDir = mOutputPath;
                    mOutputPath = FileUtils.getLastReplacePath(mOutputPath, fullName, newName);

                    mAddWaterMarkViewModel.updateSavedData(tempOldDir, mOutputPath);
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }

    private void startAddWaterMark() {
        mCreatingStatus = STATE_CREATING;
        setForLayoutView();

        mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText(0 + "%");

        AsyncTask.execute(() -> {
            try {
                mOutputPath = FileUtils.getUniquePdfFileName(this, FileUtils.getLastReplacePath(mWatermark.getFilePath(),
                        DataConstants.PDF_EXTENSION, getString(R.string.add_watermark_output_file_name)));

                PdfReader reader = new PdfReader(mWatermark.getFilePath());
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(mOutputPath));

                BaseFont baseFont = BaseFont.createFont(mWatermark.getFontFamily(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font font = new Font(baseFont, mWatermark.getTextSize(), mWatermark.getFontStyle(), mWatermark.getTextColor());
                Phrase p = new Phrase(mWatermark.getWatermarkText(), font);

                PdfContentByte over;
                Rectangle pageSize;
                float x, y;
                int n = reader.getNumberOfPages();
                if (n == 0) {
                    runOnUiThread(() -> {
                        mCreatingStatus = STATE_FAIL;
                        setForLayoutView();
                        ToastUtils.showMessageShort(this, getString(R.string.add_watermark_error));

                        FileUtils.deleteFileOnExist(mOutputPath);
                    });

                    return;
                }

                for (int i = 1; i <= n; i++) {
                    pageSize = reader.getPageSizeWithRotation(i);
                    x = PdfUtils.getXForWaterMark(mWatermark.getPosition(), pageSize);
                    y = PdfUtils.getYForWaterMark(mWatermark.getPosition(), pageSize);

                    over = stamper.getOverContent(i);

                    ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, x, y, mWatermark.getRotationAngle());

                    int finalI = i;
                    runOnUiThread(() -> {
                        if (n == 0) return;

                        int progress = Math.round(finalI / (float) n * 100);
                        mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText(progress + " %");
                    });
                }

                stamper.close();
                reader.close();

                runOnUiThread(() -> {
                    mActivityCreatePdfDoneBinding.createLayout.txtProgress.setText("100 %");
                    mCreatingStatus = STATE_DONE;

                    mAddWaterMarkViewModel.saveRecent(mOutputPath, getString(R.string.add_water_mark));

                    setForLayoutView();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mCreatingStatus = STATE_FAIL;

                    ToastUtils.showMessageShort(this, getString(R.string.add_watermark_error));
                    setForLayoutView();
                });

                FileUtils.deleteFileOnExist(mOutputPath);
            }
        });
    }
}
