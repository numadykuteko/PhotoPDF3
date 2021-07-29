package com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf;

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
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityUnlockPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RemovePasswordFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.unlockpdf.done.UnlockPdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UnlockPdfActivity extends BaseBindingActivity<ActivityUnlockPdfBinding, UnlockPdfViewModel> implements UnlockPdfNavigator, OnFileItemClickListener {

    private UnlockPdfViewModel mUnlockPdfViewModel;
    private ActivityUnlockPdfBinding mActivityUnlockPdfBinding;
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
        return R.layout.activity_unlock_pdf;
    }

    @Override
    public UnlockPdfViewModel getViewModel() {
        mUnlockPdfViewModel = ViewModelProviders.of(this).get(UnlockPdfViewModel.class);
        return mUnlockPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityUnlockPdfBinding = getViewDataBinding();
        mUnlockPdfViewModel.setNavigator(this);

        initView();
    }

    @Override
    protected void initView() {
        mActivityUnlockPdfBinding.selectLayout.toolbar.toolbarNameTv.setText(getString(R.string.unlock_pdf));
        mActivityUnlockPdfBinding.selectLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityUnlockPdfBinding.selectLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.loadingText.setText(getString(R.string.loading_locked_file_text));
        mActivityUnlockPdfBinding.selectLayout.btnLayoutSelectFile.setOnClickListener(view -> checkPermissionBeforeGetFile());

        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        requestForFileSelector();

        mActivityUnlockPdfBinding.selectLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                mFilePath = filePath;
                String name = FileUtils.getFileName(this, uri);
                mSelectedFile = new DocumentData(name, uri, mFilePath);

                mPassword = null;

                showDialogRemovePassword();

            } else {
                ToastUtils.showMessageLong(this, getString(R.string.unlock_pdf_file_not_encrypted_before));
            }
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
    }

    private void showDialogRemovePassword() {
        RemovePasswordFileDialog removePasswordFileDialog = new RemovePasswordFileDialog(this, mFilePath, new RemovePasswordFileDialog.RemovePasswordFileListener() {
            @Override
            public void onSubmitPassword(String password) {
                mPassword = password;
                startRemovePassword();
            }

            @Override
            public void onCancel() {
                mPassword = null;
                mSelectedFile = null;
                mFilePath = null;
            }
        });
        removePasswordFileDialog.show();
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

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityUnlockPdfBinding.selectLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mUnlockPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mUnlockPdfViewModel.getLockedFileList();
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
                if (mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {

        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityUnlockPdfBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
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
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.unlock_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    private void startRemovePassword() {
        if (mSelectedFile == null || mFilePath == null) return;

        if (mPassword.length() == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.unlock_pdf_please_enter_password));
        } else if (!PdfUtils.isPasswordValid(mFilePath, mPassword.getBytes())) {
            ToastUtils.showMessageShort(this, getString(R.string.unlock_pdf_enter_wrong_password));
        } else {
            checkIAPDoneBeforeAction(() -> {
                Intent intent = new Intent(this, UnlockPdfDoneActivity.class);
                intent.putExtra(EXTRA_FILE_PATH, mFilePath);
                intent.putExtra(EXTRA_PASSWORD, mPassword);
                startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
            });
        }
    }
}
