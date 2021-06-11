package com.pdfconverter.jpg2pdf.pdf.converter.ui.search;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivitySearchBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.AddWaterMarkActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ConfirmDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PdfLockedOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PdfOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf.EditPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsWithOptionAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SearchActivity extends BaseBindingActivity<ActivitySearchBinding, SearchViewModel> implements SearchNavigator, OnFileItemWithOptionClickListener {
    private String mSearchKeyword;

    private ActivitySearchBinding mActivitySearchBinding;
    private SearchViewModel mSearchViewModel;

    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;

    private boolean mIsResume = false;
    private FileListNoAdsWithOptionAdapter mFileListAdapter;
    private List<FileData> mListFile;
    private boolean mIsLoading = false;

    private PdfOptionDialog pdfOptionDialog;
    private PdfLockedOptionDialog pdfLockedOptionDialog;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    public SearchViewModel getViewModel() {
        mSearchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        return mSearchViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSearchViewModel.setNavigator(this);
        mActivitySearchBinding = getViewDataBinding();

        mSearchKeyword = "";
        mIsResume = true;

        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        updateKeywordSearch();
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
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        mIsResume = true;
        super.onResume();

        if (mIsRequestFullPermission) {
            mIsRequestFullPermission = false;

            if (!notHaveStoragePermission()) {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    updateKeywordSearch();
                });
            } else {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                mRequestPermissionDialog.setContentText(getString(R.string.couldnt_get_file_now));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
            }
        }
    }
    @Override
    protected void initView() {
        setNoActionBar();
        preloadMyPdfAdsIfInit();
        setForLiveData();
        setForRecyclerView();
        setForPullRefresh();

        mActivitySearchBinding.searchView.setIconified(false);
        mActivitySearchBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // do nothing
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchKeyword = s;
                updateKeywordSearch();
                return false;
            }
        } );

        mActivitySearchBinding.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

        startGetData(true, false);
    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
        super.onBackPressed();
    }

    private void setForLiveData() {
        mSearchViewModel.getListFileLiveData().observe(this, this::updateData);
        mSearchViewModel.getLoadListLiveData().observe(this, this::updateLoadListDone);
    }

    private void updateLoadListDone(boolean loadListDone) {
    }

    private void setForRecyclerView() {
        mFileListAdapter = new FileListNoAdsWithOptionAdapter(this);
        mActivitySearchBinding.dataList.setAdapter(mFileListAdapter);

        mActivitySearchBinding.dataList.setLayoutManager(new LinearLayoutManager(this));
        mActivitySearchBinding.dataList.setItemAnimator(null);
    }

    private void setForPullRefresh() {
        mActivitySearchBinding.pullRefresh.setOnRefreshListener(() -> {
            startGetData(false, true);
        });
    }

    private void updateKeywordSearch() {
        if (notHaveStoragePermission()) {
            if (mRequestPermissionDialog != null && mRequestPermissionDialog.isShowing()) return;

            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE);
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
            if (mSearchKeyword == null) {
                if (getViewModel().getListFile() != null && getViewModel().getListFile().size() > 0) {

                } else {
                    showNoData();
                }
            } else {
                startGetData(false, false);
            }
        }
    }

    private void startGetData(boolean forceReload, boolean pullRefresh) {
        if (notHaveStoragePermission()) {
            showNoData();
        } else {
            if (pullRefresh) mIsResume = true;

            mSearchViewModel.setTypeSearch(0, mSearchKeyword);
            if (!mIsLoading) {
                if ((mIsResume && !pullRefresh) || forceReload) {
                    showLoadingArea();
                }

                mIsLoading = true;
                mSearchViewModel.startSeeding(mIsResume, pullRefresh);
            }
        }
    }

    @Override
    public void showNoData() {
        mActivitySearchBinding.pullRefresh.setRefreshing(false);
        mFileListAdapter.clearAllData();
        showNoDataArea();
    }

    private void showLoadingArea() {
        mActivitySearchBinding.loadingArea.setVisibility(View.VISIBLE);

        mActivitySearchBinding.dataListArea.setVisibility(View.GONE);
        mActivitySearchBinding.noDataArea.setVisibility(View.GONE);
    }

    private void showNoDataArea() {
        mActivitySearchBinding.noDataArea.setVisibility(View.VISIBLE);

        mActivitySearchBinding.dataListArea.setVisibility(View.GONE);
        mActivitySearchBinding.loadingArea.setVisibility(View.GONE);
    }

    private void updateData(List<FileData> dataList) {
        mIsLoading = false;
        mIsResume = false;

        mActivitySearchBinding.pullRefresh.setRefreshing(false);

        if (dataList.size() > 0) {
            mListFile = new ArrayList<>();
            mListFile.addAll(dataList);

            mFileListAdapter.setData(mListFile);
            showDataArea();
        } else {
            showNoData();
        }
    }

    private void showDataArea() {
        mActivitySearchBinding.dataListArea.setVisibility(View.VISIBLE);

        mActivitySearchBinding.noDataArea.setVisibility(View.GONE);
        mActivitySearchBinding.loadingArea.setVisibility(View.GONE);
    }

    @Override
    public void onClickItem(int position) {
        String filePath = mListFile.get(position).getFilePath();
        if (filePath == null) {
            filePath = RealPathUtil.getInstance().getRealPath(this, mListFile.get(position).getFileUri(), FileUtils.FileType.type_PDF);
        }
        try {
            if (mListFile.get(position).getFileType().equals(DataConstants.FILE_TYPE_PDF)) {
                String finalFilePath = filePath;
                showMyPdfAdsBeforeAction(() -> {
                    gotoPdfFileViewActivity(finalFilePath);
                });
            } else if (mListFile.get(position).getFileType().equals(DataConstants.FILE_TYPE_WORD)) {
                gotoViewByAndroidViewActivity(filePath, FileUtils.FileType.type_WORD);
            } else {
                gotoViewByAndroidViewActivity(filePath, FileUtils.FileType.type_EXCEL);
            }

            mFileListAdapter.setCurrentItem(position);
        } catch (Exception e) {
            ToastUtils.showMessageShort(this, getString(R.string.can_not_open_file));
        }
    }

    @Override
    public void onShareItem(int position) {
        FileUtils.shareFile(this, new File(mListFile.get(position).getFilePath()));
    }

    @Override
    public void onOptionItem(int position) {
        FileData fileData = mListFile.get(position);
        mSearchViewModel.startCheckIsFileBookmarked(fileData.getFilePath(), position, this::onShowOption);
    }

    private void onShowOption(int position, boolean isBookmarked) {
        FileData fileData = mListFile.get(position);

        if (PdfUtils.isPDFEncrypted(fileData.getFilePath())) {
            hideOptionDialog();
            pdfLockedOptionDialog = new PdfLockedOptionDialog(isBookmarked, fileData.getDisplayName(), fileData.getTimeAdded(), position, new PdfLockedOptionDialog.LockedFileOptionListener() {
                @Override
                public void openFile(int position) {
                    FirebaseUtils.sendEventFunctionUsed(getApplicationContext(), "Open file", "From search");
                    openPdfFile(position);
                }

                @Override
                public void shareFile(int position) {
                    onShareItem(position);
                }

                @Override
                public void uploadFile(int position) {
                    FileUtils.uploadFile(SearchActivity.this, new File(mListFile.get(position).getFilePath()));
                }

                @Override
                public void optionBookmark(int position, boolean isAdd) {
                    optionBookmarkPdf(mListFile.get(position).getFilePath(), isAdd);
                }

                @Override
                public void setPassword(int position) {
                    removePasswordPdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void renameFile(int position) {
                    renamePdfFile(position);
                }

                @Override
                public void deleteFile(int position) {
                    deletePdfFile(position);
                }
            });
            pdfLockedOptionDialog.show(getSupportFragmentManager(), pdfLockedOptionDialog.getTag());
            CommonUtils.hideKeyboard(this);
        } else {
            hideOptionDialog();
            pdfOptionDialog = new PdfOptionDialog(isBookmarked, fileData.getDisplayName(), fileData.getTimeAdded(), position, new PdfOptionDialog.FileOptionListener() {
                @Override
                public void openFile(int position) {
                    openPdfFile(position);
                }

                @Override
                public void shareFile(int position) {
                    onShareItem(position);
                }

                @Override
                public void printFile(int position) {
                    printPdfFile(mListFile.get(position).getFilePath());
                }

                @Override
                public void uploadFile(int position) {
                    FileUtils.uploadFile(SearchActivity.this, new File(mListFile.get(position).getFilePath()));
                }

                @Override
                public void optionBookmark(int position, boolean isAdd) {
                    optionBookmarkPdf(mListFile.get(position).getFilePath(), isAdd);
                }

                @Override
                public void setPassword(int position) {
                    hideOptionDialog();
                    setPasswordPdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void splitFile(int position) {
                    splitPdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void editFile(int position) {
                    editPdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void addWatermark(int position) {
                    addWatermarkPdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void extractToText(int position) {
                    extractToTextPdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void extractToImage(int position) {
                    extractToImagePdf(mListFile.get(position).getFilePath());
                }

                @Override
                public void renameFile(int position) {
                    renamePdfFile(position);
                }

                @Override
                public void deleteFile(int position) {
                    deletePdfFile(position);
                }
            });
            pdfOptionDialog.show(getSupportFragmentManager(), pdfOptionDialog.getTag());
            CommonUtils.hideKeyboard(this);
        }
    }
    private void hideOptionDialog() {
        if (pdfLockedOptionDialog != null && pdfLockedOptionDialog.isVisible()) {
            pdfLockedOptionDialog.dismiss();
        }
        if (pdfOptionDialog != null && pdfOptionDialog.isVisible()) {
            pdfOptionDialog.dismiss();
        }
    }

    private void openPdfFile(int position) {
        onClickItem(position);
    }

    private void renamePdfFile(int position) {
        FileData fileData = mListFile.get(position);
        String displayName;
        try {
            displayName = fileData.getDisplayName().substring(0, fileData.getDisplayName().lastIndexOf("."));
        } catch (Exception e) {
            return;
        }

        RenameFileDialog renameFileDialog = new RenameFileDialog(this, displayName, new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String name) {
                String newName = name + ".pdf";
                int result = FileUtils.renameFile(fileData, newName);

                if (result == -2 || result == 0) {
                    ToastUtils.showMessageShort(SearchActivity.this, getString(R.string.can_not_edit_video_name));
                } else if (result == -1) {
                    SnackBarUtils.getSnackbar(SearchActivity.this, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(SearchActivity.this, getString(R.string.rename_file_success)).show();
                    String oldFilePath = fileData.getFilePath();

                    fileData.setFilePath(fileData.getFilePath().replace(fileData.getDisplayName(), newName));
                    fileData.setDisplayName(newName);
                    mListFile.set(position, fileData);
                    mFileListAdapter.updateData(position, fileData);

                    mSearchViewModel.updateSavedData(oldFilePath, fileData.getFilePath());

                    startGetData(true, true);
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }

    private void deletePdfFile(int position) {
        FileData fileData = mListFile.get(position);

        ConfirmDialog confirmDialog = new ConfirmDialog(this, getString(R.string.confirm_delete_file_title), getString(R.string.confirm_delete_file_message), new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                if (!notHaveStoragePermission()) {
                    FileUtils.deleteFileOnExist(fileData.getFilePath());
                    mSearchViewModel.clearSavedData(fileData.getFilePath());

                    if (position >= 0 && position < mListFile.size()) {
                        mListFile.remove(position);
                        if (position == 0 && mListFile.size() > 1) {
                            Collections.swap(mListFile, 0, 1);
                        }
                    }

                    mFileListAdapter.clearData(position);
                    if (mListFile.size() <= 1) {
                        showNoDataArea();
                    }
                    SnackBarUtils.getSnackbar(SearchActivity.this, getString(R.string.delete_success_text)).show();

                    hideOptionDialog();

                    startGetData(true, true);
                }
            }

            @Override
            public void onCancel() {

            }
        });
        confirmDialog.show();
    }

    protected void extractToImagePdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(this, PdfToImageActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(this, "Pdf to image", "From function");
    }

    protected void extractToTextPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(this, PdfToTextActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(this, "Pdf to Text", "From function");
    }

    protected void addWatermarkPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(this, AddWaterMarkActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(this, "Add WaterMark PDF", "From function");
    }

    protected void editPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(this, EditPdfActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(this, "Edit PDF", "From function");
    }

    protected void splitPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(this, SplitPdfActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(this, "Split PDF", "From function");
    }

    protected void removePasswordPdf(String filePath) {
        gotoUnlockPasswordActivity(filePath, null);
    }

    protected void setPasswordPdf(String filePath) {
        gotoProtectPasswordActivity(filePath);
    }

    protected void optionBookmarkPdf(String filePath, boolean isAdd) {
        if (isAdd) {
            getViewModel().saveBookmarked(filePath);
            SnackBarUtils.getSnackbar(this, getString(R.string.bookmark_save_success)).show();
        } else {
            getViewModel().clearBookmarked(filePath);
            SnackBarUtils.getSnackbar(this, getString(R.string.bookmark_remove_success)).show();
        }
    }

    protected void printPdfFile(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            SnackBarUtils.getSnackbar(this, getString(R.string.view_pdf_can_not_print_protected_file)).show();
            return;
        }
        FileUtils.printFile(this, new File(filePath));
    }
}
