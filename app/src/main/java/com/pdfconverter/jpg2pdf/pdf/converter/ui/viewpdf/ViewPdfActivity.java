package com.pdfconverter.jpg2pdf.pdf.converter.ui.viewpdf;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ads.control.Admod;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ViewPdfOption;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityViewPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.AddWaterMarkActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ConfirmDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.EnterPasswordFileDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.JumpPageDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.ViewPdfOptionDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf.EditPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
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

public class ViewPdfActivity extends BaseBindingActivity<ActivityViewPdfBinding, ViewPdfViewModel> implements ViewPdfNavigator, OnFileItemClickListener {
    private ViewPdfViewModel mViewPdfViewModel;
    private ActivityViewPdfBinding mActivityViewPdfBinding;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE = 2;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 3;

    private SweetAlertDialog mRequestPermissionDialog;

    private String mFilePath = null;
    private String mPassword = null;
    private DocumentData mSelectedFile = null;
    private boolean mIsNeedToReview = false;
    private boolean mIsFromOtherScreen = false;
    private boolean mIsFromSplash = false;

    private boolean mIsViewFull = false;

    private SweetAlertDialog mOpeningDialog;

    private List<FileData> mListFileSelector = new ArrayList<>();
    private FileListNoAdsAdapter mFileListSelectorAdapter;

    private ViewPdfOptionDialog pdfOptionDialog;

    private boolean mIsBookmarked;
    EnterPasswordFileDialog enterPasswordFileDialog;

    private String mFileSelectorSearchKey = "";

