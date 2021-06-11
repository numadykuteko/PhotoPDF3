package com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf;

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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ads.control.Admod;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityEditPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnEditPageItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.common.CommonDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.DeleteByRangeDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.EditPageOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SwapPageDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf.EditPdfListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf.EditPdfManager;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf.FindDuplicateListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf.FindDuplicateManager;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf.PdfToBitmapListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf.PdfToBitmapManager;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditPdfActivity extends BaseBindingActivity<ActivityEditPdfBinding, EditPdfViewModel> implements EditPdfNavigator, OnEditPageItemClickListener, OnFileItemClickListener {
    private EditPdfViewModel mEditPdfViewModel;
    private ActivityEditPdfBinding mActivityEditPdfBinding;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;

    private SweetAlertDialog mRequestPermissionDialog;

    private ArrayList<PageData> mInputList = new ArrayList<>();
    private ArrayList<PageData> mOutputList = new ArrayList<>();
    private PageDataAdapter mPageAdapter;

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
        return R.layout.activity_edit_pdf;
    }

    @Override
    public EditPdfViewModel getViewModel() {
        mEditPdfViewModel = ViewModelProviders.of(this).get(EditPdfViewModel.class);
        return mEditPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityEditPdfBinding = getViewDataBinding();
        mEditPdfViewModel.setNavigator(this);

        mIsFromOtherScreen = false;

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_PDF)) {
            mIsFromOtherScreen = true;
            mFilePath = extraFilePath;
            String name = FileUtils.getFileName(extraFilePath);
            mSelectedFile = new DocumentData(name, null, mFilePath);
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivityEditPdfBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.edit_pdf_title));
        mActivityEditPdfBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityEditPdfBinding.listLayout.toolbar.toolbarNameTv.setText(getString(R.string.edit_pdf_title));
        mActivityEditPdfBinding.listLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mActivityEditPdfBinding.listLayout.toolbar.toolbarActionFullScreen.setVisibility(View.GONE);

        ImageView closeBtn = mActivityEditPdfBinding.defaultLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mActivityEditPdfBinding.defaultLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        mActivityEditPdfBinding.listLayout.saveAs.setOnClickListener((v) -> this.savePdfFile());
        mActivityEditPdfBinding.listLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mActivityEditPdfBinding.listLayout.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mPageAdapter = new PageDataAdapter(this);
        mActivityEditPdfBinding.listLayout.recyclerView.setAdapter(mPageAdapter);

        setForLayoutView();

        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        } else {
            checkFilePathGet(Uri.fromFile(new File(mFilePath)), mFilePath);
        }

        mActivityEditPdfBinding.listLayout.toolbar.toolbarActionMore.setOnClickListener((View.OnClickListener) view -> {
            EditPageOptionDialog editPageOptionDialog = new EditPageOptionDialog(new EditPageOptionDialog.EditPageOptionListener() {
                @Override
                public void swapPage() {
                    if (mFilePath == null) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_choose_file_first));
                        return;
                    }
                    fastSwap();
                }

                @Override
                public void deletePage() {
                    if (mFilePath == null) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_choose_file_first));
                        return;
                    }
                    deleteFromToPages();
                }

                @Override
                public void removeDuplicate() {
                    if (mFilePath == null) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_choose_file_first));
                        return;
                    }
                    removeAllDuplicatePage();
                }

                @Override
                public void resetAllChange() {
                    if (mFilePath == null) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_choose_file_first));
                        return;
                    }
                    resetAllChanges();
                }
            });

            editPageOptionDialog.show(getSupportFragmentManager(), editPageOptionDialog.getTag());
        });

        mActivityEditPdfBinding.defaultLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

            //Getting Absolute Path
            final String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_PDF);
            checkFilePathGet(uri, filePath);
        } else if (requestCode == CREATE_PDF_FROM_SELECT_FILE) {
            if (resultCode == RESULT_NEED_FINISH) {
                finish();
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
                SnackBarUtils.getSnackbar(this, getString(R.string.edit_pdf_file_encrypted))
                        .setAction(R.string.remove_password_now, v -> gotoUnlockPasswordActivity(filePath, null)).show();
                return;
            }
            int numberPages = FileUtils.getNumberPages(filePath);
            if (numberPages == 0) {
                ToastUtils.showMessageLong(this, getString(R.string.edit_pdf_file_empty));
                return;
            }

            // Reading pdf file
            SweetAlertDialog loadingDialog = DialogFactory.getDialogProgress(this, getString(R.string.edit_pdf_loading_pages));
            loadingDialog.show();

            PdfToBitmapManager pdfToBitmapManager = new PdfToBitmapManager(uri, filePath, this, new PdfToBitmapListener() {
                @Override
                public void onReadSuccess(ArrayList<PageData> pageDataList) {
                    runOnUiThread(() -> {
                        if (pageDataList != null && pageDataList.size() > 0) {
                            mInputList.clear();
                            mInputList = new ArrayList<>();
                            mInputList.addAll(pageDataList);

                            mOutputList.clear();
                            mOutputList = new ArrayList<>();
                            mOutputList.addAll(pageDataList);

                            mPageAdapter.setPageData(mOutputList);
                            mActivityEditPdfBinding.listLayout.recyclerView.scrollToPosition(0);

                            mFilePath = filePath;
                            String name = FileUtils.getFileName(getApplicationContext(), uri);
                            mSelectedFile = new DocumentData(name, uri, mFilePath);
                            mNumberPage = numberPages;

                            setForLayoutView();

                            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.edit_pdf_load_page_finish));
                        } else {
                            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.edit_pdf_can_not_load_page));
                        }
                        try {
                            if (loadingDialog.isShowing()) {
                                loadingDialog.dismiss();
                            }
                        } catch (Exception e) {
                            // donothing
                        }

                    });
                }

                @Override
                public void onReadFail() {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.edit_pdf_can_not_load_page));
                    });
                }

                @Override
                public void onReadStart() {
                    runOnUiThread(loadingDialog::show);
                }
            });
            pdfToBitmapManager.execute();
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
    }

    @Override
    public void onBackPressed() {
        if (!mIsFromOtherScreen && (mFilePath != null || mSelectedFile != null)) {

            // TODO check any changes before back

            mInputList.clear();
            mInputList = new ArrayList<>();

            mOutputList.clear();
            mOutputList = new ArrayList<>();
            mPageAdapter.removeAllData();

            mSelectedFile = null;
            mFilePath = null;

            setForLayoutView();
            startRequestForFileList(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mInputList.clear();
        mOutputList.clear();
        super.onDestroy();
    }

    private void startChooseFileActivity() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, getString(R.string.pdf_type));
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.edit_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedFile != null && mFilePath != null) {
            mActivityEditPdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            mActivityEditPdfBinding.listLayout.contentView.setVisibility(View.VISIBLE);

            if (mOutputList.size() > 0) {
                mActivityEditPdfBinding.listLayout.noPage.setVisibility(View.GONE);
            } else {
                mActivityEditPdfBinding.listLayout.noPage.setVisibility(View.VISIBLE);
            }
        } else {
            mActivityEditPdfBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivityEditPdfBinding.listLayout.contentView.setVisibility(View.GONE);
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityEditPdfBinding.defaultLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mEditPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mEditPdfViewModel.getUnlockedFileList();
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
                if (mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityEditPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

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

    @SuppressLint("SetTextI18n")
    private void resetAllChanges() {
        if (mInputList.equals(mOutputList)) {
            ToastUtils.showMessageShort(this, getString(R.string.edit_pdf_nothing_changes));
            return;
        }

        mOutputList = new ArrayList<>();
        mOutputList.addAll(mInputList);

        setForLayoutView();

        mPageAdapter.setPageData(mOutputList);
        mActivityEditPdfBinding.listLayout.recyclerView.scrollToPosition(0);

        SnackBarUtils.getSnackbar(EditPdfActivity.this, getString(R.string.edit_pdf_reset_change_success)).show();
    }

    private void removeAllDuplicatePage() {
        if (mOutputList.size() == 1) {
            ToastUtils.showMessageShort(this, getString(R.string.edit_pdf_file_only_have_one_page));
            return;
        }
        SweetAlertDialog findingDuplicate = DialogFactory.getDialogProgress(this, getString(R.string.edit_pdf_finding_duplicate));
        FindDuplicateManager findDuplicateManager = new FindDuplicateManager(mOutputList, new FindDuplicateListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFindSuccess(ArrayList<String> listDuplicate) {
                runOnUiThread(() -> {
                    findingDuplicate.dismiss();

                    if (listDuplicate.size() == 0) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_no_duplicate_found));
                        return;
                    }

                    StringBuilder resultNotice = new StringBuilder();
                    ArrayList<PageData> listToRemove = new ArrayList<>();

                    for (String duplicateFound : listDuplicate) {
                        if (duplicateFound != null && duplicateFound.length() > 0) {
                            String[] result = duplicateFound.split(",");
                            if (result.length >= 2) {
                                try {
                                    int from = Integer.parseInt(result[0]);
                                    int to = Integer.parseInt(result[1]);

                                    if (from >= 0 && from < mOutputList.size()) {
                                        resultNotice.append(from + 1).append("(").append(to + 1).append(")  ");
                                        listToRemove.add(mOutputList.get(from));
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }

                    if (listToRemove.size() > 0) {
                        mOutputList.removeAll(listToRemove);
                        mPageAdapter.setPageData(mOutputList);

                        setForLayoutView();
                    }

                    if (resultNotice.length() > 0) {
                        SnackBarUtils.getSnackbar(EditPdfActivity.this, getString(R.string.edit_pdf_duplicate_found_delete) + " " + resultNotice.toString()).show();
                    } else {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_no_duplicate_found));
                    }
                });
            }

            @Override
            public void onFindStart() {
                runOnUiThread(findingDuplicate::show);
            }
        });

        findDuplicateManager.execute();
    }

    @SuppressLint("SetTextI18n")
    private void deleteFromToPages() {
        if (mOutputList.size() == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.edit_pdf_no_page_to_delete));
            return;
        }

        DeleteByRangeDialog deleteByRangeDialog = new DeleteByRangeDialog(this, mNumberPage, new DeleteByRangeDialog.DeleteRangeListener() {
            @Override
            public void onSubmitRange(int start, int end) {
                int toPage = end;
                try {

                    if (start <= 0 || toPage <= 0 || start > toPage) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_delete_from_to_pages_error));
                    } else {
                        if (toPage > mOutputList.size()) {
                            toPage = mOutputList.size();
                        }

                        for (int i = start - 1; i <= toPage - 1; i++) {
                            mOutputList.remove(start - 1);
                        }

                        setForLayoutView();
                        mPageAdapter.setPageData(mOutputList);

                        SnackBarUtils.getSnackbar(EditPdfActivity.this, getString(R.string.edit_pdf_delete_from_to_pages_done) + " " + start + " to page " + toPage).show();
                    }

                } catch (Exception e) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_delete_from_to_pages_error));
                }
            }

            @Override
            public void onCancel() {

            }
        });
        deleteByRangeDialog.show();
    }

    private void fastSwap() {
        if (mOutputList.size() <= 1) {
            ToastUtils.showMessageShort(this, getString(R.string.edit_pdf_no_page_to_swap));
            return;
        }

        SwapPageDialog swapPageDialog = new SwapPageDialog(this, mNumberPage, new SwapPageDialog.SwapPageListener() {
            @Override
            public void onSubmitPages(int start, int end) {
                try {
                    if (start <= 0 || end <= 0 || start > mOutputList.size() || end > mOutputList.size()) {
                        ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_fast_swap_error));
                    } else {
                        Collections.swap(mOutputList, start - 1, end - 1);
                        mPageAdapter.swapData(start - 1, end - 1);
                        SnackBarUtils.getSnackbar(EditPdfActivity.this, getString(R.string.edit_pdf_fast_swap_done) + " " + start + " and page " + end).show();
                    }

                } catch (Exception e) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.edit_pdf_fast_swap_error));
                }
            }

            @Override
            public void onCancel() {

            }
        });
        swapPageDialog.show();
    }

    private void savePdfFile() {
        if (mInputList.equals(mOutputList)) {
            ToastUtils.showMessageShort(this, getString(R.string.edit_pdf_nothing_changes));
            return;
        }

        if (mOutputList.size() == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.edit_pdf_nothing_to_save));
            return;
        }

        String suggestFileName = FileUtils.getLastReplacePath(FileUtils.getFileName(mFilePath), DataConstants.PDF_EXTENSION, "_" +
                DateTimeUtils.currentTimeToNaming() + getString(R.string.edit_pdf_edited_file_name));
        String displayName = suggestFileName;
        try {
            displayName = suggestFileName.substring(0, suggestFileName.lastIndexOf("."));
        } catch (Exception ignored) {
        }

        RenameFileDialog renameFileDialog = new RenameFileDialog(this, displayName, new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String newName) {
                runOnUiThread(() -> {
                    String fullName = newName + ".pdf";
                    String fullPath = FileUtils.getLastReplacePath(mFilePath, FileUtils.getFileName(mFilePath), fullName);

                    startSavingFile(fullPath);
                });
            }

            @Override
            public void onCancel() {

            }
        });
        renameFileDialog.show();
    }

    private void startSavingFile(String newName) {
        SweetAlertDialog savingDialog = DialogFactory.getDialogProgress(this, getString(R.string.saving_text));
        EditPdfManager editPdfManager = new EditPdfManager(mFilePath, getApplicationContext(), true, newName, mOutputList, new EditPdfListener() {
            @Override
            public void onEditSuccess(String outputPath) {
                runOnUiThread(() -> {
                    savingDialog.dismiss();
                    getViewModel().saveRecent(outputPath, getString(R.string.edit_pdf));

                    Intent intent = new Intent(EditPdfActivity.this, CommonDoneActivity.class);
                    intent.putExtra(EXTRA_FILE_PATH, outputPath);
                    intent.putExtra(EXTRA_FILE_TYPE, DataConstants.FILE_TYPE_PDF);
                    intent.putExtra(EXTRA_FILE_EXTENSION, ".pdf");
                    startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
                });

            }

            @Override
            public void onEditFail() {
                runOnUiThread(() -> {
                    savingDialog.dismiss();
                    ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.edit_pdf_save_error));
                });
            }

            @Override
            public void onEditStart() {
            }
        });
        editPdfManager.execute();
        savingDialog.show();
    }

    @Override
    public void onSwap(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(mOutputList, i, i + 1);
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(mOutputList, i, i - 1);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDeleteItem(int position) {
        if (position >= 0 && position < mOutputList.size()) {
            mOutputList.remove(position);
            mPageAdapter.removeData(position);

            setForLayoutView();
        }
    }
}
