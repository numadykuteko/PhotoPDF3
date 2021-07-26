package com.pdfconverter.jpg2pdf.pdf.converter.ui.split;

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
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitFileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivitySplitPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemSplitActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SplitPageSelectDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SplitRangeDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.done.SplitPdfDoneActivity;
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

public class SplitPdfActivity extends BaseBindingActivity<ActivitySplitPdfBinding, SplitPdfViewModel> implements SplitPdfNavigator, OnFileItemClickListener, OnItemSplitActionListener {

    private SplitPdfViewModel mSplitPdfViewModel;
    private ActivitySplitPdfBinding mActivitySplitPdfBinding;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;

    private SweetAlertDialog mRequestPermissionDialog;

    private ArrayList<SplitFileData> mInputList = new ArrayList<>();
    private SplitPdfAdapter mInputAdapter;

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
        return R.layout.activity_split_pdf;
    }

    @Override
    public SplitPdfViewModel getViewModel() {
        mSplitPdfViewModel = ViewModelProviders.of(this).get(SplitPdfViewModel.class);
        return mSplitPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySplitPdfBinding = getViewDataBinding();
        mSplitPdfViewModel.setNavigator(this);

        mIsFromOtherScreen = false;

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_PDF)) {
            mIsFromOtherScreen = true;
            mFilePath = extraFilePath;
            String name = FileUtils.getFileName(extraFilePath);
            mNumberPage = FileUtils.getNumberPages(mFilePath);
            mSelectedFile = new DocumentData(name, Uri.fromFile(new File(mFilePath)), mFilePath);
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivitySplitPdfBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.split_pdf_title));
        mActivitySplitPdfBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivitySplitPdfBinding.listLayout.toolbarNameTv.setText(getString(R.string.split_pdf_title));
        mActivitySplitPdfBinding.listLayout.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mActivitySplitPdfBinding.listLayout.toolbarActionReset.setOnClickListener(view -> {
            mInputList = new ArrayList<>();
            mInputAdapter.removeAllData();
        });

        ImageView closeBtn = mActivitySplitPdfBinding.defaultLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mActivitySplitPdfBinding.defaultLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        mActivitySplitPdfBinding.listLayout.layoutCreatePdfFile.setOnClickListener((v) -> this.splitPdfFile());
        mActivitySplitPdfBinding.listLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mActivitySplitPdfBinding.listLayout.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mInputAdapter = new SplitPdfAdapter(this);
        mActivitySplitPdfBinding.listLayout.recyclerView.setAdapter(mInputAdapter);

        setForLayoutView();
        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        }

        mActivitySplitPdfBinding.defaultLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                SnackBarUtils.getSnackbar(this, getString(R.string.split_pdf_file_encrypted))
                        .setAction(R.string.remove_password_now, v -> gotoUnlockPasswordActivity(filePath, null)).show();
                return;
            }

            mFilePath = filePath;
            String name = FileUtils.getFileName(this, uri);
            mNumberPage = FileUtils.getNumberPages(mFilePath);
            mSelectedFile = new DocumentData(name, uri, mFilePath);

            setForLayoutView();
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
    }

    @Override
    public void onBackPressed() {
        if (mInputList.size() > 0) {
            mInputList = new ArrayList<>();
            mInputAdapter.removeAllData();

            setForLayoutView();
        } else {
            if (!mIsFromOtherScreen && (mFilePath != null || mSelectedFile != null)) {
                mSelectedFile = null;
                mFilePath = null;
                setForLayoutView();
                return;
            }

            super.onBackPressed();
        }
    }

    private void startChooseFileActivity() {
        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, getString(R.string.pdf_type));
        try {
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.split_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedFile != null && mFilePath != null) {
            mActivitySplitPdfBinding.listLayout.nameFile.setText(mSelectedFile.getDisplayName());
            mActivitySplitPdfBinding.listLayout.nameFile.setOnClickListener(view -> {
                gotoPdfFileViewActivity(mFilePath);
            });

            mActivitySplitPdfBinding.listLayout.descriptionFile.setText(getString(R.string.split_pdf_number_page) + " " + mNumberPage);
        } else {
            mActivitySplitPdfBinding.listLayout.nameFile.setText(getString(R.string.split_pdf_nothing_to_clear));
            mActivitySplitPdfBinding.listLayout.nameFile.setOnClickListener(view -> {
                // donothing
            });

            mActivitySplitPdfBinding.listLayout.descriptionFile.setText("");
        }

        if (mSelectedFile != null && mFilePath != null) {
            mActivitySplitPdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            mActivitySplitPdfBinding.listLayout.contentView.setVisibility(View.VISIBLE);

            mActivitySplitPdfBinding.listLayout.btnOption1.setOnClickListener(view -> startOption1());

            mActivitySplitPdfBinding.listLayout.btnOption2.setOnClickListener(view -> startOption2());

        } else {
            mActivitySplitPdfBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivitySplitPdfBinding.listLayout.contentView.setVisibility(View.GONE);
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivitySplitPdfBinding.defaultLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mSplitPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mSplitPdfViewModel.getUnlockedFileList();
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
                if (mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivitySplitPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

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

    private void splitPdfFile() {
        if (mInputList.size() == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.split_pdf_empty_split_list));
        } else {

            checkIAPDoneBeforeAction(() -> {
                SplitPDFOptions splitPDFOptions = new SplitPDFOptions(mInputList, mFilePath, mSelectedFile);
                Gson gson = new Gson();
                String json = gson.toJson(splitPDFOptions);
                Intent intent = new Intent(this, SplitPdfDoneActivity.class);
                intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
                startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
            });
        }
    }

    private void startOption1() {
        if (mNumberPage == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.split_pdf_empty_pdf));
            return;
        }
        SplitRangeDialog splitRangeDialog = new SplitRangeDialog(this, mNumberPage, new SplitRangeDialog.SplitRangeListener() {
            @Override
            public void onSubmitRange(int start, int end) {
                addSplitFileByRange(start, end);
            }

            @Override
            public void onCancel() {

            }
        });
        splitRangeDialog.show();
    }

    private void startOption2() {
        if (mNumberPage == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.split_pdf_empty_pdf));
            return;
        }

        SplitPageSelectDialog splitPageSelectDialog = new SplitPageSelectDialog(this, mNumberPage, output -> {
            if (output.size() > 0) {
                addSplitFileByPageList(output);
            } else {
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.split_pdf_option_2_error));
            }
        });
        splitPageSelectDialog.show();
    }

    private void addSplitFileByRange(int fromPage, int toPage) {
        int nextIndex = mInputList.size() + 1;
        String splitName = FileUtils.generateSplitFileName(mSelectedFile.getDisplayName(), nextIndex);

        SplitFileData splitFileData = new SplitFileData(splitName, null, fromPage, toPage, false);
        mInputList.add(splitFileData);
        mInputAdapter.addData(splitFileData);
    }

    private void addSplitFileByPageList(ArrayList<Integer> pageList) {
        if (pageList.size() == 0) return;
        int nextIndex = mInputList.size() + 1;
        String splitName = FileUtils.generateSplitFileName(mSelectedFile.getDisplayName(), nextIndex);

        SplitFileData splitFileData = new SplitFileData(splitName, null, pageList, false);
        mInputList.add(splitFileData);
        mInputAdapter.addData(splitFileData);
    }

    private void createOtherSplitPart(int position) {
        SplitFileData splitFileData = mInputList.get(position);

        ArrayList<Integer> otherList = new ArrayList<>();
        ArrayList<Integer> mainList = splitFileData.getPageList();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Add split file successfully with page: ");

        for (int i = 1; i <= mNumberPage; i++) {
            if (!mainList.contains(i)) {
                otherList.add(i);
                stringBuilder.append(i).append(" ");
            }
        }

        if (otherList.size() > 0) {
            addSplitFileByPageList(otherList);
            SnackBarUtils.getSnackbar(this, stringBuilder.toString()).show();
        } else {
            ToastUtils.showMessageShort(this, getString(R.string.split_pdf_selected_file_is_total_page));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view, boolean isCreated, int position) {
        // show option menu'
        PopupMenu popup = new PopupMenu(this, view);
        //inflating menu from xml resource
        popup.inflate(R.menu.item_split_menu);
        //adding click listener
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.create_other_split_file:
                    createOtherSplitPart(position);
                    return true;
                default:
                    return false;
            }
        });
        //displaying the popup
        popup.show();
    }

    @Override
    public void onOption(boolean isCreated, int position) {
        if (position < mInputList.size()) {
            mInputList.remove(position);
        }
        mInputAdapter.removeData(position);
    }
}
