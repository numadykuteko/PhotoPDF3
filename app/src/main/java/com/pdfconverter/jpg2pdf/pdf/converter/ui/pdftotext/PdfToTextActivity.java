package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PDFToTextOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityPdfToTextBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.done.PdfToTextDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PdfToTextActivity extends BaseBindingActivity<ActivityPdfToTextBinding, PdfToTextViewModel> implements PdfToTextNavigator, OnFileItemClickListener {

    private PdfToTextViewModel mPdfToTextViewModel;
    private ActivityPdfToTextBinding mActivityPdfToTextBinding;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;

    private SweetAlertDialog mRequestPermissionDialog;

    private String mFilePath = null;

    private DocumentData mSelectedFile = null;
    private int mNumberPage = -1;

    private List<FileData> mListFileSelector = new ArrayList<>();
    private FileListNoAdsAdapter mFileListSelectorAdapter;

    private boolean mIsFromOtherScreen = false;
    private String mFileSelectorSearchKey = "";

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_pdf_to_text;
    }

    @Override
    public PdfToTextViewModel getViewModel() {
        mPdfToTextViewModel = ViewModelProviders.of(this).get(PdfToTextViewModel.class);
        return mPdfToTextViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityPdfToTextBinding = getViewDataBinding();
        mPdfToTextViewModel.setNavigator(this);

        mIsFromOtherScreen = false;

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_PDF)) {
            mIsFromOtherScreen = true;
            mFilePath = extraFilePath;
            String name = FileUtils.getFileName(extraFilePath);

            mNumberPage = FileUtils.getNumberPages(mFilePath);
            mSelectedFile = new DocumentData(name, Uri.fromFile(new File(mFilePath)), mFilePath);

            mActivityPdfToTextBinding.defaultLayout.enterStartPageEdt.setText("1");
            mActivityPdfToTextBinding.defaultLayout.enterEndPageEdt.setText("" + mNumberPage);
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivityPdfToTextBinding.selectLayout.toolbar.toolbarNameTv.setText(getString(R.string.pdf_to_text_title));
        mActivityPdfToTextBinding.selectLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityPdfToTextBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.pdf_to_text_title));
        mActivityPdfToTextBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityPdfToTextBinding.selectLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);
        preloadDoneAdsIfInit();

        mActivityPdfToTextBinding.selectLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        setForLayoutView();
        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        }

        mActivityPdfToTextBinding.selectLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null) newText = "";
                mFileSelectorSearchKey = newText.trim();
                updateForSearchFileSelector();

                return false;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        startChooseFileActivity();
                        sweetAlertDialog.dismiss();
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.couldnt_get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                }
                break;

            case REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestForFileSelector();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsRequestFullPermission) {
            mIsRequestFullPermission = false;

            if (mRequestFullPermissionCode == REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE) {
                if (!notHaveStoragePermission()) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        startChooseFileActivity();
                        sweetAlertDialog.dismiss();
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.couldnt_get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                }
            } else {
                if (!notHaveStoragePermission()) {
                    requestForFileSelector();
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_FILE_REQUEST && data != null) {
            if (data == null) return;
            Uri uri = data.getData();
            if (uri == null) return;

            if (RealPathUtil.getInstance().isDriveFile(uri)) {
                startDownloadFromGoogleDrive(uri);
                return;
            }

            String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_PDF);
            checkFilePathGet(uri, filePath);
        } else if (requestCode == CREATE_PDF_FROM_SELECT_FILE) {
            if (resultCode == RESULT_NEED_FINISH) {
                finish();
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void updateFilePathFromGGDrive(Uri uri, String filePath) {
        super.updateFilePathFromGGDrive(uri, filePath);
        checkFilePathGet(uri, filePath);
    }

    private void checkFilePathGet(Uri uri, String filePath) {
        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
            if (PdfUtils.isPDFEncrypted(filePath)) {
                SnackBarUtils.getSnackbar(this, getString(R.string.pdf_to_text_file_encrypted))
                        .setAction(R.string.remove_password_now, v -> gotoUnlockPasswordActivity(filePath, null)).show();
                return;
            }

            mFilePath = filePath;
            String name = FileUtils.getFileName(this, uri);
            mNumberPage = FileUtils.getNumberPages(mFilePath);
            mSelectedFile = new DocumentData(name, uri, mFilePath);

            mActivityPdfToTextBinding.defaultLayout.enterStartPageEdt.setText("1");
            mActivityPdfToTextBinding.defaultLayout.enterEndPageEdt.setText("" + mNumberPage);

            setForLayoutView();
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
    }

    @Override
    public void onBackPressed() {
        if (!mIsFromOtherScreen && (mFilePath != null || mSelectedFile != null)) {
            mSelectedFile = null;
            mFilePath = null;
            CommonUtils.hideKeyboard(this);
            setForLayoutView();
            return;
        }

        super.onBackPressed();
    }

    private void startChooseFileActivity() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, getString(R.string.pdf_type));
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.pdf_to_text_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedFile == null || mFilePath == null) {
            mActivityPdfToTextBinding.selectLayout.contentView.setVisibility(View.VISIBLE);
            mActivityPdfToTextBinding.defaultLayout.contentView.setVisibility(View.GONE);

        } else {
            mActivityPdfToTextBinding.selectLayout.contentView.setVisibility(View.GONE);
            mActivityPdfToTextBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);

            mActivityPdfToTextBinding.defaultLayout.nameFile.setText(mSelectedFile.getDisplayName());
            mActivityPdfToTextBinding.defaultLayout.descriptionFile.setText(getString(R.string.pdf_to_text_number_page) + " " + mNumberPage);

            mActivityPdfToTextBinding.defaultLayout.btnExtractText.setOnClickListener(v -> startMakeText());

            mActivityPdfToTextBinding.defaultLayout.nameFile.setOnClickListener(v -> gotoPdfFileViewActivity(mFilePath));
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityPdfToTextBinding.selectLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mPdfToTextViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mPdfToTextViewModel.getUnlockedFileList();
    }

    private void updateListFileSelector(List<FileData> fileDataList) {
        if (mListFileSelector == fileDataList) {
            return;
        }

        mListFileSelector = new ArrayList<>();
        mListFileSelector.addAll(fileDataList);

        if (mListFileSelector.size() > 0) {
            if (mFileSelectorSearchKey.length() > 0) {
                updateForSearchFileSelector();
            } else {
                Parcelable oldPosition = null;
                if (mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
    }

    private void updateForSearchFileSelector() {
        if (mListFileSelector.size() > 0) {
            if (mFileSelectorSearchKey != null && mFileSelectorSearchKey.length() > 0) {
                List<FileData> searchList = new ArrayList<>();
                for (FileData fileData : mListFileSelector) {
                    String fileName = FileUtils.getFileName(fileData.getFilePath());
                    if (fileName.toLowerCase().contains(mFileSelectorSearchKey.toLowerCase())) {
                        searchList.add(fileData);
                    }
                }
                mFileListSelectorAdapter.setData(searchList);
                mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityPdfToTextBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            }
        }
    }

    private void startRequestPermissionForFileSelector() {
        if (notHaveStoragePermission()) {
            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR);
        } else {
            requestForFileSelector();
        }
    }

    private void checkPermissionBeforeGetFile() {
        if (notHaveStoragePermission()) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE);
            });
            mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                sweetAlertDialog.setContentText(getString(R.string.couldnt_get_file_now));
                sweetAlertDialog.setConfirmClickListener(Dialog::dismiss);
                sweetAlertDialog.showCancelButton(false);
                sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
            });
            mRequestPermissionDialog.show();
        } else {
            startChooseFileActivity();
        }
    }

    private void startMakeText() {
        if (mFilePath == null || mSelectedFile == null) {
            setForLayoutView();
            return;
        }

        String startPageString = "1";
        if (mActivityPdfToTextBinding.defaultLayout.enterStartPageEdt.getText() != null) {
            startPageString = mActivityPdfToTextBinding.defaultLayout.enterStartPageEdt.getText().toString();
        }

        String endPageString = "" + mNumberPage;
        if (mActivityPdfToTextBinding.defaultLayout.enterEndPageEdt.getText() != null) {
            endPageString = mActivityPdfToTextBinding.defaultLayout.enterEndPageEdt.getText().toString();
        }

        int startPage = 1;
        int endPage = mNumberPage;

        try {
            startPage = Integer.parseInt(startPageString);
        } catch (Exception ignored) {
        }

        try {
            endPage = Integer.parseInt(endPageString);
        } catch (Exception ignored) {
        }

        if (startPage <= 0 || startPage > endPage || startPage > mNumberPage) {
            ToastUtils.showMessageShort(this, getString(R.string.pdf_to_text_start_page_not_valid));
            return;
        }

        if (endPage > mNumberPage) {
            ToastUtils.showMessageShort(this, getString(R.string.pdf_to_text_end_page_not_valid));
            return;
        }

        CommonUtils.hideKeyboard(this);
        PDFToTextOptions pdfToTextOptions = new PDFToTextOptions(mFilePath, startPage, endPage);

        showDoneAdsBeforeAction(() -> {
            Gson gson = new Gson();
            String json = gson.toJson(pdfToTextOptions);
            Intent intent = new Intent(this, PdfToTextDoneActivity.class);
            intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
            startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
        });
    }
}
