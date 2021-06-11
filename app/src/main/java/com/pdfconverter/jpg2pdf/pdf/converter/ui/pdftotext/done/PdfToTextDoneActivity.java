package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.done;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PDFToTextOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityPdfToTextDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemTextActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.common.CommonDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ConfirmDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextNavigator;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftotext.PdfToTextListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftotext.PdfToTextManager;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PdfToTextDoneActivity extends BaseBindingActivity<ActivityPdfToTextDoneBinding, PdfToTextViewModel> implements PdfToTextNavigator, OnItemTextActionListener, PdfToTextListener {
    private static final int STATE_NOT_STARTED = -1;
    private static final int STATE_CREATING = 0;
    private static final int STATE_DONE = 1;

    private int mCreatingStatus = STATE_NOT_STARTED;

    private PdfToTextViewModel mPdfToTextViewModel;
    private ActivityPdfToTextDoneBinding mActivityPdfToTextDoneBinding;

    private PdfToTextManager mTaskManager;
    private PDFToTextOptions mOptions;

    private ArrayList<TextExtractData> mOutputList = new ArrayList<>();
    private PdfToTextAdapter mOutputAdapter;
    private String mOutputTxtFile = null;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_pdf_to_text_done;
    }

    @Override
    public PdfToTextViewModel getViewModel() {
        mPdfToTextViewModel = ViewModelProviders.of(this).get(PdfToTextViewModel.class);
        return mPdfToTextViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityPdfToTextDoneBinding = getViewDataBinding();
        mPdfToTextViewModel.setNavigator(this);

        mCreatingStatus = STATE_NOT_STARTED;

        String json = getIntent().getStringExtra(EXTRA_DATA_CREATE_PDF);
        mOptions = new Gson().fromJson(json, PDFToTextOptions.class);
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
    protected void initView() {
        mActivityPdfToTextDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.done_create_pdf_title));
        mActivityPdfToTextDoneBinding.createLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mActivityPdfToTextDoneBinding.createLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mActivityPdfToTextDoneBinding.createLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mOutputAdapter = new PdfToTextAdapter(this);
        mActivityPdfToTextDoneBinding.createLayout.recyclerView.setAdapter(mOutputAdapter);
        mActivityPdfToTextDoneBinding.createLayout.backToHome.setOnClickListener(view -> {
            showBackHomeAdsBeforeAction(() -> {
                setResult(RESULT_NEED_FINISH);
                finish();
            });
        });
        mActivityPdfToTextDoneBinding.createLayout.saveToTxt.setOnClickListener(view -> {
            saveToTxtFile();
        });

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
    public void onBackPressed() {
        if (mCreatingStatus == STATE_DONE) {
            finish();
        } else {
            ConfirmDialog confirmDialog = new ConfirmDialog(this, getString(R.string.warning_text), getString(R.string.confirm_cancel_current_action), new ConfirmDialog.ConfirmListener() {
                @Override
                public void onSubmit() {
                    if (mCreatingStatus == STATE_CREATING && mTaskManager != null && !mTaskManager.isCancelled()) {
                        mTaskManager.cancel(true);
                    }
                }

                @Override
                public void onCancel() {

                }
            });
            confirmDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PREVIEW_FILE_REQUEST) {
            if (resultCode == RESULT_FILE_DELETED) {
                finish();
            }
        } else if (requestCode == CREATE_PDF_FROM_SELECT_FILE) {
            if (resultCode == RESULT_NEED_FINISH) {
                setResult(RESULT_NEED_FINISH);
                finish();
                return;
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
        mActivityPdfToTextDoneBinding.createLayout.nameFile.setText(FileUtils.getFileName(mOptions.getInputFilePath()));
        mActivityPdfToTextDoneBinding.createLayout.nameFile.setOnClickListener(v -> gotoPdfFileViewActivity(mOptions.getInputFilePath()));

        if (mCreatingStatus == STATE_NOT_STARTED) {
            mActivityPdfToTextDoneBinding.createLayout.contentView.setVisibility(View.GONE);
        } else if (mCreatingStatus == STATE_CREATING) {
            mActivityPdfToTextDoneBinding.createLayout.optionPdf.setVisibility(View.GONE);
            mActivityPdfToTextDoneBinding.createLayout.progressPdf.setVisibility(View.VISIBLE);
        } else {
            mActivityPdfToTextDoneBinding.createLayout.optionPdf.setVisibility(View.VISIBLE);
            mActivityPdfToTextDoneBinding.createLayout.progressPdf.setVisibility(View.GONE);
        }
    }

    private void startCreateFile() {
        mTaskManager = new PdfToTextManager(this, this, mOptions);
        mTaskManager.execute();
    }

    private void saveToTxtFile() {
        RenameFileDialog renameFileDialog = new RenameFileDialog(this, FileUtils.getDefaultFileName(DataConstants.PDF_TO_TEXT_PREFIX_NAME), new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String newName) {
                startSaveTxtFile(newName);
            }

            @Override
            public void onCancel() {

            }
        });
        renameFileDialog.show();
    }

    private void startSaveTxtFile(String input) {
        SweetAlertDialog savingDialog = DialogFactory.getDialogProgress(this, getString(R.string.saving_text));
        savingDialog.show();

        AsyncTask.execute(() -> {
            try {
                String inputString = input.trim();
                String fileName = inputString + DataConstants.TEXT_EXTENSION;

                String finalOutputFileDir = FileUtils.getUniqueOtherFileName(this, DirectoryUtils.getDefaultStorageLocation() + fileName, DataConstants.TEXT_EXTENSION);

                File textFile = new File(finalOutputFileDir);
                FileWriter writer = new FileWriter(textFile);
                for (TextExtractData textExtractData : mOutputList) {
                    if (textExtractData.getTextContent() != null && textExtractData.getTextContent().length() > 0) {
                        writer.append(textExtractData.getTextContent()).append("\n");
                    }
                }
                writer.flush();
                writer.close();

                runOnUiThread(() -> {
                    mOutputTxtFile = textFile.getAbsolutePath();
                    savingDialog.dismiss();

                    openSaveResultActivity();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    savingDialog.dismiss();
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.pdf_to_text_can_not_save_file));
                });
            }
        });
    }

    private void openSaveResultActivity() {
        Intent intent = new Intent(PdfToTextDoneActivity.this, CommonDoneActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, mOutputTxtFile);
        intent.putExtra(EXTRA_FILE_TYPE, DataConstants.FILE_TYPE_TXT);
        intent.putExtra(EXTRA_FILE_EXTENSION, ".txt");
        startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
    }

    @Override
    public void onClick(int position) {
        // TODO click item
    }

    @Override
    public void onOption(int position) {
        if (mOutputList.get(position).getTextContent() != null && mOutputList.get(position).getTextContent().length() > 0) {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied from " + AppConstants.APP_NAME, mOutputList.get(position).getTextContent());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    SnackBarUtils.getSnackbar(this, getString(R.string.pdf_to_text_copied_text)).show();
                } else {
                    ToastUtils.showSystemIssueToast(this);
                }
            } catch (Exception e) {
                ToastUtils.showSystemIssueToast(this);
            }
        } else {
            ToastUtils.showMessageShort(this, getString(R.string.pdf_to_text_no_text_for_this_page));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreateStart() {
        runOnUiThread(() -> {
            mCreatingStatus = STATE_CREATING;
            setForLayoutView();
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreateFinish(int numberSuccess, int numberError) {
        runOnUiThread(() -> {
            mCreatingStatus = STATE_DONE;
            setForLayoutView();

            if (numberSuccess == 0) {
                ToastUtils.showMessageShort(this, getString(R.string.pdf_to_text_extract_error));
            } else {
                ToastUtils.showMessageShort(this, getString(R.string.pdf_to_text_extract_finish));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateProcess(int numberSuccess, int numberError, TextExtractData output) {
        runOnUiThread(() -> {
            if (output != null) {
                mOutputList.add(output);
                mOutputAdapter.addData(output);
            }
        });
    }
}
