package com.pdfconverter.jpg2pdf.pdf.converter.ui.split.done;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitFileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivitySplitPdfDoneBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemSplitActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfNavigator;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.split.SplitPdfListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.split.SplitPdfManager;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SplitPdfDoneActivity extends BaseBindingActivity<ActivitySplitPdfDoneBinding, SplitPdfViewModel> implements SplitPdfNavigator, SplitPdfListener, OnItemSplitActionListener {
    private static final int STATE_NOT_STARTED = -1;
    private static final int STATE_CREATING = 0;
    private static final int STATE_DONE = 1;

    private int mCreatingStatus = STATE_NOT_STARTED;

    private SplitPdfViewModel mSplitPdfViewModel;
    private ActivitySplitPdfDoneBinding mActivitySplitPdfDoneBinding;

    private SplitPdfManager mTaskManager;
    private SplitPDFOptions mOptions;

    private SplitPdfAdapter mOutputAdapter;
    private ArrayList<SplitFileData> mOutputList = new ArrayList<>();

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_split_pdf_done;
    }

    @Override
    public SplitPdfViewModel getViewModel() {
        mSplitPdfViewModel = ViewModelProviders.of(this).get(SplitPdfViewModel.class);
        return mSplitPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySplitPdfDoneBinding = getViewDataBinding();
        mSplitPdfViewModel.setNavigator(this);

        mCreatingStatus = STATE_NOT_STARTED;

        String json = getIntent().getStringExtra(EXTRA_DATA_CREATE_PDF);
        mOptions = new Gson().fromJson(json, SplitPDFOptions.class);
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
        mActivitySplitPdfDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.splitting_pdf_title));
        mActivitySplitPdfDoneBinding.createLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        preloadViewPdfAdsIfInit();

        mActivitySplitPdfDoneBinding.createLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mActivitySplitPdfDoneBinding.createLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mOutputAdapter = new SplitPdfAdapter(this);
        mActivitySplitPdfDoneBinding.createLayout.recyclerView.setAdapter(mOutputAdapter);

        mActivitySplitPdfDoneBinding.createLayout.splitSuccessBtnFinish.setOnClickListener(view -> {
            showBackHomeAdsBeforeAction(() -> {
                setResult(RESULT_NEED_FINISH);
                finish();
            });
        });
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
            setResult(RESULT_NEED_FINISH);
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

    private void startCreateFile() {
        mTaskManager = new SplitPdfManager(this, this, mOptions);
        mTaskManager.execute();
    }

    @Override
    public void onCreateStart() {
        runOnUiThread(() -> {
            mCreatingStatus = STATE_CREATING;
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreateFinish(int numberSuccess, int numberError) {
        runOnUiThread(() -> {
            mCreatingStatus = STATE_DONE;
            if (numberSuccess == 0) {
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.split_pdf_file_error));
                mActivitySplitPdfDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.split_pdf_fail_title));
            } else {
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.split_pdf_finish));
                mActivitySplitPdfDoneBinding.createLayout.toolbar.toolbarNameTv.setText(getString(R.string.split_pdf_successfully));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdateProcess(int numberSuccess, int numberError, SplitFileData splitFileData) {
        runOnUiThread(() -> {
            int percent = Math.round((numberSuccess + numberError) * 100 / (float) mOptions.getOutputList().size());

            if (splitFileData != null) {
                mOutputList.add(splitFileData);
                mOutputAdapter.addData(splitFileData);

                mSplitPdfViewModel.saveRecent(splitFileData.getFilePath(), getString(R.string.split_pdf));
            }
        });
    }

    @Override
    public void onClick(View view, boolean isCreated, int position) {
        showViewPdfAdsBeforeAction(() -> {
            gotoPdfFilePreviewActivity(mOutputList.get(position).getFilePath());
            onBackPressed();
        });
    }

    @Override
    public void onOption(boolean isCreated, int position) {
        showViewPdfAdsBeforeAction(() -> {
            gotoPdfFilePreviewActivity(mOutputList.get(position).getFilePath());
            onBackPressed();
        });
    }
}
