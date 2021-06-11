package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.done;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PDFToImageOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityPdfToImageDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemImageActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.imageview.ImageViewActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageNavigator;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftoimage.PdfToImageListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftoimage.PdfToImageManager;

import java.io.File;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants.AUTHORITY_APP;

public class PdfToImageDoneActivity extends BaseBindingActivity<ActivityPdfToImageDoneBinding, PdfToImageViewModel> implements PdfToImageNavigator, OnItemImageActionListener, PdfToImageListener {
    private static final int STATE_NOT_STARTED = -1;
    private static final int STATE_CREATING = 0;
    private static final int STATE_DONE = 1;

    private int mCreatingStatus = STATE_NOT_STARTED;

    private PdfToImageViewModel mPdfToImageViewModel;
    private ActivityPdfToImageDoneBinding mActivityPdfToImageDoneBinding;

    private PdfToImageManager mTaskManager;
    private PDFToImageOptions mOptions;

    private PdfToImageAdapter mOutputAdapter;
    private ArrayList<ImageExtractData> mOutputList = new ArrayList<>();

    private int mNumberPage;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_pdf_to_image_done;
    }

    @Override
    public PdfToImageViewModel getViewModel() {
        mPdfToImageViewModel = ViewModelProviders.of(this).get(PdfToImageViewModel.class);
        return mPdfToImageViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityPdfToImageDoneBinding = getViewDataBinding();
        mPdfToImageViewModel.setNavigator(this);

        mCreatingStatus = STATE_NOT_STARTED;

        String json = getIntent().getStringExtra(EXTRA_DATA_CREATE_PDF);
        mOptions = new Gson().fromJson(json, PDFToImageOptions.class);
        if (mOptions == null || mOptions.getInputFilePath() == null) {
            SweetAlertDialog errorDialog = DialogFactory.getDialogError(this, getString(R.string.data_not_valid));
            errorDialog.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finish();
            });
            errorDialog.show();
        } else {
            startCreateFile();
        }

        initView();
    }

    @Override
    protected void onDestroy() {
        DirectoryUtils.cleanImageStorage(this);
        super.onDestroy();
    }

    @Override
    protected void initView() {
        mActivityPdfToImageDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.pdf_to_image_title));
        mActivityPdfToImageDoneBinding.createLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mActivityPdfToImageDoneBinding.createLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mActivityPdfToImageDoneBinding.createLayout.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mOutputAdapter = new PdfToImageAdapter(this);
        mActivityPdfToImageDoneBinding.createLayout.recyclerView.setAdapter(mOutputAdapter);
        mActivityPdfToImageDoneBinding.createLayout.backToHome.setOnClickListener(view -> {
            showBackHomeAdsBeforeAction(() -> {
                setResult(RESULT_NEED_FINISH);
                finish();
            });
        });

        setForLayoutView();
    }

    @Override
    public void onBackPressed() {
        if (mCreatingStatus == STATE_DONE) {
            finish();
        } else {
            SweetAlertDialog confirmCancel = DialogFactory.getDialogConfirm(this, getString(R.string.confirm_cancel_current_action));
            confirmCancel.setConfirmClickListener(sweetAlertDialog -> {
                if (mCreatingStatus == STATE_CREATING && mTaskManager != null && !mTaskManager.isCancelled()) {
                    mTaskManager.cancel(true);
                }
                sweetAlertDialog.dismiss();
            });
            confirmCancel.show();
        }
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
        mNumberPage = mOptions.getNumberPage();

        mActivityPdfToImageDoneBinding.createLayout.nameFile.setText(mOptions.getInputFileData().getDisplayName());
        mActivityPdfToImageDoneBinding.createLayout.nameFile.setOnClickListener(v -> gotoPdfFileViewActivity(mOptions.getInputFilePath()));

        if (mCreatingStatus == STATE_NOT_STARTED) {
            mActivityPdfToImageDoneBinding.createLayout.contentView.setVisibility(View.GONE);
        } else if (mCreatingStatus == STATE_CREATING) {
            mActivityPdfToImageDoneBinding.createLayout.contentView.setVisibility(View.VISIBLE);
            mActivityPdfToImageDoneBinding.createLayout.optionPdf.setVisibility(View.GONE);
            mActivityPdfToImageDoneBinding.createLayout.progressPdf.setVisibility(View.VISIBLE);
        } else {
            mActivityPdfToImageDoneBinding.createLayout.contentView.setVisibility(View.VISIBLE);
            mActivityPdfToImageDoneBinding.createLayout.optionPdf.setVisibility(View.VISIBLE);
            mActivityPdfToImageDoneBinding.createLayout.progressPdf.setVisibility(View.GONE);
        }
    }

    private void startCreateFile() {
        mTaskManager = new PdfToImageManager(this, this, mOptions);
        mTaskManager.execute();
    }

    @Override
    public void onClick(int position) {
        Intent intent = new Intent(PdfToImageDoneActivity.this, ImageViewActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, mOutputList.get(position).getFilePath());
        startActivity(intent);
    }

    @Override
    public void onDownload(int position) {
        if (FileUtils.copyImageToDownload(this, mOutputList.get(position).getFilePath())) {
            SnackBarUtils.getSnackbar(this, getString(R.string.download_success_text)).show();
        } else {
            SnackBarUtils.getSnackbar(this, getString(R.string.download_not_success_text)).show();
        }
    }

    @Override
    public void onShare(int position) {
        String filePath = mOutputList.get(position).getFilePath();
        try {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(PdfToImageDoneActivity.this, AUTHORITY_APP, new File(filePath)));
            sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(sharingIntent, "Share file with"));
        } catch (Exception e) {
            ToastUtils.showMessageShort(getApplicationContext(), "Can not share file now.");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreateStart(int type) {
        runOnUiThread(() -> {
            if (type == PdfToImageManager.TYPE_EXACT) {
                mActivityPdfToImageDoneBinding.createLayout.completeExtract.setText("Image created: 0/" + mNumberPage);
            } else {
                mActivityPdfToImageDoneBinding.createLayout.completeExtract.setText("Image created: 0");
            }

            mCreatingStatus = STATE_CREATING;
            setForLayoutView();
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreateFinish(int numberSuccess, int numberError, int type) {
        runOnUiThread(() -> {
            if (type == PdfToImageManager.TYPE_EXACT) {
                mActivityPdfToImageDoneBinding.createLayout.completeExtract.setText("Image created: " + numberSuccess + "/" + mNumberPage);
            } else {
                mActivityPdfToImageDoneBinding.createLayout.completeExtract.setText("Image created: " + numberSuccess);
            }

            mCreatingStatus = STATE_DONE;
            setForLayoutView();

            if (numberSuccess == 0) {
                if (type == PdfToImageManager.TYPE_EXACT) {
                    ToastUtils.showMessageShort(this, getString(R.string.pdf_to_image_extract_error));
                } else {
                    ToastUtils.showMessageShort(this, getString(R.string.pdf_to_image_find_error));
                }

                mActivityPdfToImageDoneBinding.createLayout.btnSaveGallery.setOnClickListener(view -> {
                    if (mCreatingStatus != STATE_DONE) return;
                    if (type == PdfToImageManager.TYPE_EXACT) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.pdf_to_image_extract_error));
                    } else {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.pdf_to_image_find_error));
                    }

                });
            } else {
                if (type == PdfToImageManager.TYPE_EXACT) {
                    ToastUtils.showMessageShort(this, getString(R.string.pdf_to_image_extract_finish));
                } else {
                    ToastUtils.showMessageShort(this, getString(R.string.pdf_to_image_find_finish));
                }

                mActivityPdfToImageDoneBinding.createLayout.btnSaveGallery.setOnClickListener(view -> {
                    if (mCreatingStatus != STATE_DONE) return;

                    SweetAlertDialog savingDialog = DialogFactory.getDialogProgress(this, getString(R.string.saving_text));
                    savingDialog.show();
                    AsyncTask.execute(() -> {
                        int numberSavedSuccess = 0;
                        for (int i = 0; i < mOutputList.size(); i++) {
                            if (FileUtils.copyImageToDownload(this, mOutputList.get(i).getFilePath())) {
                                numberSavedSuccess ++;
                            }
                        }

                        int finalNumberSavedSuccess = numberSavedSuccess;
                        runOnUiThread(() -> {
                            savingDialog.dismiss();
                            SnackBarUtils.getSnackbar(this, "Saved " + finalNumberSavedSuccess + " files to Download").show();
                        });
                    });

                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateProcess(int numberSuccess, int numberError, ImageExtractData output, int type) {
        runOnUiThread(() -> {
            if (type == PdfToImageManager.TYPE_EXACT) {
                mActivityPdfToImageDoneBinding.createLayout.completeExtract.setText("Image created: " + numberSuccess + "/" + mNumberPage);
            } else {
                mActivityPdfToImageDoneBinding.createLayout.completeExtract.setText("Image created: " + numberSuccess);
            }

            if (output != null) {
                mOutputList.add(output);
                mOutputAdapter.addData(output);
            }
        });
    }
}
