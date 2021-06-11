package com.pdfconverter.jpg2pdf.pdf.converter.ui.browser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.FragmentBrowserBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ConfirmDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PdfLockedOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PdfOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.BrowserAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BrowserFragment extends BaseFragment<FragmentBrowserBinding, BrowserViewModel> implements BrowserNavigator, OnFileItemWithOptionClickListener {

    private BrowserViewModel mBrowserViewModel;
    private FragmentBrowserBinding mFragmentBrowserBinding;
    private boolean mIsLoading;
    private List<FileData> mListFile = new ArrayList<>();
    private BrowserAdapter mFileListAdapter;

    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;

    private PdfOptionDialog pdfOptionDialog;
    private PdfLockedOptionDialog pdfLockedOptionDialog;

    private FileData mCurrentFolder;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_browser;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    @SuppressLint("SetTextI18n")
    public void reloadData(boolean isForceReload) {
        if (mActivity != null && mActivity.notHaveStoragePermission()) {
            mFragmentBrowserBinding.pullToRefresh.setRefreshing(false);

            showPermissionIssueArea();
            mIsLoading = false;
            return;
        }

        if (mIsLoading) return;

        mIsLoading = true;

        if (mListFile == null || mListFile.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        mFragmentBrowserBinding.fileDir.setText("Dir: " + mCurrentFolder.getFilePath());
        mBrowserViewModel.setCurrentPath(mCurrentFolder);
        mBrowserViewModel.getFileList();
    }

    @Override
    public BrowserViewModel getViewModel() {
        mBrowserViewModel = ViewModelProviders.of(this).get(BrowserViewModel.class);
        return mBrowserViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBrowserViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBrowserBinding = getViewDataBinding();

        setForClick();
        initView();
        setForLiveData();

        reloadData(true);
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
                        reloadData(true);
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
    public void onResume() {
        if (mIsRequestFullPermission) {
            mIsRequestFullPermission = false;

            if (!mActivity.notHaveStoragePermission()) {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
            } else {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                mRequestPermissionDialog.setContentText(getString(R.string.couldnt_get_file_now));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
            }
        }

        reloadData(false);
        super.onResume();
    }


    public static BrowserFragment newInstance() {
        BrowserFragment browserFragment = new BrowserFragment();

        Bundle args = new Bundle();
        browserFragment.setArguments(args);
        browserFragment.setRetainInstance(true);

        return browserFragment;
    }

    private void initView() {
        mFragmentBrowserBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });

        mFileListAdapter = new BrowserAdapter(this);
        mFragmentBrowserBinding.dataListArea.setLayoutManager(new LinearLayoutManager(mActivity));
        mFragmentBrowserBinding.dataListArea.setAdapter(mFileListAdapter);

        File rootFile = Environment.getExternalStorageDirectory();
        String rootDir = rootFile.getAbsolutePath();

        mCurrentFolder = new FileData();
        mCurrentFolder.setFilePath(rootDir);
    }

    public boolean isCurrentFolderRoot() {
        File rootFile = Environment.getExternalStorageDirectory();
        String rootDir = rootFile.getAbsolutePath();
        return mCurrentFolder == null || mCurrentFolder.getFilePath().equals(rootDir);
    }

    public void onBackPress() {
        try {
            if (mCurrentFolder.getParentFile() != null && mCurrentFolder.getParentFile().getFilePath() != null && mCurrentFolder.getParentFile().getFilePath().length() > 0) {
                if (mIsLoading) return;

                File dir = new File(mCurrentFolder.getParentFile().getFilePath());
                if (dir.isDirectory()) {
                    FileData temp = mCurrentFolder.getParentFile();
                    mCurrentFolder = new FileData(temp);

                    reloadData(false);
                }
            }

        } catch (Exception e) {
        }
    }

    private void setForClick() {
    }

    private void setForLiveData() {
        mBrowserViewModel.getListFileLiveData().observe(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(List<FileData> fileDataList) {
        if (fileDataList.size() > 0) {
            if (fileDataList.equals(mListFile)) {
                mIsLoading = false;
                mFragmentBrowserBinding.pullToRefresh.setRefreshing(false);

                return;
            }

            mListFile = new ArrayList<>();
            mListFile.addAll(fileDataList);

            Parcelable oldPosition = null;
            if (mFragmentBrowserBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mFragmentBrowserBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mFileListAdapter.setData(mListFile);
            if (oldPosition != null) {
                mFragmentBrowserBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        } else {
            showNoDataArea();
        }

        mIsLoading = false;
        mFragmentBrowserBinding.pullToRefresh.setRefreshing(false);
    }

    private void startRequestPermission() {
        if (mActivity != null && mActivity.notHaveStoragePermission()) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(mActivity, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
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
            reloadEasyChangeData();
        }
    }

    private void showNoDataArea() {
        mFragmentBrowserBinding.noDataErrorTv.setText(R.string.no_pdf_found);

        mFragmentBrowserBinding.dataListArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.noDataErrorArea.setVisibility(View.VISIBLE);
        mFragmentBrowserBinding.noPermissionArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mFragmentBrowserBinding.noPermissionArea.setOnClickListener(v -> {
            startRequestPermission();
        });
        mFragmentBrowserBinding.dataListArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.noPermissionArea.setVisibility(View.VISIBLE);
        mFragmentBrowserBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mFragmentBrowserBinding.dataListArea.setVisibility(View.VISIBLE);
        mFragmentBrowserBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.noPermissionArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mFragmentBrowserBinding.dataListArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentBrowserBinding.loadingArea.setVisibility(View.VISIBLE);
        mFragmentBrowserBinding.noPermissionArea.setVisibility(View.GONE);
    }

    @Override
    public void onClickItem(int position) {
        if (position >= 0 && position < mListFile.size()) {
            FileData fileData = mListFile.get(position);
            String filePath = fileData.getFilePath();
            if (fileData.getFileType().equals(DataConstants.FILE_TYPE_DIRECTORY)) {
                mCurrentFolder = fileData;
                reloadData(false);
                return;
            }
            if (filePath == null) {
                filePath = RealPathUtil.getInstance().getRealPath(mActivity, mListFile.get(position).getFileUri(), FileUtils.FileType.type_PDF);
            }

            String finalFilePath = filePath;
            if (mActivity != null) {
                mActivity.showMyPdfAdsBeforeAction(() -> {
                    mActivity.gotoPdfFileViewActivity(finalFilePath);
                    mFileListAdapter.setCurrentItem(position);
                });
            }
        }

    }

    @Override
    public void onShareItem(int position) {
        FileUtils.shareFile(mActivity, new File(mListFile.get(position).getFilePath()));
    }

    @Override
    public void onOptionItem(int position) {
        FileData fileData = mListFile.get(position);
        mBrowserViewModel.startCheckIsFileBookmarked(fileData.getFilePath(), position, this::onShowOption);
    }

    private void onShowOption(int position, boolean isBookmarked) {
        FileData fileData = mListFile.get(position);

        if (PdfUtils.isPDFEncrypted(fileData.getFilePath())) {
            hideOptionDialog();
            pdfLockedOptionDialog = new PdfLockedOptionDialog(isBookmarked, fileData.getDisplayName(), fileData.getTimeAdded(), position, new PdfLockedOptionDialog.LockedFileOptionListener() {
                @Override
                public void openFile(int position) {
                    openPdfFile(position);
                }

                @Override
                public void shareFile(int position) {
                    onShareItem(position);
                }

                @Override
                public void uploadFile(int position) {
                    FileUtils.uploadFile(mActivity, new File(mListFile.get(position).getFilePath()));
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
            pdfLockedOptionDialog.show(getChildFragmentManager(), pdfLockedOptionDialog.getTag());
            CommonUtils.hideKeyboard(mActivity);
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
                    FileUtils.uploadFile(mActivity, new File(mListFile.get(position).getFilePath()));
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
            pdfOptionDialog.show(getChildFragmentManager(), pdfOptionDialog.getTag());
            CommonUtils.hideKeyboard(mActivity);
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
        FirebaseUtils.sendEventFunctionUsed(mActivity, "Open file", "From browser");
        onClickItem(position);
    }

    private void renamePdfFile(int position) {
        FirebaseUtils.sendEventFunctionUsed(mActivity, "Rename file", "From browser");

        FileData fileData = mListFile.get(position);
        String displayName;
        try {
            displayName = fileData.getDisplayName().substring(0, fileData.getDisplayName().lastIndexOf("."));
        } catch (Exception e) {
            return;
        }

        RenameFileDialog renameFileDialog = new RenameFileDialog(mActivity, displayName, new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String name) {
                String newName = name + ".pdf";
                int result = FileUtils.renameFile(fileData, newName);

                if (result == -2 || result == 0) {
                    ToastUtils.showMessageShort(mActivity, getString(R.string.can_not_edit_video_name));
                } else if (result == -1) {
                    SnackBarUtils.getSnackbar(mActivity, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(mActivity, getString(R.string.rename_file_success)).show();
                    String oldFilePath = fileData.getFilePath();

                    fileData.setFilePath(fileData.getFilePath().replace(fileData.getDisplayName(), newName));
                    fileData.setDisplayName(newName);
                    mListFile.set(position, fileData);
                    mFileListAdapter.updateData(position, fileData);

                    mBrowserViewModel.updateSavedData(oldFilePath, fileData.getFilePath());
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }

    private void deletePdfFile(int position) {
        FirebaseUtils.sendEventFunctionUsed(mActivity, "Delete file", "From browser");

        FileData fileData = mListFile.get(position);

        ConfirmDialog confirmDialog = new ConfirmDialog(mActivity, mActivity.getString(R.string.confirm_delete_file_title), mActivity.getString(R.string.confirm_delete_file_message), new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                if (mActivity != null && !mActivity.notHaveStoragePermission()) {
                    FileUtils.deleteFileOnExist(fileData.getFilePath());
                    mBrowserViewModel.clearSavedData(fileData.getFilePath());

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
                    SnackBarUtils.getSnackbar(mActivity, mActivity.getString(R.string.delete_success_text)).show();
                    hideOptionDialog();
                }
            }

            @Override
            public void onCancel() {

            }
        });
        confirmDialog.show();
    }
}
