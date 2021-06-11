package com.pdfconverter.jpg2pdf.pdf.converter.ui.lib;

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
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.FragmentLibBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ConfirmDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PdfLockedOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.PdfOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.RenameFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingSortDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.main.MainActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LibFragment extends BaseFragment<FragmentLibBinding, LibViewModel> implements LibNavigator, OnFileItemWithOptionClickListener, SettingSortDialog.SettingSortSubmit {

    private LibViewModel mLibViewModel;
    private FragmentLibBinding mFragmentLibBinding;
    private int mCurrentSortBy = FileUtils.SORT_BY_DATE_DESC;
    private boolean mIsLoading;
    private List<FileData> mListFile = new ArrayList<>();
    private FileListAdapter mFileListAdapter;

    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;

    private PdfOptionDialog pdfOptionDialog;
    private PdfLockedOptionDialog pdfLockedOptionDialog;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_lib;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    public void reloadData(boolean isForceReload) {
        if (mActivity != null && mActivity.notHaveStoragePermission()) {
            mFragmentLibBinding.pullToRefresh.setRefreshing(false);

            showPermissionIssueArea();
            mIsLoading = false;
            return;
        }

        if (mIsLoading) return;

        mIsLoading = true;

        if (mListFile == null || mListFile.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        mLibViewModel.getFileList(mCurrentSortBy);
    }

    @Override
    public LibViewModel getViewModel() {
        mLibViewModel = ViewModelProviders.of(this).get(LibViewModel.class);
        return mLibViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mLibViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentLibBinding = getViewDataBinding();

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


    public static LibFragment newInstance() {
        LibFragment libFragment = new LibFragment();

        Bundle args = new Bundle();
        libFragment.setArguments(args);
        libFragment.setRetainInstance(true);

        return libFragment;
    }

    private void initView() {
        mFragmentLibBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });

        mFileListAdapter = new FileListAdapter(this);
        mFragmentLibBinding.dataListArea.setLayoutManager(new LinearLayoutManager(mActivity));
        mFragmentLibBinding.dataListArea.setAdapter(mFileListAdapter);
    }

    private void setForClick() {
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).setClickSortItem(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSortPopup();
                }
            });
        }
    }

    private void showSortPopup() {
        if (mIsLoading) {
            ToastUtils.showMessageShort(mActivity, getString(R.string.sort_not_available));
            return;
        }

        SettingSortDialog dialog = new SettingSortDialog(mActivity, this, mCurrentSortBy);
        dialog.show();
    }

    @Override
    public void updateNewSort(int newSort) {
        mCurrentSortBy = newSort;
        reloadData(true);
    }

    private void setForLiveData() {
        mLibViewModel.getListFileLiveData().observe(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(List<FileData> fileDataList) {
        if (fileDataList.size() > 0) {
            if (fileDataList.equals(mListFile)) {
                mIsLoading = false;
                mFragmentLibBinding.pullToRefresh.setRefreshing(false);

                return;
            }

            mListFile = new ArrayList<>();
            mListFile.addAll(fileDataList);

            mListFile.add(FileListAdapter.ADS_INDEX, new FileData(null, null, null, -100000, -100000, "ADS"));
            Parcelable oldPosition = null;
            if (mFragmentLibBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mFragmentLibBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mFileListAdapter.setData(mListFile);
            if (oldPosition != null) {
                mFragmentLibBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        } else {
            showNoDataArea();
        }

        mIsLoading = false;
        mFragmentLibBinding.pullToRefresh.setRefreshing(false);
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
        mFragmentLibBinding.noDataErrorTv.setText(R.string.no_pdf_found);

        mFragmentLibBinding.dataListArea.setVisibility(View.GONE);
        mFragmentLibBinding.noDataErrorArea.setVisibility(View.VISIBLE);
        mFragmentLibBinding.noPermissionArea.setVisibility(View.GONE);
        mFragmentLibBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mFragmentLibBinding.noPermissionArea.setOnClickListener(v -> {
            startRequestPermission();
        });
        mFragmentLibBinding.dataListArea.setVisibility(View.GONE);
        mFragmentLibBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentLibBinding.noPermissionArea.setVisibility(View.VISIBLE);
        mFragmentLibBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mFragmentLibBinding.dataListArea.setVisibility(View.VISIBLE);
        mFragmentLibBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentLibBinding.noPermissionArea.setVisibility(View.GONE);
        mFragmentLibBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mFragmentLibBinding.dataListArea.setVisibility(View.GONE);
        mFragmentLibBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentLibBinding.loadingArea.setVisibility(View.VISIBLE);
        mFragmentLibBinding.noPermissionArea.setVisibility(View.GONE);
    }

    @Override
    public void onClickItem(int position) {
        String filePath = mListFile.get(position).getFilePath();
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

    @Override
    public void onShareItem(int position) {
        FileUtils.shareFile(mActivity, new File(mListFile.get(position).getFilePath()));
    }

    @Override
    public void onOptionItem(int position) {
        FileData fileData = mListFile.get(position);
        mLibViewModel.startCheckIsFileBookmarked(fileData.getFilePath(), position, this::onShowOption);
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
        FirebaseUtils.sendEventFunctionUsed(mActivity, "Open file", "From lib");
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

                    mLibViewModel.updateSavedData(oldFilePath, fileData.getFilePath());
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

        ConfirmDialog confirmDialog = new ConfirmDialog(mActivity, mActivity.getString(R.string.confirm_delete_file_title), mActivity.getString(R.string.confirm_delete_file_message), new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                if (mActivity != null && !mActivity.notHaveStoragePermission()) {
                    FileUtils.deleteFileOnExist(fileData.getFilePath());
                    mLibViewModel.clearSavedData(fileData.getFilePath());

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
