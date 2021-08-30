package com.pdfconverter.jpg2pdf.pdf.converter.ui.exceltopdf;

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
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityExcelToPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemFileActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;

import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingExcelToPdfDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.exceltopdf.done.ExcelToPdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListSelectAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ExcelToPdfActivity extends BaseBindingActivity<ActivityExcelToPdfBinding, ExcelToPdfViewModel> implements ExcelToPdfNavigator, OnItemFileActionListener, SettingExcelToPdfDialog.OnDialogSubmit, OnFileItemClickListener {
    private ExcelToPdfViewModel mExcelToPdfViewModel;
    private ActivityExcelToPdfBinding mActivityExcelToPdfBinding;
    private ArrayList<DocumentData> mSelectedList = new ArrayList<>();

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 3;

    private SweetAlertDialog mRequestPermissionDialog;
    private ExcelFileAdapter mExcelFileAdapter;
    private ExcelToPDFOptions mOptions;

    private List<FileData> mListFileSelector = new ArrayList<>();
    private FileListSelectAdapter mFileListSelectorAdapter;
    private ItemTouchHelper mItemTouchHelper;

    private boolean mIsFromOtherScreen = false;

    private String mFileSelectorSearchKey = "";

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_excel_to_pdf;
    }

    @Override
    public ExcelToPdfViewModel getViewModel() {
        mExcelToPdfViewModel = ViewModelProviders.of(this).get(ExcelToPdfViewModel.class);
        return mExcelToPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityExcelToPdfBinding = getViewDataBinding();
        mExcelToPdfViewModel.setNavigator(this);

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_EXCEL)) {
            mIsFromOtherScreen = true;
            String name = FileUtils.getFileName(extraFilePath);
            DocumentData selectedFile = new DocumentData(name, Uri.fromFile(new File(extraFilePath)), extraFilePath);
            mSelectedList = new ArrayList<>();
            mSelectedList.add(selectedFile);
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivityExcelToPdfBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.excel_to_pdf_title));
        mActivityExcelToPdfBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityExcelToPdfBinding.listLayout.toolbar.toolbarNameTv.setText(getString(R.string.excel_to_pdf_title));
        mActivityExcelToPdfBinding.listLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityExcelToPdfBinding.defaultLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        mActivityExcelToPdfBinding.defaultLayout.toolbar.toolbarNameOption.setOnClickListener(view -> showConverterPopup(2));
        mActivityExcelToPdfBinding.defaultLayout.toolbar.toolbarNameTv.setOnClickListener(view -> showConverterPopup(2));

        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        fetchOldOption();

        mActivityExcelToPdfBinding.defaultLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        mActivityExcelToPdfBinding.listLayout.layoutCreatePdfFile.setOnClickListener((v) -> this.createPdfFile());

        mActivityExcelToPdfBinding.listLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mExcelFileAdapter = new ExcelFileAdapter(this);
        mActivityExcelToPdfBinding.listLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActivityExcelToPdfBinding.listLayout.recyclerView.setAdapter(mExcelFileAdapter);
        ExcelFileTouchCallback callback = new ExcelFileTouchCallback(mExcelFileAdapter.getItemTouchListener());
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mActivityExcelToPdfBinding.listLayout.recyclerView);

        mActivityExcelToPdfBinding.defaultLayout.importFileBtnImport.setOnClickListener(view -> {
            if (mFileListSelectorAdapter.getNumberSelectedFile() == 0) {
                SnackBarUtils.getShortSnackbar(this, getString(R.string.excel_to_pdf_nothing_to_clear)).show();
            } else {
                CommonUtils.hideKeyboard(this);
                mActivityExcelToPdfBinding.defaultLayout.searchEdt.clearFocus();

                SweetAlertDialog checkingFileDialog = DialogFactory.getDialogProgress(this, "Checking file...");
                checkingFileDialog.show();

                AsyncTask.execute(() -> {
                    mSelectedList = new ArrayList<>();
                    List<FileData> selectedList = mFileListSelectorAdapter.getSelectedList();
                    for (int i = 0; i < selectedList.size(); i++) {
                        FileData fileData = selectedList.get(i);
                        String filePath = fileData.getFilePath();
                        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_EXCEL)) {
                            String fileName = FileUtils.getFileName(filePath);
                            mSelectedList.add(new DocumentData(fileName, fileData.getFileUri(), filePath));
                        }
                    }

                    runOnUiThread(() -> {
                        if (mSelectedList.size() == 0) {
                            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file_from_selector));
                        }

                        updateSelectedFile(true);

                        checkingFileDialog.dismiss();
                    });
                });
            }
        });

        setForLayoutView();

        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        } else {
            updateSelectedFile(true);
        }

        mActivityExcelToPdfBinding.defaultLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_FILE_REQUEST) {

            if (data == null) return;
            mSelectedList = new ArrayList<>();

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
                        String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_EXCEL);

                        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_EXCEL)) {
                            String fileName = FileUtils.getFileName(this, uri);
                            mSelectedList.add(new DocumentData(fileName, uri, filePath));
                        }
                    }

                    runOnUiThread(() -> {
                        mLoadFromLocalDialog.dismiss();

                        if (mSelectedList.size() == 0) {
                            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
                        }

                        updateSelectedFile(true);
                    });

                    if (googleDriveUri.size() > 0)
                        startDownloadFromGoogleDriveList(googleDriveUri);
                });

            } else if (data.getData() != null) {
                Uri uri = data.getData();

                String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_EXCEL);

                if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_EXCEL)) {
                    String fileName = FileUtils.getFileName(this, uri);
                    mSelectedList.add(new DocumentData(fileName, uri, filePath));
                }

                if (RealPathUtil.getInstance().isDriveFile(uri)) {
                    startDownloadFromGoogleDrive(uri);
                    return;
                }

                if (mSelectedList.size() == 0) {
                    ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
                }

                updateSelectedFile(true);
            }
        } else if (requestCode == CREATE_PDF_FROM_SELECT_FILE) {
            mOptions = new ExcelToPDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.EXCEL_TO_PDF_PREFIX_NAME), false, "", OptionConstants.DEFAULT_PAGE_SIZE_EXCEL, OptionConstants.DEFAULT_PAGE_ORIENTATION, false);
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

        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_EXCEL)) {
            String fileName = FileUtils.getFileName(this, uri);
            mSelectedList.add(new DocumentData(fileName, uri, filePath));

            updateSelectedFile(true);
        }
    }

    @Override
    protected void updateFilePathFromGGDriveList(int index, ArrayList<Uri> uriList, String filePath) {
        super.updateFilePathFromGGDriveList(index, uriList, filePath);
        Uri uri = uriList.get(index);

        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_EXCEL)) {
            String fileName = FileUtils.getFileName(this, uri);
            mSelectedList.add(new DocumentData(fileName, uri, filePath));
        }

        updateSelectedFile(true);
    }

    @Override
    public void onBackPressed() {
        if (!mIsFromOtherScreen && mSelectedList.size() > 0) {
            mSelectedList = new ArrayList<>();

            mExcelFileAdapter.removeAllData();

            resetNameAndPassword();
            setForLayoutView();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateNumberSelected() {
        int numberSelected = (mFileListSelectorAdapter == null ? 0 : mFileListSelectorAdapter.getNumberSelectedFile());
        mActivityExcelToPdfBinding.defaultLayout.importFileBtnImport.setText(getString(R.string.import_file) + " (" + numberSelected + ")");
    }

    private void fetchOldOption() {
        ExcelToPDFOptions excelToPDFOptions = DataManager.getInstance(this).getExcelToPDFOptions();
        if (excelToPDFOptions != null) {
            mOptions = excelToPDFOptions;
            resetNameAndPassword();
        } else {
            mOptions = new ExcelToPDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.EXCEL_TO_PDF_PREFIX_NAME), false, "", OptionConstants.DEFAULT_PAGE_SIZE_EXCEL, OptionConstants.DEFAULT_PAGE_ORIENTATION, false);
        }
    }

    private void resetNameAndPassword() {
        if (mOptions != null) {
            mOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.EXCEL_TO_PDF_PREFIX_NAME));
            mOptions.setPassword("");
            mOptions.setPasswordProtected(false);
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "*/*");
        String[] mimeTypes = {"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.excel_to_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedList.size() > 0) {
            mActivityExcelToPdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            mActivityExcelToPdfBinding.listLayout.contentView.setVisibility(View.VISIBLE);
        } else {
            mActivityExcelToPdfBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivityExcelToPdfBinding.listLayout.contentView.setVisibility(View.GONE);
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
            mActivityExcelToPdfBinding.defaultLayout.importFileBtnImport.setVisibility(View.GONE);
        } else {
            mFileListSelectorAdapter = new FileListSelectAdapter(this);
            mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
            mActivityExcelToPdfBinding.defaultLayout.importFileBtnImport.setVisibility(View.VISIBLE);
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
        mExcelToPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mExcelToPdfViewModel.getFileList(DataConstants.FILE_TYPE_EXCEL, FileUtils.SORT_BY_DATE_DESC);
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
                if (mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            updateNumberSelected();
            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_excel_found);

        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {

        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityExcelToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

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
                mExcelFileAdapter.setFileData(mSelectedList);
                mExcelFileAdapter.notifyDataSetChanged();
                setForLayoutView();
            }
        } else {
            mExcelFileAdapter.setFileData(mSelectedList);
            mExcelFileAdapter.notifyDataSetChanged();
            setForLayoutView();
        }
    }

    private void createPdfFile() {
        if (mOptions != null && mOptions.getOutFileName() != null && mOptions.getOutFileName().startsWith(DataConstants.EXCEL_TO_PDF_PREFIX_NAME)) {
            mOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.EXCEL_TO_PDF_PREFIX_NAME));
        } else if (mOptions == null) {
            mOptions = new ExcelToPDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.EXCEL_TO_PDF_PREFIX_NAME),
                    false, "", OptionConstants.DEFAULT_PAGE_SIZE_EXCEL, OptionConstants.DEFAULT_PAGE_ORIENTATION, false);
        }
        SettingExcelToPdfDialog dialog = new SettingExcelToPdfDialog(mOptions, this);
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }

    @Override
    public void onClick(int position) {
        // TODO view excel file
    }

    @Override
    public void onDelete(int position) {
        if (mIsFromOtherScreen) {
            finish();
            return;
        }

        if (position < mSelectedList.size()) {
            mSelectedList.remove(position);
            mExcelFileAdapter.removeData(position);

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
    public void submitForm(final ExcelToPDFOptions options) {
        checkIAPDoneBeforeAction(() -> {
            this.mOptions = options;

            if (mOptions == null) {
                mOptions = new ExcelToPDFOptions(mSelectedList, FileUtils.getDefaultFileName(DataConstants.EXCEL_TO_PDF_PREFIX_NAME), false, "", OptionConstants.DEFAULT_PAGE_SIZE_EXCEL, OptionConstants.DEFAULT_PAGE_ORIENTATION, false);
            } else {
                mOptions.setPathList(mSelectedList);
            }

            Gson gson = new Gson();
            String json = gson.toJson(mOptions);
            Intent intent = new Intent(this, ExcelToPdfDoneActivity.class);
            intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
            startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
        });
    }
}
