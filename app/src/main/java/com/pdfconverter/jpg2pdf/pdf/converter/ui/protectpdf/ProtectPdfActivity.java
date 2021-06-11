package com.pdfconverter.jpg2pdf.pdf.converter.ui.protectpdf;

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
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityProtectPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SetPasswordFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.protectpdf.done.ProtectPdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProtectPdfActivity extends BaseBindingActivity<ActivityProtectPdfBinding, ProtectPdfViewModel> implements ProtectPdfNavigator, OnFileItemClickListener {

    private ProtectPdfViewModel mProtectPdfViewModel;
    private ActivityProtectPdfBinding mActivityProtectPdfBinding;
    private DocumentData mSelectedFile = null;
    private String mFilePath = null;
    private String mPassword;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;

    private SweetAlertDialog mRequestPermissionDialog;

    private List<FileData> mListFileSelector = new ArrayList<>();
    private FileListNoAdsAdapter mFileListSelectorAdapter;

    private String mFileSelectorSearchKey = "";

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_protect_pdf;
    }

    @Override
    public ProtectPdfViewModel getViewModel() {
        mProtectPdfViewModel = ViewModelProviders.of(this).get(ProtectPdfViewModel.class);
        return mProtectPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityProtectPdfBinding = getViewDataBinding();
        mProtectPdfViewModel.setNavigator(this);

        initView();
    }

    @Override
    protected void initView() {
        mActivityProtectPdfBinding.selectLayout.toolbar.toolbarNameTv.setText(getString(R.string.protect_pdf));
        mActivityProtectPdfBinding.selectLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityProtectPdfBinding.selectLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        mActivityProtectPdfBinding.selectLayout.btnLayoutSelectFile.setOnClickListener(view -> checkPermissionBeforeGetFile());

        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);
        requestForFileSelector();

        mActivityProtectPdfBinding.selectLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_FILE_REQUEST) {
            if (data == null) return;
            Uri uri = data.getData();
            if (uri == null) return;

            if (RealPathUtil.getInstance().isDriveFile(uri)) {
                startDownloadFromGoogleDrive(uri);
                return;
            }

            //Getting Absolute Path
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
        if (uri != null && filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
            if (PdfUtils.isPDFEncrypted(filePath)) {
                SnackBarUtils.getSnackbar(this, getString(R.string.protect_pdf_file_already_set_password)).show();
                return;
            }

            mFilePath = filePath;
            String name = FileUtils.getFileName(this, uri);
            mSelectedFile = new DocumentData(name, uri, mFilePath);

            mPassword = null;

            showDialogSetPassword();
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
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

    private void showDialogSetPassword() {
        SetPasswordFileDialog setPasswordFileDialog = new SetPasswordFileDialog(this, new SetPasswordFileDialog.SetPasswordFileListener() {
            @Override
            public void onSubmitPassword(String password) {
                mPassword = password;
                startSetPassword();
            }

            @Override
            public void onCancel() {
                mPassword = null;
                mSelectedFile = null;
                mFilePath = null;
            }
        });
        setPasswordFileDialog.show();
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityProtectPdfBinding.selectLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mProtectPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mProtectPdfViewModel.getUnlockedFileList();
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
                if (mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityProtectPdfBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
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

    private void startChooseFileActivity() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, getString(R.string.pdf_type));
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.protect_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    private void startSetPassword() {
        if (mSelectedFile == null || mFilePath == null) return;

        if (mPassword.length() == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.protect_pdf_please_enter_password));
        } else {
            showDoneAdsBeforeAction(() -> {
                Intent intent = new Intent(this, ProtectPdfDoneActivity.class);
                intent.putExtra(EXTRA_FILE_PATH, mFilePath);
                intent.putExtra(EXTRA_PASSWORD, mPassword);
                startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
            });
        }
    }
}
