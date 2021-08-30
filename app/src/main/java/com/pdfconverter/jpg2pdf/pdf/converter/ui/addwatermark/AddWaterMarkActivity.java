package com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.itextpdf.text.BaseColor;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.Watermark;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityAddWatermarkBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.done.AddWaterMarkDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ColorOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.DataOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddWaterMarkActivity extends BaseBindingActivity<ActivityAddWatermarkBinding, AddWaterMarkViewModel> implements AddWaterMarkNavigator, OnFileItemClickListener {
    private AddWaterMarkViewModel mAddWaterMarkViewModel;
    private ActivityAddWatermarkBinding mActivityAddWaterMarkBinding;
    private DocumentData mSelectedFile = null;
    private String mFilePath = null;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;
    private SweetAlertDialog mRequestPermissionDialog;

    private Watermark mWatermark;

    private List<FileData> mListFileSelector = new ArrayList<>();
    private FileListNoAdsAdapter mFileListSelectorAdapter;

    private boolean mIsFromOtherScreen = false;

    private int mWatermarkColor;
    private String mWatermarkFontFamily;
    private String mWatermarkFontStyle;
    private String mWatermarkPosition;

    private String mFileSelectorSearchKey = "";

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_add_watermark;
    }

    @Override
    public AddWaterMarkViewModel getViewModel() {
        mAddWaterMarkViewModel = ViewModelProviders.of(this).get(AddWaterMarkViewModel.class);
        return mAddWaterMarkViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityAddWaterMarkBinding = getViewDataBinding();
        mAddWaterMarkViewModel.setNavigator(this);

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
        mActivityAddWaterMarkBinding.selectLayout.toolbar.toolbarNameTv.setText(getString(R.string.add_watermark_title));
        mActivityAddWaterMarkBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.add_watermark_title));

        mActivityAddWaterMarkBinding.selectLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());
        mActivityAddWaterMarkBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);
        preloadDoneAdsIfInit();

        setForLayoutView();
        resetAllInputField();

        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        }

        mActivityAddWaterMarkBinding.defaultLayout.contentScrollView.setSmoothScrollingEnabled(false);

        ImageView closeBtn = mActivityAddWaterMarkBinding.selectLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        mActivityAddWaterMarkBinding.selectLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
    protected void setClick() {}

    @Override
    protected void updateFilePathFromGGDrive(Uri uri, String filePath) {
        super.updateFilePathFromGGDrive(uri, filePath);

        checkFilePathGet(uri, filePath);
    }

    private void checkFilePathGet(Uri uri, String filePath) {
        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
            if (!PdfUtils.isPDFEncrypted(filePath)) {
                mFilePath = filePath;
                String name = FileUtils.getFileName(this, uri);
                mSelectedFile = new DocumentData(name, uri, mFilePath);
                mActivityAddWaterMarkBinding.defaultLayout.contentScrollView.scrollTo(0,0);

                setForLayoutView();
            } else {
                SnackBarUtils.getSnackbar(this, getString(R.string.add_watermark_file_is_encrypted_before))
                        .setAction(R.string.remove_password_now, v -> gotoUnlockPasswordActivity(filePath, null)).show();
            }
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file_from_google_drive));
        }
    }

    @Override
    public void onFragmentDetached(String tag) {}

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
    public void onBackPressed() {
        if (!mIsFromOtherScreen && (mFilePath != null || mSelectedFile != null)) {
            mSelectedFile = null;
            mFilePath = null;
            CommonUtils.hideKeyboard(this);
            setForLayoutView();
            return;
        }

        super.onBackPressed();
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedFile == null || mFilePath == null) {
            mActivityAddWaterMarkBinding.selectLayout.contentView.setVisibility(View.VISIBLE);
            mActivityAddWaterMarkBinding.defaultLayout.contentView.setVisibility(View.GONE);

            mActivityAddWaterMarkBinding.selectLayout.btnLayoutSelectFile.setOnClickListener(view -> checkPermissionBeforeGetFile());
        } else {
            mActivityAddWaterMarkBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            mActivityAddWaterMarkBinding.selectLayout.contentView.setVisibility(View.GONE);

            mActivityAddWaterMarkBinding.defaultLayout.btnStartAddWatermark.setOnClickListener(view -> startAddWaterMark());
        }
    }

    @SuppressLint("SetTextI18n")
    private void resetAllInputField() {
        mActivityAddWaterMarkBinding.defaultLayout.watermarkTextEdt.setText("");
        mActivityAddWaterMarkBinding.defaultLayout.watermarkFontSizeEdt.setText(OptionConstants.DEFAULT_FONT_SIZE + "");
        mActivityAddWaterMarkBinding.defaultLayout.watermarkAngleEdt.setText(OptionConstants.DEFAULT_ANGLE + "");

        mWatermarkColor = ColorUtils.getColorFromResource(this, R.color.black_large_transparent);
        mWatermarkFontFamily = OptionConstants.LIST_FONT_FAMILY_NAME[0];
        mWatermarkFontStyle = OptionConstants.LIST_FONT_STYLE_NAME[0];
        mWatermarkPosition = OptionConstants.LIST_POSITION_NAME[0];

        mActivityAddWaterMarkBinding.defaultLayout.addWmFontFamily.setText(mWatermarkFontFamily);
        mActivityAddWaterMarkBinding.defaultLayout.addWmFontStyle.setText(mWatermarkFontStyle);
        mActivityAddWaterMarkBinding.defaultLayout.addWmPosition.setText(mWatermarkPosition);

        mActivityAddWaterMarkBinding.defaultLayout.addWmColor.setCardBackgroundColor(mWatermarkColor);

        mActivityAddWaterMarkBinding.defaultLayout.addWmFontFamily.setOnClickListener(view -> {
            DataOptionDialog dataOptionDialog = new DataOptionDialog(this, getString(R.string.import_font_family), OptionConstants.LIST_FONT_FAMILY_NAME, mWatermarkFontFamily, newOption -> {
                mWatermarkFontFamily = OptionConstants.LIST_FONT_FAMILY_NAME[newOption];
                mActivityAddWaterMarkBinding.defaultLayout.addWmFontFamily.setText(mWatermarkFontFamily);
            });
            dataOptionDialog.show();
        });

        mActivityAddWaterMarkBinding.defaultLayout.addWmFontStyle.setOnClickListener(view -> {
            DataOptionDialog dataOptionDialog = new DataOptionDialog(this, getString(R.string.font_style), OptionConstants.LIST_FONT_STYLE_NAME, mWatermarkFontStyle, newOption -> {
                mWatermarkFontStyle = OptionConstants.LIST_FONT_STYLE_NAME[newOption];
                mActivityAddWaterMarkBinding.defaultLayout.addWmFontStyle.setText(mWatermarkFontStyle);
            });
            dataOptionDialog.show();
        });

        mActivityAddWaterMarkBinding.defaultLayout.addWmPosition.setOnClickListener(view -> {
            DataOptionDialog dataOptionDialog = new DataOptionDialog(this, getString(R.string.watermark_position), OptionConstants.LIST_POSITION_NAME, mWatermarkPosition, newOption -> {
                mWatermarkPosition = OptionConstants.LIST_POSITION_NAME[newOption];
                mActivityAddWaterMarkBinding.defaultLayout.addWmPosition.setText(mWatermarkPosition);
            });
            dataOptionDialog.show();
        });

        mActivityAddWaterMarkBinding.defaultLayout.addWmColor.setOnClickListener(view -> {
            ColorOptionDialog colorOptionDialog = new ColorOptionDialog(this, mWatermarkColor, newColor -> {
                mWatermarkColor = newColor;
                mActivityAddWaterMarkBinding.defaultLayout.addWmColor.setCardBackgroundColor(mWatermarkColor);
            });
            colorOptionDialog.show();
        });
    }
    
    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityAddWaterMarkBinding.selectLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }
    
    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mAddWaterMarkViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mAddWaterMarkViewModel.getUnlockedFileList();
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
                if (mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);

        updateForSearchFileSelector();
    }

    private void showLoadingArea() {
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityAddWaterMarkBinding.selectLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);
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
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.add_watermark_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    private void collectWaterMark() {
        mWatermark = new Watermark();
        mWatermark.setDocumentData(mSelectedFile);
        mWatermark.setFilePath(mFilePath);

        if (mActivityAddWaterMarkBinding.defaultLayout.watermarkTextEdt.getText() != null) {
            mWatermark.setWatermarkText(mActivityAddWaterMarkBinding.defaultLayout.watermarkTextEdt.getText().toString().trim());
        }
        if (mActivityAddWaterMarkBinding.defaultLayout.watermarkAngleEdt.getText() != null) {
            String angle = mActivityAddWaterMarkBinding.defaultLayout.watermarkAngleEdt.getText().toString();
            int angleNumber = 0;
            try {
                angleNumber = Integer.parseInt(angle);
            } catch (Exception ignored) {
            }
            mWatermark.setRotationAngle(angleNumber);
        }
        if (mActivityAddWaterMarkBinding.defaultLayout.watermarkFontSizeEdt.getText() != null) {
            String size = mActivityAddWaterMarkBinding.defaultLayout.watermarkFontSizeEdt.getText().toString();
            int sizeNumber = 0;
            try {
                sizeNumber = Integer.parseInt(size);
            } catch (Exception ignored) {
            }
            mWatermark.setTextSize(sizeNumber);
        }

        mWatermark.setFontFamily(OptionConstants.LIST_FONT_FAMILY[indexOfData(OptionConstants.LIST_FONT_FAMILY_NAME, mWatermarkFontFamily)]);
        mWatermark.setFontStyle(OptionConstants.LIST_FONT_STYLE[indexOfData(OptionConstants.LIST_FONT_STYLE_NAME, mWatermarkFontStyle)]);
        mWatermark.setPosition(OptionConstants.LIST_POSITION[indexOfData(OptionConstants.LIST_POSITION_NAME, mWatermarkPosition)]);

        mWatermark.setTextColor(new BaseColor(mWatermarkColor));
    }

    private int indexOfData(String[] dataList, String data) {
        for (int i=0; i<dataList.length; i++) {
            if (data.equals(dataList[i])) {
                return i;
            }
        }
        return 0;
    }
    private void startAddWaterMark() {
        if (mSelectedFile == null || mFilePath == null) return;

        collectWaterMark();

        if (mWatermark == null) {
            ToastUtils.showSystemIssueToast(this);
            return;
        }

        if (mWatermark.getWatermarkText() == null || mWatermark.getWatermarkText().length() == 0) {
            ToastUtils.showMessageShort(this, getString(R.string.add_watermark_please_enter_text));
            return;
        }

        checkIAPDoneBeforeAction(() -> {
            Gson gson = new Gson();
            String json = gson.toJson(mWatermark);
            Intent intent = new Intent(this, AddWaterMarkDoneActivity.class);
            intent.putExtra(EXTRA_DATA_CREATE_PDF, json);
            startActivityForResult(intent, CREATE_PDF_FROM_SELECT_FILE);
        });
    }
}
