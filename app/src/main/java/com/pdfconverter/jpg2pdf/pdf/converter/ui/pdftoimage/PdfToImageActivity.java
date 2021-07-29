package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PDFToImageOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityPdfToImageBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.done.PdfToImageDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftoimage.PdfToImageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PdfToImageActivity extends BaseBindingActivity<ActivityPdfToImageBinding, PdfToImageViewModel> implements PdfToImageNavigator, OnFileItemClickListener {

    private PdfToImageViewModel mPdfToImageViewModel;
    private ActivityPdfToImageBinding mActivityPdfToImageBinding;

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
        return R.layout.activity_pdf_to_image;
    }

    @Override
    public PdfToImageViewModel getViewModel() {
        mPdfToImageViewModel = ViewModelProviders.of(this).get(PdfToImageViewModel.class);
        return mPdfToImageViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityPdfToImageBinding = getViewDataBinding();
        mPdfToImageViewModel.setNavigator(this);

        mIsFromOtherScreen = false;

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_PDF)) {
            mIsFromOtherScreen = true;
            mFilePath = extraFilePath;
            String name = FileUtils.getFileName(extraFilePath);
            mSelectedFile = new DocumentData(name, Uri.fromFile(new File(mFilePath)), mFilePath);

            mNumberPage = FileUtils.getNumberPages(mFilePath);
            mActivityPdfToImageBinding.defaultLayout.enterStartPageEdt.setText("1");
            mActivityPdfToImageBinding.defaultLayout.enterEndPageEdt.setText("" + mNumberPage);
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivityPdfToImageBinding.selectLayout.toolbar.toolbarNameTv.setText(getString(R.string.pdf_to_image_title));
        mActivityPdfToImageBinding.selectLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityPdfToImageBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.pdf_to_image_title));
        mActivityPdfToImageBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityPdfToImageBinding.selectLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);
        preloadDoneAdsIfInit();

        mActivityPdfToImageBinding.selectLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        setForLayoutView();
        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        }

        mActivityPdfToImageBinding.selectLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                SnackBarUtils.getSnackbar(this, getString(R.string.edit_pdf_file_encrypted))
                        .setAction(R.string.remove_password_now, v -> gotoUnlockPasswordActivity(filePath, null)).show();
                return;
            }

            mFilePath = filePath;
            String name = FileUtils.getFileName(this, uri);
            mNumberPage = FileUtils.getNumberPages(mFilePath);
            mSelectedFile = new DocumentData(name, uri, mFilePath);

            mActivityPdfToImageBinding.defaultLayout.enterStartPageEdt.setText("1");
            mActivityPdfToImageBinding.defaultLayout.enterEndPageEdt.setText("" + mNumberPage);

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
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.pdf_to_image_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {

        if (mSelectedFile == null || mFilePath == null) {
            mActivityPdfToImageBinding.selectLayout.contentView.setVisibility(View.VISIBLE);
            mActivityPdfToImageBinding.defaultLayout.contentView.setVisibility(View.GONE);
        } else {
            mActivityPdfToImageBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivityPdfToImageBinding.selectLayout.contentView.setVisibility(View.GONE);

            mActivityPdfToImageBinding.defaultLayout.nameFile.setText(mSelectedFile.getDisplayName());
            mActivityPdfToImageBinding.defaultLayout.descriptionFile.setText(getString(R.string.pdf_to_image_number_page) + " " + mNumberPage);

            mActivityPdfToImageBinding.defaultLayout.btnFindImages.setOnClickListener(v -> startMakeImage(PdfToImageManager.TYPE_FIND));
            mActivityPdfToImageBinding.defaultLayout.btnExtractImages.setOnClickListener(v -> startMakeImage(PdfToImageManager.TYPE_EXACT));

            mActivityPdfToImageBinding.defaultLayout.nameFile.setOnClickListener(v -> gotoPdfFileViewActivity(mFilePath));
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityPdfToImageBinding.selectLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mPdfToImageViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mPdfToImageViewModel.getUnlockedFileList();
    }

    private void updateListFileSelector(List<FileData> fileDataList) {
        mListFileSelector = new ArrayList<>();
        mListFileSelector.addAll(fileDataList);

        if (mListFileSelector.size() > 0) {
            if (mFileSelectorSearchKey.length() > 0) {
                updateForSearchFileSelector();
            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {

        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityPdfToImageBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

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

    private void startMakeImage(int type) {
        if (mFilePath == null || mSelectedFile == null) {
            setForLayoutView();
            return;
        }

        String startPageString = "1";
        if (mActivityPdfToImageBinding.defaultLayout.enterStartPageEdt.getText() != null) {
            startPageString = mActivityPdfToImageBinding.defaultLayout.enterStartPageEdt.getText().toString();
        }

        String endPageString = "" + mNumberPage;
        if (mActivityPdfToImageBinding.defaultLayout.enterEndPageEdt.getText() != null) {
            endPageString = mActivityPdfToImageBinding.defaultLayout.enterEndPageEdt.getText().toString();
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
            ToastUtils.showMessageShort(this, getString(R.string.pdf_to_image_start_page_not_valid));
            return;
        }

        if (endPage > mNumberPage) {
            ToastUtils.showMessageShort(this, getString(R.string.pdf_to_image_end_page_not_valid));
            return;
        }

        CommonUtils.hideKeyboard(this);

        PDFToImageOptions pdfToImageOptions = new PDFToImageOptions(mFilePath, mSelectedFile, startPage, endPage, mNumberPage, type);
        checkIAPDoneBeforeAction(() -> {
            Gson gson = new Gson();
            String json = gson.toJson(pdfToImageOptions);
            Intent intent = new Intent(this, PdfToImageDoneActivity.class);
            intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
            startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
        });
    }
}