    private ViewPdfOption mViewOption;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_view_pdf;
    }

    @Override
    public ViewPdfViewModel getViewModel() {
        mViewPdfViewModel = ViewModelProviders.of(this).get(ViewPdfViewModel.class);
        return mViewPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityViewPdfBinding = getViewDataBinding();
        mViewPdfViewModel.setNavigator(this);

        mIsFromOtherScreen = false;

        String extraFilePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (extraFilePath != null && extraFilePath.length() > 0 && FileUtils.checkFileExistAndType(extraFilePath, FileUtils.FileType.type_PDF)) {
            mIsFromOtherScreen = true;
            mIsFromSplash = getIntent().getBooleanExtra(EXTRA_FROM_SPLASH, false);

            if (PdfUtils.isPDFEncrypted(extraFilePath)) {
                handleEncryptedFile(extraFilePath, null);
            } else {
                String name = FileUtils.getFileName(extraFilePath);
                mFilePath = extraFilePath;
                mSelectedFile = new DocumentData(name, null, mFilePath);

                mOpeningDialog = DialogFactory.getDialogProgress(this, getString(R.string.loading_text));
                mOpeningDialog.show();
            }

            mIsNeedToReview = getIntent().getBooleanExtra(EXTRA_IS_PREVIEW, false);
        }

        initView();
    }

    @Override
    protected void initView() {
        mActivityViewPdfBinding.defaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.view_pdf));
        mActivityViewPdfBinding.defaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityViewPdfBinding.contentLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        ImageView closeBtn = mActivityViewPdfBinding.defaultLayout.searchEdt.findViewById(R.id.search_close_btn);
        if (closeBtn != null) {
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(null);
        }

        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        mIsViewFull = false;
        setForViewFullScreen();

        mViewOption = DataManager.getInstance(this).getViewPDFOptions();
        if (mViewOption == null) {
            mViewOption = new ViewPdfOption(DataConstants.VIEW_MODE_DAY, DataConstants.VIEW_ORIENTATION_VERTICAL);
        }

        setForViewMode();
        mActivityViewPdfBinding.contentLayout.optionViewMode.setOnClickListener(v -> {
            if (mViewOption.getViewMode() == DataConstants.VIEW_MODE_DAY) {
                mViewOption.setViewMode(DataConstants.VIEW_MODE_NIGHT);
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_night_mode));
            } else {
                mViewOption.setViewMode(DataConstants.VIEW_MODE_DAY);
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_day_mode));
            }

            setForViewMode();
            DataManager.getInstance(this).saveViewPDFOptions(mViewOption);
        });

        setForViewOrientation(true);
        mActivityViewPdfBinding.contentLayout.optionViewOrientation.setOnClickListener(v -> {
            if (mViewOption.getOrientation() == DataConstants.VIEW_ORIENTATION_VERTICAL) {
                mViewOption.setOrientation(DataConstants.VIEW_ORIENTATION_HORIZONTAL);
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_horizontal_mode));
            } else {
                mViewOption.setOrientation(DataConstants.VIEW_ORIENTATION_VERTICAL);
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_vertical_mode));
            }
            setForViewOrientation(false);
            DataManager.getInstance(this).saveViewPDFOptions(mViewOption);
        });

        mActivityViewPdfBinding.contentLayout.optionViewBookmark.setOnClickListener(v -> {
            if (mIsBookmarked) {
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_unbookmarked));
            } else {
                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_bookmarked));
            }
            mViewPdfViewModel.revertBookmarked(mFilePath);
        });

        mActivityViewPdfBinding.contentLayout.optionViewJump.setOnClickListener(v -> {
            JumpPageDialog jumpPageDialog = new JumpPageDialog(this, mActivityViewPdfBinding.contentLayout.pdfView.getPageCount(), new JumpPageDialog.SplitRangeListener() {
                @Override
                public void onSubmitRange(int page) {
                    mActivityViewPdfBinding.contentLayout.pdfView.jumpTo(page - 1);
                }

                @Override
                public void onCancel() {

                }
            });
            jumpPageDialog.show();
        });

        mActivityViewPdfBinding.defaultLayout.btnLayoutSelectFile.setOnClickListener((v) -> this.checkPermissionBeforeGetFile());

        setForLayoutView();

        if (!mIsFromOtherScreen) {
            requestForFileSelector();
        }

        setForBookmark();

        mActivityViewPdfBinding.defaultLayout.searchEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            case REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        if (requestCode == REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE) {
                            startChooseFileActivity();
                        } else {
                            openPdfFile();
                        }
                        sweetAlertDialog.dismiss();
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.couldnt_get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        if (mIsFromOtherScreen) {
                            finish();
                        }
                    });
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

            if (mRequestFullPermissionCode == REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE || mRequestFullPermissionCode == REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE) {
                if (!notHaveStoragePermission()) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        if (mRequestFullPermissionCode == REQUEST_EXTERNAL_PERMISSION_FOR_GET_LOCAL_FILE) {
                            startChooseFileActivity();
                        } else {
                            openPdfFile();
                        }
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void updateFilePathFromGGDrive(Uri uri, String filePath) {
        super.updateFilePathFromGGDrive(uri, filePath);
        checkFilePathGet(uri, filePath);
    }

    private void checkFilePathGet(Uri uri, String filePath) {
        if (filePath != null && filePath.length() > 0 && FileUtils.checkFileExistAndType(filePath, FileUtils.FileType.type_PDF)) {
            if (PdfUtils.isPDFEncrypted(filePath)) {
                handleEncryptedFile(filePath, uri);
                return;
            } else {
                int numberPages = FileUtils.getNumberPages(filePath);
                if (numberPages == 0) {
                    ToastUtils.showMessageLong(this, getString(R.string.view_pdf_file_empty));
                    return;
                }
            }

            mFilePath = filePath;
            String name = FileUtils.getFileName(getApplicationContext(), uri);
            mSelectedFile = new DocumentData(name, uri, mFilePath);

            mIsViewFull = false;
            setForViewFullScreen();

            setForLayoutView();
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.can_not_select_file));
        }
    }

    private void handleEncryptedFile(String filePath, Uri uri) {
        enterPasswordFileDialog = new EnterPasswordFileDialog(this, new EnterPasswordFileDialog.EnterPasswordFileListener() {
            @Override
            public void onSubmitPassword(String password) {
                runOnUiThread(() -> {
                    if (PdfUtils.isPasswordValid(filePath, password.getBytes())) {
                        String name = "";

                        if (uri != null) {
                            name = FileUtils.getFileName(getApplicationContext(), uri);
                        } else {
                            name = FileUtils.getFileName(filePath);
                        }

                        mPassword = password;
                        mFilePath = filePath;
                        mSelectedFile = new DocumentData(name, uri, mFilePath);

                        mOpeningDialog = DialogFactory.getDialogProgress(ViewPdfActivity.this, getString(R.string.loading_text));
                        mOpeningDialog.show();

                        enterPasswordFileDialog.dismiss();
                        setForLayoutView();

                    } else {
                        ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.view_pdf_input_wrong_password));
                    }
                });
            }

            @Override
            public void onCancel() {
                if (mIsFromOtherScreen) finish();
            }
        });
        enterPasswordFileDialog.setOnCancelListener(dialogInterface -> {
            dialogInterface.dismiss();
            if (mIsFromOtherScreen) finish();
        });
        enterPasswordFileDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mIsViewFull) {
            mIsViewFull = false;
            setForViewFullScreen();
        } else {
            if (!mIsFromOtherScreen && mSelectedFile != null) {
                mSelectedFile = null;
                mFilePath = null;
                setForLayoutView();
                return;
            } else if (mIsFromSplash) {
                restartApp(false);
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
            startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.view_pdf_select_file_title)), TAKE_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO show error
        }
    }

    @SuppressLint("SetTextI18n")
    private void setForLayoutView() {
        if (mSelectedFile != null && mFilePath != null) {
            String fileName = mSelectedFile.getDisplayName();
            if (mIsFromOtherScreen && mSelectedFile.getDisplayName().contains(DataConstants.TEMP_FILE_NAME)) {
                fileName = "PDF file";
            }
            mActivityViewPdfBinding.contentLayout.toolbar.toolbarNameTv.setText(fileName);

            mActivityViewPdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            mActivityViewPdfBinding.contentLayout.contentView.setVisibility(View.VISIBLE);

            mActivityViewPdfBinding.contentLayout.toolbar.toolbarActionFullScreen.setOnClickListener(view -> {
                mIsViewFull = true;
                setForViewFullScreen();

                ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.change_to_full_screen_mode));
            });
            mActivityViewPdfBinding.contentLayout.toolbar.toolbarActionMore.setOnClickListener(view -> {
                showMoreMenu();
            });

            checkPermissionBeforeOpenFile();
        } else {
            mActivityViewPdfBinding.contentLayout.toolbar.toolbarNameTv.setText(getString(R.string.view_pdf_nothing_to_clear));

            if (!mIsFromOtherScreen) {
                mActivityViewPdfBinding.defaultLayout.contentView.setVisibility(View.VISIBLE);
            } else {
                mActivityViewPdfBinding.defaultLayout.contentView.setVisibility(View.GONE);
            }
            mActivityViewPdfBinding.contentLayout.contentView.setVisibility(View.GONE);
        }
    }

    private void hideOptionDialog() {
        if (pdfOptionDialog != null && pdfOptionDialog.isVisible()) {
            pdfOptionDialog.dismiss();
        }
    }

    private void showMoreMenu() {
        hideOptionDialog();
        pdfOptionDialog = new ViewPdfOptionDialog(mIsBookmarked, PdfUtils.isPDFEncrypted(mFilePath), mIsNeedToReview, FileUtils.getFileName(mFilePath), new ViewPdfOptionDialog.FileOptionListener() {
            @Override
            public void openFile() {

            }

            @Override
            public void shareFile() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                }
                sharePdfFile();
            }

            @Override
            public void printFile() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                }
                printPdfFile();
            }

            @Override
            public void uploadFile() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                }
                uploadPdfFile();
            }

            @Override
            public void optionBookmark() {
                mViewPdfViewModel.revertBookmarked(mFilePath);
            }

            @Override
            public void optionPassword() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                } else if (!PdfUtils.isPDFEncrypted(mFilePath)) {
                    gotoProtectPasswordActivity(mFilePath);
                    hideOptionDialog();
                } else {
                    gotoUnlockPasswordActivity(mFilePath, mPassword);
                    hideOptionDialog();
                }
            }

            @Override
            public void splitFile() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                } else if (PdfUtils.isPDFEncrypted(mFilePath)) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.add_watermark_file_is_encrypted_before));
                    return;
                }

                Intent splitIntent = new Intent(ViewPdfActivity.this, SplitPdfActivity.class);
                splitIntent.putExtra(EXTRA_FILE_PATH, mFilePath);
                startActivity(splitIntent);

                FirebaseUtils.sendEventFunctionUsed(ViewPdfActivity.this, "Split PDF", "From function");
            }

            @Override
            public void editFile() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                } else if (PdfUtils.isPDFEncrypted(mFilePath)) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.add_watermark_file_is_encrypted_before));
                    return;
                }

                Intent editIntent = new Intent(ViewPdfActivity.this, EditPdfActivity.class);
                editIntent.putExtra(EXTRA_FILE_PATH, mFilePath);
                startActivity(editIntent);

                FirebaseUtils.sendEventFunctionUsed(ViewPdfActivity.this, "Edit PDF", "From function");
            }

            @Override
            public void addWatermark() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                } else if (PdfUtils.isPDFEncrypted(mFilePath)) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.add_watermark_file_is_encrypted_before));
                    return;
                }

                Intent addWaterMarkIntent = new Intent(ViewPdfActivity.this, AddWaterMarkActivity.class);
                addWaterMarkIntent.putExtra(EXTRA_FILE_PATH, mFilePath);
                startActivity(addWaterMarkIntent);

                FirebaseUtils.sendEventFunctionUsed(ViewPdfActivity.this, "Add WaterMark PDF", "From function");
            }

            @Override
            public void extractToText() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                } else if (PdfUtils.isPDFEncrypted(mFilePath)) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.add_watermark_file_is_encrypted_before));
                    return;
                }

                Intent toTextIntent = new Intent(ViewPdfActivity.this, PdfToTextActivity.class);
                toTextIntent.putExtra(EXTRA_FILE_PATH, mFilePath);
                startActivity(toTextIntent);

                FirebaseUtils.sendEventFunctionUsed(ViewPdfActivity.this, "PDF To Text", "From function");
            }

            @Override
            public void extractToImage() {
                if (mFilePath == null) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.view_pdf_please_open_file));
                    return;
                } else if (PdfUtils.isPDFEncrypted(mFilePath)) {
                    ToastUtils.showMessageShort(getApplicationContext(), getString(R.string.add_watermark_file_is_encrypted_before));
                    return;
                }

                Intent toImageIntent = new Intent(ViewPdfActivity.this, PdfToImageActivity.class);
                toImageIntent.putExtra(EXTRA_FILE_PATH, mFilePath);
                startActivity(toImageIntent);

                FirebaseUtils.sendEventFunctionUsed(ViewPdfActivity.this, "PDF To Image", "From function");
            }

            @Override
            public void deleteFile() {
                deleteFileAndExit();
            }
        });

        pdfOptionDialog.show(getSupportFragmentManager(), pdfOptionDialog.getTag());
    }

    private void setForBookmark() {
        mActivityViewPdfBinding.contentLayout.optionViewBookmarkImg.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mViewPdfViewModel.getIsBookmarkedLiveData().observe(this, this::updateBookmarkState);
        mViewPdfViewModel.startCheckIsBookmarked(mFilePath);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "ResourceAsColor"})
    private void updateBookmarkState(boolean isBookmarked) {
        mIsBookmarked = isBookmarked;
        if (pdfOptionDialog != null && pdfOptionDialog.isVisible()) {
            pdfOptionDialog.setBookmark(mIsBookmarked);
        }
        if (mIsBookmarked) {
            mActivityViewPdfBinding.contentLayout.optionViewBookmarkImg.setColorFilter(getBookmarkedColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            mActivityViewPdfBinding.contentLayout.optionViewBookmarkImg.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    private void setForViewFullScreen() {
        if (mIsViewFull) {
            mActivityViewPdfBinding.bannerAds.setVisibility(View.GONE);
            mActivityViewPdfBinding.contentLayout.toolbar.layoutToolbar.setVisibility(View.GONE);
            mActivityViewPdfBinding.contentLayout.optionView.setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mActivityViewPdfBinding.bannerAds.setVisibility(View.VISIBLE);
            mActivityViewPdfBinding.contentLayout.toolbar.layoutToolbar.setVisibility(View.VISIBLE);
            mActivityViewPdfBinding.contentLayout.optionView.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForViewMode() {
        mActivityViewPdfBinding.contentLayout.toolbar.layoutToolbar.setBackgroundColor(getViewOptionColor());
        mActivityViewPdfBinding.contentLayout.toolbar.toolbarBtnBack.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mActivityViewPdfBinding.contentLayout.toolbar.toolbarActionFullScreen.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mActivityViewPdfBinding.contentLayout.toolbar.toolbarActionMore.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mActivityViewPdfBinding.contentLayout.toolbar.toolbarNameTv.setTextColor(getViewTextColor());
        mActivityViewPdfBinding.contentLayout.separator.setBackgroundColor(getViewOptionColor());

        mActivityViewPdfBinding.contentLayout.pdfViewContainer.setBackgroundColor(getViewPdfContainerColor());
        mActivityViewPdfBinding.contentLayout.pdfView.setBackgroundColor(getViewPdfContainerColor());
        mActivityViewPdfBinding.contentLayout.optionView.setBackgroundColor(getViewOptionColor());

        mActivityViewPdfBinding.contentLayout.optionViewOrientationImg.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        mActivityViewPdfBinding.contentLayout.optionViewJumpImg.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);

        if (mViewOption.getViewMode() == DataConstants.VIEW_MODE_DAY) {
            mActivityViewPdfBinding.contentLayout.optionViewModeImg.setImageDrawable(getDrawable(R.drawable.ic_view_night_mode));
            mActivityViewPdfBinding.contentLayout.pdfView.setNightMode(false);
        } else {
            mActivityViewPdfBinding.contentLayout.optionViewModeImg.setImageDrawable(getDrawable(R.drawable.ic_view_day_mode));
            mActivityViewPdfBinding.contentLayout.pdfView.setNightMode(true);
        }

        if (!mIsBookmarked) {
            mActivityViewPdfBinding.contentLayout.optionViewBookmarkImg.setColorFilter(getIconColor(), android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getViewOptionColor());
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForViewOrientation(boolean isFirstTime) {
        if (mViewOption.getOrientation() == DataConstants.VIEW_ORIENTATION_HORIZONTAL) {
            mActivityViewPdfBinding.contentLayout.optionViewOrientationImg.setImageDrawable(getDrawable(R.drawable.ic_view_horizontal));
        } else {
            mActivityViewPdfBinding.contentLayout.optionViewOrientationImg.setImageDrawable(getDrawable(R.drawable.ic_view_vertical));
        }

        if (!isFirstTime) {
            int currentPage = mActivityViewPdfBinding.contentLayout.pdfView.getCurrentPage();
            openPdfFileForChangeOrientation(currentPage);
        }
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
        } else {
            mFileListSelectorAdapter = new FileListNoAdsAdapter(this);
            mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new LinearLayoutManager(this));
            mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
        }
    }

    private int getBookmarkedColor() {
        return ColorUtils.getColorFromResource(this, R.color.orange_theme_color);
    }

    private int getIconColor() {
        if (mViewOption.getViewMode() == DataConstants.VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.icon_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.icon_type_night_mode);
        }
    }

    private int getViewPdfContainerColor() {
        if (mViewOption.getViewMode() == DataConstants.VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.background_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.background_type_night_mode);
        }
    }

    private int getViewOptionColor() {
        if (mViewOption.getViewMode() == DataConstants.VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.option_view_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.option_view_type_night_mode);
        }
    }

    private int getViewTextColor() {
        if (mViewOption.getViewMode() == DataConstants.VIEW_MODE_DAY) {
            return ColorUtils.getColorFromResource(this, R.color.text_type_day_mode);
        } else {
            return ColorUtils.getColorFromResource(this, R.color.text_type_night_mode);
        }
    }

    @Override
    public void onClickItem(int position) {
        if (position < mFileListSelectorAdapter.getListData().size() && position >= 0) {
            CommonUtils.hideKeyboard(this);
            mActivityViewPdfBinding.defaultLayout.searchEdt.clearFocus();

            FileData selectedFile = mFileListSelectorAdapter.getListData().get(position);
            checkFilePathGet(selectedFile.getFileUri(), selectedFile.getFilePath());
        }
    }

    @Override
    protected void onDestroy() {
        if (mIsFromOtherScreen && mSelectedFile != null && mSelectedFile.getDisplayName() != null && mSelectedFile.getDisplayName().contains(DataConstants.TEMP_FILE_NAME)) {
            if (!mIsBookmarked) {
                FileUtils.deleteFileOnExist(mSelectedFile.getFilePath());
                mViewPdfViewModel.clearSavedData(mSelectedFile.getFilePath());
            }
        }
        super.onDestroy();
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mViewPdfViewModel.getListFileSelectorLiveData().observe(this, this::updateListFileSelector);
        mViewPdfViewModel.getFileList(DataConstants.FILE_TYPE_PDF, FileUtils.SORT_BY_DATE_DESC);
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
                if (mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager() != null) {
                    oldPosition = mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onSaveInstanceState();
                }
                mFileListSelectorAdapter.setData(mListFileSelector);
                if (oldPosition != null) {
                    mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
                }
            }

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void showNoDataArea() {
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorTv.setText(R.string.no_pdf_found);

        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });

        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {

        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
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
                mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

            } else {
                mFileListSelectorAdapter.setData(mListFileSelector);
                mActivityViewPdfBinding.defaultLayout.fileSelectorLayout.dataListArea.scrollToPosition(0);

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

    private void checkPermissionBeforeOpenFile() {
        if (notHaveStoragePermission()) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_OPEN_LOCAL_FILE);
            });
            mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                sweetAlertDialog.setContentText(getString(R.string.couldnt_get_file_now));
                sweetAlertDialog.setConfirmClickListener(Dialog::dismiss);
                sweetAlertDialog.showCancelButton(false);
                sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                    sweetAlertDialog.dismiss();
                    if (mIsFromOtherScreen) {
                        finish();
                    }
                });
            });
            mRequestPermissionDialog.show();
        } else {
            openPdfFile();
        }
    }

    private void openPdfFile() {
        setForBookmark();
        mActivityViewPdfBinding.contentLayout.pdfView.fromFile(new File(mFilePath))
                .enableSwipe(true)
                .swipeHorizontal(mViewOption.getOrientation() == DataConstants.VIEW_ORIENTATION_HORIZONTAL)
                .onError(t -> errorOpenPdfFile())
                .onLoad(nbPages -> {
                    try {
                        if (mOpeningDialog != null) mOpeningDialog.dismiss();
                        mViewPdfViewModel.saveRecent(mFilePath, getString(R.string.view_pdf));
                    } catch (Exception ignored) {
                    }
                })
                .enableDoubletap(true)
                .spacing(10)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .password(PdfUtils.isPDFEncrypted(mFilePath) ? mPassword : null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .fitEachPage(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .nightMode(mViewOption.getViewMode() == DataConstants.VIEW_MODE_NIGHT)
                .load();

    }

    private void openPdfFileForChangeOrientation(int currentPage) {
        mActivityViewPdfBinding.contentLayout.pdfView.fromFile(new File(mFilePath))
                .enableSwipe(true)
                .swipeHorizontal(mViewOption.getOrientation() == DataConstants.VIEW_ORIENTATION_HORIZONTAL)
                .onError(t -> errorOpenPdfFile())
                .enableDoubletap(true)
                .onRender(nbPages -> {
                    mActivityViewPdfBinding.contentLayout.pdfView.jumpTo(currentPage);
                })
                .spacing(10)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .password(PdfUtils.isPDFEncrypted(mFilePath) ? mPassword : null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .fitEachPage(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .nightMode(mViewOption.getViewMode() == DataConstants.VIEW_MODE_NIGHT)
                .load();

    }

    private void errorOpenPdfFile() {
        if (mOpeningDialog != null) mOpeningDialog.dismiss();

        if (mIsFromOtherScreen) {
            SweetAlertDialog errorNoticeDialog = DialogFactory.getDialogError(this, getString(R.string.view_pdf_can_not_open_file_notice));
            errorNoticeDialog.setConfirmText(getString(R.string.close_text));
            errorNoticeDialog.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finish();
            });
            errorNoticeDialog.setCanceledOnTouchOutside(false);
            errorNoticeDialog.setOnDismissListener(dialogInterface -> finish());
            errorNoticeDialog.show();
        } else {
            ToastUtils.showMessageLong(this, getString(R.string.view_pdf_can_not_open_file));
            mFilePath = null;
            mSelectedFile = null;
            setForLayoutView();
        }
    }

    private void printPdfFile() {
        if (PdfUtils.isPDFEncrypted(mFilePath)) {
            SnackBarUtils.getSnackbar(this, getString(R.string.view_pdf_can_not_print_protected_file)).show();
            return;
        }
        FileUtils.printFile(this, new File(mFilePath));
    }

    private void sharePdfFile() {
        FileUtils.shareFile(this, new File(mFilePath));
    }

    private void uploadPdfFile() {
        FileUtils.uploadFile(this, new File(mFilePath));
    }

    private void deleteFileAndExit() {
        ConfirmDialog confirmDialog = new ConfirmDialog(this, getString(R.string.confirm_delete_file_title), getString(R.string.confirm_delete_file_message), new ConfirmDialog.ConfirmListener() {
            @Override
            public void onSubmit() {
                if (!notHaveStoragePermission()) {
                    FileUtils.deleteFileOnExist(mFilePath);
                    mViewPdfViewModel.clearSavedData(mFilePath);

                    SnackBarUtils.getSnackbar(ViewPdfActivity.this, getString(R.string.delete_success_text)).show();
                    hideOptionDialog();

                    setResult(RESULT_FILE_DELETED);
                    finish();
                }
            }

            @Override
            public void onCancel() {

            }
        });
        confirmDialog.show();
    }
}
