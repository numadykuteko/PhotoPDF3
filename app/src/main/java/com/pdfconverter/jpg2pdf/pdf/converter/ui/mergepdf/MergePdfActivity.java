package com.pdfconverter.jpg2pdf.pdf.converter.ui.mergepdf;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.MergePDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityMergePdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemFileActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingMergePdfDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.mergepdf.done.MergePdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListSelectAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MergePdfActivity extends BaseBindingActivity<ActivityMergePdfBinding, MergePdfViewModel> implements MergePdfNavigator, OnItemFileActionListener, OnFileItemClickListener, SettingMergePdfDialog.OnDialogSubmit {
    private MergePdfViewModel mMergePdfViewModel;
    private ActivityMergePdfBinding mActivityMergePdfBinding;
    private ArrayList<DocumentData> mSelectedList = new ArrayList<>();

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_MORE_FILE = 2;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 3;

    private SweetAlertDialog mRequestPermissionDialog;
    private MergeFileAdapter mMergeFileAdapter;

    private int mNumberSuccess = 0;
    private int mNumberFail = 0;
    private int mNumberEncrypted = 0;

    private MergePDFOptions mOptions;

    private List<FileData> mListFileSelector = new ArrayList<>();
    private FileListSelectAdapter mFileListSelectorAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private String mFileSelectorSearchKey = "";

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_merge_pdf;
    }

    @Override
    public MergePdfViewModel getViewModel() {
        mMergePdfViewModel = ViewModelProviders.of(this).get(MergePdfViewModel.class);
        return mMergePdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityMergePdfBinding = getViewDataBinding();
        mMergePdfViewModel.setNavigator(this);

        initView();
    }

    @Override
    protected void initView() {
        mActivityMergePdfBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.merge_pdf_title));
        mActivityMergePdfBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityMergePdfBinding.listLayout.toolbar.toolbarNameTv.setText(getString(R.string.merge_pdf_title));
        mActivityMergePdfBinding.listLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityMergePdfBinding.defaultLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mActivityMergePdfBinding.defaultLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        mActivityMergePdfBinding.listLayout.layoutCreatePdfFile.setOnClickListener((v) -> this.createPdfFile());

        mActivityMergePdfBinding.listLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mMergeFileAdapter = new MergeFileAdapter(this);
        mActivityMergePdfBinding.listLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActivityMergePdfBinding.listLayout.recyclerView.setAdapter(mMergeFileAdapter);
        MergeFileTouchCallback callback = new MergeFileTouchCallback(mMergeFileAdapter.getItemTouchListener());
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mActivityMergePdfBinding.listLayout.recyclerView);

        mActivityMergePdfBinding.defaultLayout.importFileBtnImport.setOnClickListener(view -> {
            if (mFileListSelectorAdapter.getNumberSelectedFile() == 0) {
                SnackBarUtils.getShortSnackbar(this, getString(R.string.merge_pdf_nothing_to_clear)).show();
            } else {
                CommonUtils.hideKeyboard(this);
                mActivityMergePdfBinding.defaultLayout.searchEdt.clearFocus();

                SweetAlertDialog checkingFileDialog = DialogFactory.getDialogProgress(this, "Checking file...");
                checkingFileDialog.show();

                AsyncTask.execute(() -> {
                    mNumberSuccess = 0;
                    mNumberEncrypted = 0;
                    mNumberFail = 0;

                    mSelectedList = new ArrayList<>();
                    List<FileData> selectedList = mFileListSelectorAdapter.getSelectedList();
                    for (int i = 0; i < selectedList.size(); i++) {
                        FileData fileData = selectedList.get(i);
                        String filePath = fileData.getFilePath();
                        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
                            if (!PdfUtils.isPDFEncrypted(filePath)) {
                                String fileName = FileUtils.getFileName(filePath);
                                mSelectedList.add(new DocumentData(fileName, fileData.getFileUri(), filePath));
                                mNumberSuccess++;
                            } else {
                                mNumberEncrypted++;
                            }
                        } else {
                            mNumberFail++;
                        }
                    }

                    runOnUiThread(() -> {
                        updateSelectedFile(true);

                        noticeGetFileResult();

                        if (mSelectedList.size() == 0) {
                            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file_from_selector));
                        }

                        checkingFileDialog.dismiss();
                    });
                });
            }
        });

        setForLayoutView();

        requestForFileSelector();

        mActivityMergePdfBinding.defaultLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        updateNumberSelected();
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

            case REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_MORE_FILE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        startAddFileActivity();
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
        if (requestCode == TAKE_FILE_REQUEST || requestCode == ADD_FILE_REQUEST) {

            if (data == null) return;

            if (requestCode == TAKE_FILE_REQUEST || mSelectedList == null) {
                mSelectedList = new ArrayList<>();
            }

            mNumberSuccess = 0;
            mNumberEncrypted = 0;
            mNumberFail = 0;

            if (data.getClipData() != null) {
                int numberFile = data.getClipData().getItemCount();
                if (numberFile == 0) return;

                ArrayList<Uri> localUri = new ArrayList<>();
                ArrayList<Uri> googleDriveUri = new ArrayList<>();

                for (int i = 0; i < numberFile; i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();

                    if (RealPathUtil.getInstance().isDriveFile(fileUri)) {
                        googleDriveUri.add(fileUri);
                    } else {
                        localUri.add(fileUri);
                    }
                }

                mLoadFromLocalDialog = DialogFactory.getDialogProgress(this, getString(R.string.loading_from_local_text));
                mLoadFromLocalDialog.show();

                AsyncTask.execute(() -> {
                    for (Uri uri : localUri) {
                        String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_PDF);

                        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
                            if (!PdfUtils.isPDFEncrypted(filePath)) {
                                String fileName = FileUtils.getFileName(this, uri);
                                mSelectedList.add(new DocumentData(fileName, uri, filePath));
                                mNumberSuccess++;
                            } else {
                                mNumberEncrypted++;
                            }
                        } else {
                            mNumberFail++;
                        }
                    }

                    runOnUiThread(() -> {
                        mLoadFromLocalDialog.dismiss();

                        if (mSelectedList.size() == 0) {
                            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
                        }

                        updateSelectedFile(true);
                    });

                    if (googleDriveUri.size() > 0) {
                        startDownloadFromGoogleDriveList(googleDriveUri);
                    } else {
                        noticeGetFileResult();
                    }
                });

            } else if (data.getData() != null) {
                Uri uri = data.getData();

                if (RealPathUtil.getInstance().isDriveFile(uri)) {
                    startDownloadFromGoogleDrive(uri);
                    return;
                }

                String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_PDF);

                if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
                    if (!PdfUtils.isPDFEncrypted(filePath)) {
                        String fileName = FileUtils.getFileName(this, uri);
                        mSelectedList.add(new DocumentData(fileName, uri, filePath));
                        mNumberSuccess++;
                    } else {
                        mNumberEncrypted++;
                    }
                } else {
                    mNumberFail++;
                }

                noticeGetFileResult();

                if (mSelectedList.size() == 0) {
                    ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
                }

                updateSelectedFile(true);
            }

        } else if (requestCode == CREATE_PDF_FROM_SELECT_FILE) {
            mOptions = new MergePDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.MERGE_FILE_PREFIX_NAME), false, "");
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

        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
            if (!PdfUtils.isPDFEncrypted(filePath)) {
                String fileName = FileUtils.getFileName(this, uri);
                mSelectedList.add(new DocumentData(fileName, uri, filePath));
                mNumberSuccess++;
            } else {
                mNumberEncrypted++;
            }

            updateSelectedFile(true);
        } else {
            mNumberFail++;
        }

        noticeGetFileResult();
    }

    @Override
    protected void updateFilePathFromGGDriveList(int index, ArrayList<Uri> uriList, String filePath) {
        super.updateFilePathFromGGDriveList(index, uriList, filePath);
        Uri uri = uriList.get(index);

        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
            if (!PdfUtils.isPDFEncrypted(filePath)) {
                String fileName = FileUtils.getFileName(this, uri);
                mSelectedList.add(new DocumentData(fileName, uri, filePath));
                mNumberSuccess++;
            } else {
                mNumberEncrypted++;
            }
        } else {
            mNumberFail++;
        }

        if (index == uriList.size() - 1) {
            noticeGetFileResult();
        }
        updateSelectedFile(true);
    }

    @SuppressLint("SetTextI18n")
    private void updateNumberSelected() {
        int numberSelected = (mFileListSelectorAdapter == null ? 0 : mFileListSelectorAdapter.getNumberSelectedFile());
        mActivityMergePdfBinding.defaultLayout.importFileBtnImport.setText(getString(R.string.import_file) + " (" + numberSelected + ")");
    }

    private void noticeGetFileResult() {
        StringBuilder messBuilder = new StringBuilder();
        messBuilder.append("Load success: ").append(mNumberSuccess).append(" file(s). ");

        if (mNumberEncrypted > 0) {
            messBuilder.append("Encrypted file: ").append(mNumberEncrypted).append(" file(s). ");
        }
        if (mNumberFail > 0) {
            messBuilder.append("Failed file: ").append(mNumberFail).append(" file(s). ");
        }

        SnackBarUtils.getSnackbar(this, messBuilder.toString()).show();
    }

    @Override
    public void onBackPressed() {
        if (mSelectedList.size() > 0) {
            mSelectedList = new ArrayList<>();

            mMergeFileAdapter.removeAllData();

            mOptions = new MergePDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.MERGE_FILE_PREFIX_NAME), false, "");

            setForLayoutView();
        } else {
            super.onBackPressed();
        }
    }

    private void startChooseFileActivity() {
        if (DataManager.getInstance(this).getShowGuideSelectMulti()) {
            DataManager.getInstance(this).setShowGuideSelectMultiDone();

            SweetAlertDialog dialogNotice = DialogFactory.getDialogNotice(this, getString(R.string.guide_to_select_multiple));
            dialogNotice.setCanceledOnTouchOutside(false);
            dialogNotice.setCancelable(false);
            dialogNotice.setConfirmClickListener(sweetAlertDialog -> {
                dialogNotice.dismiss();
                gotoFileChooser();
            });
            dialogNotice.show();

        } else {
            gotoFileChooser();
        }
    }

    private void gotoFileChooser() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, getString(R.string.pdf_type));
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.merge_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedList.size() > 0) {
            mActivityMergePdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            mActivityMergePdfBinding.listLayout.contentView.setVisibility(View.VISIBLE);
        } else {

            mActivityMergePdfBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivityMergePdfBinding.listLayout.contentView.setVisibility(View.GONE);
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
            mActivityMergePdfBinding.defaultLayout.importFileBtnImport.setVisibility(View.GONE);
        } else {
            mFileListSelectorAdapter = new FileListSelectAdapter(this);
            mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
            mActivityMergePdfBinding.defaultLayout.importFileBtnImport.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClickItem(int position) {
        mFileListSelectorAdapter.revertData(position);
        updateNumberSelected();
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mMergePdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mMergePdfViewModel.getUnlockedFileList();
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
                if (mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            updateNumberSelected();
            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {

        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
    }

    private void updateForSearchFileSelector() {
        if (mListFileSelector.size() > 0) {
            if (mFileSelectorSearchKey.length() > 0) {
                List<FileData> searchList = new ArrayList<>();
                for (FileData fileData : mListFileSelector) {
                    String fileName = FileUtils.getFileName(fileData.getFilePath());
                    if (fileName.toLowerCase().contains(mFileSelectorSearchKey.toLowerCase())) {
                        searchList.add(fileData);
                    }
                }
                mFileListSelectorAdapter.setData(searchList);
                mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityMergePdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
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

    private void updateSelectedFile(boolean isFromAdd) {
        if (isFromAdd) {
            if (mSelectedList.size() > 0) {
                mMergeFileAdapter.setFileData(mSelectedList);
                mMergeFileAdapter.notifyDataSetChanged();
                setForLayoutView();
            }
        } else {
            mMergeFileAdapter.setFileData(mSelectedList);
            mMergeFileAdapter.notifyDataSetChanged();
            setForLayoutView();
        }
    }

    private void createPdfFile() {
        if (mOptions != null && mOptions.getOutFileName() != null && mOptions.getOutFileName().startsWith(DataConstants.MERGE_FILE_PREFIX_NAME)) {
            mOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.MERGE_FILE_PREFIX_NAME));
        } else if (mOptions == null) {
            mOptions = new MergePDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.MERGE_FILE_PREFIX_NAME), false, "");
        }
        SettingMergePdfDialog dialog = new SettingMergePdfDialog(mOptions, this);
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }

    private void startAddFileActivity() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, getString(R.string.pdf_type));
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.merge_pdf_select_file_title)), ADD_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @Override
    public void onClick(int position) {
        gotoPdfFileViewActivity(mSelectedList.get(position).getFilePath());
    }

    @Override
    public void onDelete(int position) {
        if (position < mSelectedList.size()) {
            mSelectedList.remove(position);
            mMergeFileAdapter.removeData(position);

            updateSelectedFile(false);
        }
    }

    @Override
    public void onClickSwap(RecyclerView.ViewHolder viewHolder) {
        if (mItemTouchHelper != null) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    }

    @Override
    public void onSwap(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(mSelectedList, i, i + 1);
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(mSelectedList, i, i - 1);
            }
        }
    }

    @Override
    public void submitForm(MergePDFOptions options) {
        showDoneAdsBeforeAction(() -> {
            this.mOptions = options;

            if (mOptions == null) {
                mOptions = new MergePDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.MERGE_FILE_PREFIX_NAME), false, "");
            } else {
                mOptions.setPathList(mSelectedList);
            }

            Gson gson = new Gson();
            String json = gson.toJson(mOptions);
            Intent intent = new Intent(this, MergePdfDoneActivity.class);
            intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
            startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
        });
    }
}
