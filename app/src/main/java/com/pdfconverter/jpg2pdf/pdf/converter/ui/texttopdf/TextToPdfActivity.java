package com.pdfconverter.jpg2pdf.pdf.converter.ui.texttopdf;

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
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityTextToPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemFileActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingTextToPdfDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.texttopdf.done.TextToPdfDoneActivity;
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

public class TextToPdfActivity extends BaseBindingActivity<ActivityTextToPdfBinding, TextToPdfViewModel> implements TextToPdfNavigator, OnItemFileActionListener, SettingTextToPdfDialog.OnDialogSubmit, OnFileItemClickListener {
    private TextToPdfViewModel mTextToPdfViewModel;
    private ActivityTextToPdfBinding mActivityTextToPdfBinding;
    private ArrayList<DocumentData> mSelectedList = new ArrayList<>();

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 3;

    private TextToPDFOptions mOptions;

    private SweetAlertDialog mRequestPermissionDialog;
    private TextFileAdapter mTextFileAdapter;

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
        return R.layout.activity_text_to_pdf;
    }

    @Override
    public TextToPdfViewModel getViewModel() {
        mTextToPdfViewModel = ViewModelProviders.of(this).get(TextToPdfViewModel.class);
        return mTextToPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityTextToPdfBinding = getViewDataBinding();
        mTextToPdfViewModel.setNavigator(this);

        mIsFromOtherScreen = false;

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_WORD)) {
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
        mActivityTextToPdfBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.text_to_pdf_title));
        mActivityTextToPdfBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityTextToPdfBinding.listLayout.toolbar.toolbarNameTv.setText(getString(R.string.text_to_pdf_title));
        mActivityTextToPdfBinding.listLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityTextToPdfBinding.defaultLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        mActivityTextToPdfBinding.defaultLayout.toolbar.toolbarNameOption.setOnClickListener(view -> showConverterPopup(1));
        mActivityTextToPdfBinding.defaultLayout.toolbar.toolbarNameTv.setOnClickListener(view -> showConverterPopup(1));

        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        fetchOldOption();

        mActivityTextToPdfBinding.defaultLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        mActivityTextToPdfBinding.listLayout.layoutCreatePdfFile.setOnClickListener((v) -> this.createPdfFile());
        mActivityTextToPdfBinding.listLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mTextFileAdapter = new TextFileAdapter(this);
        mActivityTextToPdfBinding.listLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActivityTextToPdfBinding.listLayout.recyclerView.setAdapter(mTextFileAdapter);
        TextFileTouchCallback callback = new TextFileTouchCallback(mTextFileAdapter.getItemTouchListener());
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mActivityTextToPdfBinding.listLayout.recyclerView);

        mActivityTextToPdfBinding.defaultLayout.importFileBtnImport.setOnClickListener(view -> {
            if (mFileListSelectorAdapter.getNumberSelectedFile() == 0) {
                SnackBarUtils.getShortSnackbar(this, getString(R.string.text_to_pdf_nothing_to_clear)).show();
            } else {
                CommonUtils.hideKeyboard(this);
                mActivityTextToPdfBinding.defaultLayout.searchEdt.clearFocus();

                SweetAlertDialog checkingFileDialog = DialogFactory.getDialogProgress(this, "Checking file...");
                checkingFileDialog.show();

                AsyncTask.execute(() -> {
                    mSelectedList = new ArrayList<>();
                    List<FileData> selectedList = mFileListSelectorAdapter.getSelectedList();
                    for (int i = 0; i < selectedList.size(); i++) {
                        FileData fileData = selectedList.get(i);
                        String filePath = fileData.getFilePath();
                        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_WORD)) {
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

        mActivityTextToPdfBinding.defaultLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                        String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_WORD);

                        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_WORD)) {
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

                if (RealPathUtil.getInstance().isDriveFile(uri)) {
                    startDownloadFromGoogleDrive(uri);
                    return;
                }

                String filePath = RealPathUtil.getInstance().getRealPath(this, uri, FileUtils.FileType.type_WORD);

                if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_WORD)) {
                    String fileName = FileUtils.getFileName(this, uri);
                    mSelectedList.add(new DocumentData(fileName, uri, filePath));
                }

                if (mSelectedList.size() == 0) {
                    ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
                }

                updateSelectedFile(true);
            }
        } else if (requestCode == CREATE_PDF_FROM_SELECT_FILE) {
            mOptions = new TextToPDFOptions(FileUtils.getDefaultFileName(DataConstants.TEXT_TO_PDF_PREFIX_NAME), OptionConstants.DEFAULT_PAGE_SIZE, false, "", mSelectedList,
                    OptionConstants.DEFAULT_FONT_SIZE, OptionConstants.DEFAULT_FONT_FAMILY);
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

        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_WORD)) {
            String fileName = FileUtils.getFileName(this, uri);
            mSelectedList.add(new DocumentData(fileName, uri, filePath));

            updateSelectedFile(true);
        }
    }

    @Override
    protected void updateFilePathFromGGDriveList(int index, ArrayList<Uri> uriList, String filePath) {
        super.updateFilePathFromGGDriveList(index, uriList, filePath);
        Uri uri = uriList.get(index);

        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_WORD)) {
            String fileName = FileUtils.getFileName(this, uri);
            mSelectedList.add(new DocumentData(fileName, uri, filePath));
        }

        updateSelectedFile(true);
    }

    @Override
    public void onBackPressed() {
        if (!mIsFromOtherScreen && mSelectedList.size() > 0) {
            mSelectedList = new ArrayList<>();
            mTextFileAdapter.removeAllData();

            resetNameAndPassword();
            setForLayoutView();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateNumberSelected() {
        int numberSelected = (mFileListSelectorAdapter == null ? 0 : mFileListSelectorAdapter.getNumberSelectedFile());
        mActivityTextToPdfBinding.defaultLayout.importFileBtnImport.setText(getString(R.string.import_file) + " (" + numberSelected + ")");
    }

    private void fetchOldOption() {
        TextToPDFOptions textToPDFOptions = DataManager.getInstance(this).getTextToPDFOptions();
        if (textToPDFOptions != null) {
            mOptions = textToPDFOptions;
            resetNameAndPassword();
        } else {
            mOptions = new TextToPDFOptions(FileUtils.getDefaultFileName(DataConstants.TEXT_TO_PDF_PREFIX_NAME), OptionConstants.DEFAULT_PAGE_SIZE, false, "", mSelectedList,
                    OptionConstants.DEFAULT_FONT_SIZE, OptionConstants.DEFAULT_FONT_FAMILY);
        }
    }

    private void resetNameAndPassword() {
        if (mOptions != null) {
            mOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.TEXT_TO_PDF_PREFIX_NAME));
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
        String[] mimeTypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.text_to_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedList.size() > 0) {
            mActivityTextToPdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            mActivityTextToPdfBinding.listLayout.contentView.setVisibility(View.VISIBLE);
        } else {
            mActivityTextToPdfBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivityTextToPdfBinding.listLayout.contentView.setVisibility(View.GONE);
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
            mActivityTextToPdfBinding.defaultLayout.importFileBtnImport.setVisibility(View.GONE);
        } else {
            mFileListSelectorAdapter = new FileListSelectAdapter(this);
            mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
            mActivityTextToPdfBinding.defaultLayout.importFileBtnImport.setVisibility(View.VISIBLE);
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
        mTextToPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mTextToPdfViewModel.getFileList(DataConstants.FILE_TYPE_TEXT, FileUtils.SORT_BY_DATE_DESC);
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
                if (mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            updateNumberSelected();
            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_word_found);

        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityTextToPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

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
                mTextFileAdapter.setFileData(mSelectedList);
                mTextFileAdapter.notifyDataSetChanged();
                setForLayoutView();
            }
        } else {
            mTextFileAdapter.setFileData(mSelectedList);
            mTextFileAdapter.notifyDataSetChanged();
            setForLayoutView();
        }
    }

    private void createPdfFile() {
        if (mOptions != null && mOptions.getOutFileName() != null && mOptions.getOutFileName().startsWith(DataConstants.TEXT_TO_PDF_PREFIX_NAME)) {
            mOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.TEXT_TO_PDF_PREFIX_NAME));
        } else if (mOptions == null) {
            mOptions = new TextToPDFOptions(FileUtils.getDefaultFileName(DataConstants.TEXT_TO_PDF_PREFIX_NAME), OptionConstants.DEFAULT_PAGE_SIZE, false, "", mSelectedList,
                    OptionConstants.DEFAULT_FONT_SIZE, OptionConstants.DEFAULT_FONT_FAMILY);
        }

        SettingTextToPdfDialog dialog = new SettingTextToPdfDialog(mOptions, this);
        dialog.show(getSupportFragmentManager(), dialog.getTag());
    }

    @Override
    public void onClick(int position) {
        // TODO view text file
    }

    @Override
    public void onDelete(int position) {
        if (mIsFromOtherScreen) {
            finish();
            return;
        }

        if (position < mSelectedList.size()) {
            mSelectedList.remove(position);
            mTextFileAdapter.removeData(position);

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
    public void submitForm(TextToPDFOptions textToPDFOptions) {
        checkIAPDoneBeforeAction(() -> {
            mOptions = textToPDFOptions;

            if (mOptions == null) {
                mOptions = new TextToPDFOptions(FileUtils.getDefaultFileName(DataConstants.TEXT_TO_PDF_PREFIX_NAME), OptionConstants.DEFAULT_PAGE_SIZE, false, "", mSelectedList,
                        OptionConstants.DEFAULT_FONT_SIZE, OptionConstants.DEFAULT_FONT_FAMILY);
                ;
            } else {
                mOptions.setInputFileUri(mSelectedList);
            }

            Gson gson = new Gson();
            String json = gson.toJson(mOptions);
            Intent intent = new Intent(this, TextToPdfDoneActivity.class);
            intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
            startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
        });
    }
}
