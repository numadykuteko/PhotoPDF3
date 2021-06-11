package com.pdfconverter.jpg2pdf.pdf.converter.ui.recent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.FragmentRecentBinding;
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
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.SaveListAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RecentFragment extends BaseFragment<FragmentRecentBinding, RecentViewModel> implements RecentNavigator, OnFileItemWithOptionClickListener {

    private RecentViewModel mRecentViewModel;
    private FragmentRecentBinding mFragmentRecentBinding;
    private boolean mIsLoading;
    private List<RecentData> mListFile = new ArrayList<>();
    private SaveListAdapter mFileListAdapter;

    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;

    private PdfOptionDialog pdfOptionDialog;
    private PdfLockedOptionDialog pdfLockedOptionDialog;
    private boolean mIsCreatedView = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recent;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    public void reloadData(boolean isForceReload) {
        if (mActivity != null && mActivity.notHaveStoragePermission()) {
            mFragmentRecentBinding.pullToRefresh.setRefreshing(false);

            showPermissionIssueArea();
            mIsLoading = false;
            return;
        }

        if (mIsLoading) return;

        mIsLoading = true;

        if (mListFile == null || mListFile.size() == 0 || isForceReload) {
            if (mFragmentRecentBinding != null && mIsCreatedView) {
                showLoadingArea();
            }
        }

        mRecentViewModel.getFileList();
    }

    @Override
    public RecentViewModel getViewModel() {
        mRecentViewModel = ViewModelProviders.of(this).get(RecentViewModel.class);
        return mRecentViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRecentViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentRecentBinding = getViewDataBinding();

        setForClick();
        initView();
        setForLiveData();

        reloadData(true);
        mIsCreatedView = true;
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


    public static RecentFragment newInstance() {
        RecentFragment bookmarkFragment = new RecentFragment();

        Bundle args = new Bundle();
        bookmarkFragment.setArguments(args);
        bookmarkFragment.setRetainInstance(true);

        return bookmarkFragment;
    }

    private void initView() {
        mFragmentRecentBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });

        mFileListAdapter = new SaveListAdapter(this);
        mFragmentRecentBinding.dataListArea.setLayoutManager(new LinearLayoutManager(mActivity));
        mFragmentRecentBinding.dataListArea.setAdapter(mFileListAdapter);
    }

    private void setForClick() {

    }

    private void setForLiveData() {
        mRecentViewModel.getListFileLiveData().observe(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(List<RecentData> bookmarkDataList) {
        if (bookmarkDataList.size() > 0) {
            if (bookmarkDataList.equals(mListFile)) {
                mIsLoading = false;
                mFragmentRecentBinding.pullToRefresh.setRefreshing(false);

                return;
            }

            mListFile = new ArrayList<>();
            mListFile.addAll(bookmarkDataList);
            mListFile.add(SaveListAdapter.ADS_INDEX, new RecentData());

            Parcelable oldPosition = null;
            if (mFragmentRecentBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mFragmentRecentBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mFileListAdapter.setData(mListFile);
            if (oldPosition != null) {
                mFragmentRecentBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        } else {
            showNoDataArea();
        }

        mIsLoading = false;
        mFragmentRecentBinding.pullToRefresh.setRefreshing(false);
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
        if (mFragmentRecentBinding != null) {
            mFragmentRecentBinding.noDataErrorTv.setText(R.string.no_pdf_found);

            mFragmentRecentBinding.dataListArea.setVisibility(View.GONE);
            mFragmentRecentBinding.noDataErrorArea.setVisibility(View.VISIBLE);
            mFragmentRecentBinding.noPermissionArea.setVisibility(View.GONE);
            mFragmentRecentBinding.loadingArea.setVisibility(View.GONE);
        }
    }

    private void showPermissionIssueArea() {
        if (mFragmentRecentBinding != null) {
            mFragmentRecentBinding.noPermissionArea.setOnClickListener(v -> {
                startRequestPermission();
            });
            mFragmentRecentBinding.dataListArea.setVisibility(View.GONE);
            mFragmentRecentBinding.noDataErrorArea.setVisibility(View.GONE);
            mFragmentRecentBinding.noPermissionArea.setVisibility(View.VISIBLE);
            mFragmentRecentBinding.loadingArea.setVisibility(View.GONE);
        }
    }

    private void showDataArea() {
        if (mFragmentRecentBinding != null) {
            mFragmentRecentBinding.dataListArea.setVisibility(View.VISIBLE);
            mFragmentRecentBinding.noDataErrorArea.setVisibility(View.GONE);
            mFragmentRecentBinding.noPermissionArea.setVisibility(View.GONE);
            mFragmentRecentBinding.loadingArea.setVisibility(View.GONE);
        }
    }

    private void showLoadingArea() {
        if (mFragmentRecentBinding != null) {
            mFragmentRecentBinding.dataListArea.setVisibility(View.GONE);
            mFragmentRecentBinding.noDataErrorArea.setVisibility(View.GONE);
            mFragmentRecentBinding.loadingArea.setVisibility(View.VISIBLE);
            mFragmentRecentBinding.noPermissionArea.setVisibility(View.GONE);
        }
    }

    public boolean isCreatedView() {
        return mIsCreatedView;
    }

    @Override
    public void onClickItem(int position) {
        String filePath = mListFile.get(position).getFilePath();
        if (!FileUtils.checkFileExist(filePath)) {
            ToastUtils.showMessageShort(mActivity, getString(R.string.file_not_found));
            mRecentViewModel.clearSavedData(filePath);
            if (position >= 0 && position < mListFile.size()) {
                mListFile.remove(position);
                if (position == 0 && mListFile.size() > 1) {
                    Collections.swap(mListFile, 0, 1);
                }
            }

            mFileListAdapter.clearData(position);
            if (mListFile.size() <= 1) {
                updateData(new ArrayList<>());
            }

            return;
        }

        if (mActivity != null) {
            mActivity.showMyPdfAdsBeforeAction(() -> mActivity.gotoPdfFileViewActivity(filePath));
        }
    }

    @Override
    public void onShareItem(int position) {
        FileUtils.shareFile(mActivity, new File(mListFile.get(position).getFilePath()));
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onOptionItem(int position) {
        RecentData recentData = mListFile.get(position);
        mRecentViewModel.startCheckIsFileBookmarked(recentData.getFilePath(), position, this::onShowOption);
    }

    private void onShowOption(int position, boolean isBookmarked) {
        RecentData recentData = mListFile.get(position);

        if (PdfUtils.isPDFEncrypted(recentData.getFilePath())) {
            hideOptionDialog();
            pdfLockedOptionDialog = new PdfLockedOptionDialog(isBookmarked, recentData.getDisplayName(), recentData.getTimeAdded(), position, new PdfLockedOptionDialog.LockedFileOptionListener() {
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
            pdfOptionDialog = new PdfOptionDialog(isBookmarked, recentData.getDisplayName(), recentData.getTimeAdded(), position, new PdfOptionDialog.FileOptionListener() {
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
        FirebaseUtils.sendEventFunctionUsed(mActivity, "Open file", "From recent");
        onClickItem(position);
    }

    private void renamePdfFile(int position) {
        RecentData recentData = mListFile.get(position);
        String displayName;
        try {
            displayName = recentData.getDisplayName().substring(0, recentData.getDisplayName().lastIndexOf("."));
        } catch (Exception e) {
            return;
        }

        RenameFileDialog renameFileDialog = new RenameFileDialog(mActivity, displayName, new RenameFileDialog.RenameFileListener() {
            @Override
            public void onSubmitName(String name) {
                String newName = name + ".pdf";
                int result = FileUtils.renameFile(recentData, newName);

                if (result == -2 || result == 0) {
                    ToastUtils.showMessageShort(mActivity, getString(R.string.can_not_edit_video_name));
                } else if (result == -1) {
                    SnackBarUtils.getSnackbar(mActivity, getString(R.string.duplicate_video_name) + ": " + name).show();
                } else {
                    SnackBarUtils.getSnackbar(mActivity, getString(R.string.rename_file_success)).show();
                    String oldFilePath = recentData.getFilePath();

                    recentData.setFilePath(recentData.getFilePath().replace(recentData.getDisplayName(), newName));
                    recentData.setDisplayName(newName);
                    mListFile.set(position, recentData);
                    mFileListAdapter.updateData(position, recentData);

                    mRecentViewModel.updateSavedData(oldFilePath, recentData.getFilePath());
                }
            }

            @Override
            public void onCancel() {

            }
        });

        renameFileDialog.show();
    }

    private void deletePdfFile(int position) {
        RecentData recentData = mListFile.get(position);

        ConfirmDialog confirmDialog = new ConfirmDialog(mActivity, mActivity.getString(R.string.confirm_delete_file_title), mActivity.getString(R.string.confirm_delete_file_message), new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                if (!mActivity.notHaveStoragePermission()) {
                    FileUtils.deleteFileOnExist(recentData.getFilePath());
                    mRecentViewModel.clearSavedData(recentData.getFilePath());

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

    private void removeFile(int position) {
        RecentData recentData = mListFile.get(position);
        mRecentViewModel.clearBookmarked(recentData.getFilePath());
        if (position >= 0 && position < mListFile.size()) {
            mListFile.remove(position);
            if (position == 0 && mListFile.size() > 1) {
                Collections.swap(mListFile, 0, 1);
            }
        }
        if (position == 0 && mListFile.size() > 1) {
            Collections.swap(mListFile, 0, 1);
        }

        mFileListAdapter.clearData(position);
        if (mListFile.size() <= 1) {
            showNoDataArea();
        }
    }
}
