package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.pdfconverter.jpg2pdf.pdf.converter.BR;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityImageToPdfBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingImageToPdfDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.done.ImageToPdfDoneActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DeminUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.ImageListSelectAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.RealPathUtil;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ImageToPdfActivity extends BaseBindingActivity<ActivityImageToPdfBinding, ImageToPdfViewModel> implements SettingImageToPdfDialog.OnDialogSubmit, OnFileItemClickListener {

    private static final String TAG = "ImageToPdfActivityTAG";

    private ImageToPdfViewModel mImageToPdfViewModel;
    private ActivityImageToPdfBinding mActivityBinding;
    private ImageAdapter mImageAdapter;
    private ImageToPDFOptions mImageToPDFOptions = new ImageToPDFOptions();
    public static int CROP_IMAGE_CODE = 26783;
    
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_CREATE_FILE = 1;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR = 2;

    public static final int OPEN_CREATE_PDF_ACTIVITY = 10000;
    public static final int NEED_TO_DESTROY = 10001;
    public static final int NOT_NEED_TO_DESTROY = 10002;
    public static final int REQUEST_GALLERY = 10003;

    private SweetAlertDialog mRequestPermissionDialog;

    private List<ImageData> mListFileSelector = new ArrayList<>();
    private ImageListSelectAdapter mFileListSelectorAdapter;
    
    private boolean mIsNeedScan;

    private ToolTipView myToolTipView;
    private ToolTipRelativeLayout toolTipRelativeLayout;

    @Override
    public int getBindingVariable() {
        return BR.imageToPdfViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_image_to_pdf;
    }

    @Override
    public ImageToPdfViewModel getViewModel() {
        mImageToPdfViewModel = ViewModelProviders.of(this).get(ImageToPdfViewModel.class);
        return mImageToPdfViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityBinding = getViewDataBinding();
        mIsNeedScan = getIntent().getBooleanExtra(EXTRA_NEED_SCAN, false);
        initView();
    }

    @Override
    protected void initView() {
        preloadDoneAdsIfInit();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        setForSelectLayout();
        setForListLayout();

        if (mIsNeedScan) {
            startCameraActivity(true);
        }

        updateNumberSelected();
        mImageToPdfViewModel.deleteFolder(getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        toolTipRelativeLayout = mActivityBinding.mainImageToPdfDefaultLayout.tooltipView;

        if (!DataManager.getInstance(this).getShowGuideConvert()) {
            if (!notHaveStoragePermission()) {
                ToolTip toolTip = new ToolTip()
                        .withContentView(LayoutInflater.from(this).inflate(R.layout.custom_tooltip, null))
                        .withColor(Color.WHITE)
                        .withTextColor(Color.BLACK)
                        .withShadow()
                        .withAnimationType(ToolTip.AnimationType.NONE);
                myToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarNameOption);
                if (myToolTipView != null) {
                    myToolTipView.setOnToolTipViewClickedListener(toolTipView -> {
                        toolTipView.remove();
                        toolTipRelativeLayout.setVisibility(View.GONE);
                    });

                    toolTipRelativeLayout.setOnClickListener(v -> {
                        myToolTipView.remove();
                        toolTipRelativeLayout.setVisibility(View.GONE);
                    });

                    mActivityBinding.mainImageToPdfDefaultLayout.toolbar.layoutToolbar.setOnClickListener(v -> {
                        myToolTipView.remove();
                        toolTipRelativeLayout.setVisibility(View.GONE);
                    });

                } else {
                    toolTipRelativeLayout.setVisibility(View.GONE);
                }
            } else {
                toolTipRelativeLayout.setVisibility(View.GONE);
            }
            DataManager.getInstance(this).setShowGuideConvertDone();
        } else {
            toolTipRelativeLayout.setVisibility(View.GONE);
        }
    }

    private void setForSelectLayout() {
        mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarNameTv.setText(getString(R.string.image_to_pdf));
        mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mFileListSelectorAdapter = new ImageListSelectAdapter(this);

        mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarNameOption.setOnClickListener(view -> {
            if (myToolTipView != null) {
                myToolTipView.remove();
            }
            toolTipRelativeLayout.setVisibility(View.GONE);
            showConverterPopup(0);
        });
        mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarNameTv.setOnClickListener(view -> {
            if (myToolTipView != null) {
                myToolTipView.remove();
            }
            toolTipRelativeLayout.setVisibility(View.GONE);
            showConverterPopup(0);
        });

        mActivityBinding.mainImageToPdfDefaultLayout.importFileBtnImport.setOnClickListener(view -> {
            List<Integer> selectedIndex = mFileListSelectorAdapter.getSelectedList();

            ArrayList<ImageData> selectedList = new ArrayList<>();
            for (int i = 0; i < selectedIndex.size(); i++) {
                String path = mListFileSelector.get(selectedIndex.get(i)).getImagePath();

                if (path != null && (path.contains("content:/") || FileUtils.checkFileExist(path))) {
                    ImageData copyImageData = new ImageData();
                    copyImageData.setImagePath(path);
                    copyImageData.setId(mListFileSelector.get(selectedIndex.get(i)).getId());
                    selectedList.add(copyImageData);
                }
            }

            if (selectedList.size() > 0) {
                mImageToPdfViewModel.removeAllImage();
                mImageAdapter.clearData();

                mImageToPdfViewModel.addNewListImage(this, selectedList);
                addNewImagesToListView();
            } else {
                ToastUtils.showMessageShort(this, getString(R.string.image_to_pdf_nothing_to_clear));
            }
        });

        requestForFileSelector();
    }

    private void requestForFileSelector() {
        if (notHaveStoragePermission()) {
            showPermissionIssueArea();
            mActivityBinding.mainImageToPdfDefaultLayout.importFileBtnImport.setVisibility(View.GONE);
            mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarActionFolder.setVisibility(View.GONE);
        } else {
            mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.dataListArea.setLayoutManager(new GridLayoutManager(this, 3));
            mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.dataListArea.setAdapter(mFileListSelectorAdapter);

            startRequestForFileList(true);
            mActivityBinding.mainImageToPdfDefaultLayout.importFileBtnImport.setVisibility(View.VISIBLE);
            mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarActionFolder.setVisibility(View.VISIBLE);

            mActivityBinding.mainImageToPdfDefaultLayout.toolbar.toolbarActionFolder.setOnClickListener(v -> {
                startFileExplorerActivity();
            });
        }
    }

    private void showPermissionIssueArea() {
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noPermissionArea.setOnClickListener(v -> {
            startRequestPermissionForFileSelector();
        });
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.VISIBLE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.dataListArea.setVisibility(View.GONE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noDataErrorArea.setVisibility(View.GONE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.loadingArea.setVisibility(View.VISIBLE);
        mActivityBinding.mainImageToPdfDefaultLayout.fileSelectorLayout.noPermissionArea.setVisibility(View.GONE);
    }

    private void startRequestPermissionForFileSelector() {
        if (notHaveStoragePermission()) {
            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR);
        } else {
            requestForFileSelector();
        }
    }

    private void startRequestForFileList(boolean needShowLoading) {
        if (needShowLoading) {
            showLoadingArea();
        }
        mImageToPdfViewModel.getListLocalImage().observe(this, this::updateListFileSelector);
        mImageToPdfViewModel.startGetLocalImage();
    }

    private void updateListFileSelector(List<ImageData> fileDataList) {
        if (fileDataList != null && fileDataList.size() > 0 && mListFileSelector == fileDataList) {
            return;
        }

        mListFileSelector = new ArrayList<>();

        mListFileSelector.add(0, new ImageData());
        mListFileSelector.addAll(fileDataList);
        mFileListSelectorAdapter.setData(mListFileSelector);

        updateNumberSelected();
        showDataArea();
    }

    @Override
    public void onClickItem(int position) {
        if (position == 0) {
            startCameraActivity(false);
        } else {
            mFileListSelectorAdapter.revertData(position);
            updateNumberSelected();
        }
    }

    private void setForListLayout() {
        fetchDefaultOption();
        mActivityBinding.mainImageToPdfListLayout.toolbar.toolbarNameTv.setText(getString(R.string.image_to_pdf));
        mActivityBinding.mainImageToPdfListLayout.toolbar.toolbarBtnBack.setOnClickListener(view -> onBackPressed());

        mActivityBinding.mainImageToPdfListLayout.setVariable(BR.imageToPdfViewModel, mImageToPdfViewModel);
        mActivityBinding.mainImageToPdfListLayout.layoutCreatePdfFile.setOnClickListener((v) -> {
            checkPermissionBeforeCreateFile();
        });

        mActivityBinding.mainImageToPdfListLayout.recyclerView.setLayoutManager(new NqGridLayoutManager(this, ImageAdapter.NUMBER_COLUMN));
        mActivityBinding.mainImageToPdfListLayout.recyclerView.addItemDecoration(new GridSpacingItemDecoration(ImageAdapter.NUMBER_COLUMN,
                DeminUtils.dpToPx(6, getApplicationContext()), true));
        mActivityBinding.mainImageToPdfListLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
        mImageAdapter = new ImageAdapter(mImageToPdfViewModel, this::startActivityForResult, this::onAddImage);
        mActivityBinding.mainImageToPdfListLayout.recyclerView.setAdapter(mImageAdapter);
        ImageToPdfTouchCallback callback = new ImageToPdfTouchCallback(mImageAdapter.getItemTouchListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mActivityBinding.mainImageToPdfListLayout.recyclerView);
    }

    @Override
    public void onBackPressed() {
        if (mImageToPdfViewModel.getListImage().getValue() != null && mImageToPdfViewModel.getListImage().getValue().size() > 0) {
            SweetAlertDialog confirm = DialogFactory.getDialogConfirm(this, getString(R.string.confirm_exit_image_to_pdf));
            confirm.setConfirmClickListener(sweetAlertDialog -> {
               sweetAlertDialog.dismiss();

                mImageToPdfViewModel.removeAllImage();
                mImageAdapter.clearData();
            });
            confirm.show();
            return;
        }

        super.onBackPressed();
    }

    private void fetchDefaultOption() {
        mImageToPDFOptions = new ImageToPDFOptions();

        mImageToPDFOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.IMAGE_FILE_PREFIX_NAME));
        mImageToPDFOptions.setPassword("");
        mImageToPDFOptions.setPasswordProtected(false);
        mImageToPDFOptions.setMarginBottom(0);
        mImageToPDFOptions.setMarginTop(0);
        mImageToPDFOptions.setMarginLeft(0);
        mImageToPDFOptions.setMarginRight(0);
        mImageToPDFOptions.setPageNumStyle("");
        mImageToPDFOptions.setImageScaleType(ImageToPdfConstants.SCALE_TYPE[0]);
        mImageToPDFOptions.setPageSize(ImageToPdfConstants.PAGE_SIZE_TYPE[0]);
    }

    private void onAddImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_FOR_ADD_IMAGE_REQUEST);
    }

    private void startCreatePdfActivity() {
        if (mImageToPdfViewModel.getListImage() != null && mImageToPdfViewModel.getListImage().getValue().size() > 0) {

            if (mImageToPDFOptions != null && mImageToPDFOptions.getOutFileName() != null && mImageToPDFOptions.getOutFileName().startsWith(DataConstants.IMAGE_FILE_PREFIX_NAME)) {
                mImageToPDFOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.IMAGE_FILE_PREFIX_NAME));
            } else if (mImageToPDFOptions == null) {
                fetchDefaultOption();
            } else if (mImageToPDFOptions.getOutFileName() == null || mImageToPDFOptions.getOutFileName().length() == 0) {
                mImageToPDFOptions.setOutFileName(FileUtils.getDefaultFileName(DataConstants.IMAGE_FILE_PREFIX_NAME));
            }

            SettingImageToPdfDialog settingImageToPdfDialog = new SettingImageToPdfDialog(mImageToPDFOptions, this);
            settingImageToPdfDialog.show(getSupportFragmentManager(), settingImageToPdfDialog.getTag());
        }
    }

    public void startFileExplorerActivity() {
        if (DataManager.getInstance(this).getShowGuideSelectMulti()) {
            DataManager.getInstance(this).setShowGuideSelectMultiDone();

            SweetAlertDialog dialogNotice = DialogFactory.getDialogNotice(this, getString(R.string.guide_to_select_multiple));
            dialogNotice.setCanceledOnTouchOutside(false);
            dialogNotice.setCancelable(false);
            dialogNotice.setConfirmClickListener(sweetAlertDialog -> {
                dialogNotice.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            });
            dialogNotice.show();

        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
    }

    public void startCameraActivity(boolean isFromScanButton) {
        Intent cameraImgIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ResolveInfo cameraPackage = getCameraPackage();
        if (cameraPackage != null) {
            cameraImgIntent.setPackage(cameraPackage.activityInfo.packageName);
        }

        File imageFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File newFile;
        try {
            newFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".jpg", imageFile);
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", newFile);
            mCurrentPhotoPath = uri.toString();
            cameraImgIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraImgIntent, isFromScanButton ? SCAN_REQUEST : CAMERA_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ResolveInfo getCameraPackage() {
        try {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            ResolveInfo cameraInfo  = null;
            PackageManager pm = getPackageManager();

            List<ResolveInfo> pkgList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if(pkgList.size() > 0) {
                cameraInfo = pkgList.get(0);
            }
            return cameraInfo;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_CREATE_PDF_ACTIVITY) {
            if (resultCode == RESULT_NEED_FINISH) {
                finish();
                return;
            } else {
                if (mImageToPDFOptions != null) {
                    mImageToPDFOptions.setWatermark("");
                    mImageToPDFOptions.setWatermarkAdded(false);
                } else {
                    fetchDefaultOption();
                }
            }
        }
        if (resultCode != RESULT_OK) {
            if (requestCode == SCAN_REQUEST && mIsNeedScan) {
                finish();
            }
            return;
        }
        if (requestCode == CAMERA_REQUEST || requestCode == SCAN_REQUEST) {
            if (mListFileSelector.size() == 0) {
                mListFileSelector = new ArrayList<>();
                mListFileSelector.add(0, new ImageData());
            }

            ImageData imageData = new ImageData(mCurrentPhotoPath, "", 0, System.nanoTime());
            mListFileSelector.add(1, imageData);
            mFileListSelectorAdapter.addToFirstPosition(imageData);

            updateNumberSelected();
        } else if (requestCode == PICK_IMAGE_REQUEST && data != null) {
            if (data.getClipData() != null) {
                int mNewImageCount = data.getClipData().getItemCount();
                if (mNewImageCount == 0) return;
                ArrayList<ImageData> arrayList = new ArrayList<>();
                for (int i = 0; i < mNewImageCount; i++) {
                    try {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        if (imageUri != null) {
                            String imagePath = RealPathUtil.getInstance().getRealPath(this, imageUri, FileUtils.FileType.type_IMAGE);
                            if (FileUtils.checkFileExist(imagePath)) {
                                arrayList.add(new ImageData(imagePath, "", 0, System.nanoTime()));
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (arrayList.size() > 0) {
                    for (int i = arrayList.size() - 1; i>=0; i--) {
                        mListFileSelector.add(1, arrayList.get(i));
                        mFileListSelectorAdapter.addToFirstPosition(arrayList.get(i));
                    }
                }

                updateNumberSelected();
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                String imagePath = RealPathUtil.getInstance().getRealPath(this, imageUri, FileUtils.FileType.type_IMAGE);
                if (FileUtils.checkFileExist(imagePath)) {
                    ImageData imageData  = new ImageData(imagePath, "", 0, System.nanoTime());
                    mListFileSelector.add(1, imageData);
                    mFileListSelectorAdapter.addToFirstPosition(imageData);
                    updateNumberSelected();
                }
            }
        } else if (requestCode == PICK_IMAGE_FOR_ADD_IMAGE_REQUEST && data != null) {
            if (data.getClipData() != null) {
                int mNewImageCount = data.getClipData().getItemCount();
                if (mNewImageCount == 0) return;
                ArrayList<ImageData> arrayList = new ArrayList<>();
                for (int i = 0; i < mNewImageCount; i++) {
                    try {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        if (imageUri != null) {
                            String imagePath = RealPathUtil.getInstance().getRealPath(this, imageUri, FileUtils.FileType.type_IMAGE);
                            if (FileUtils.checkFileExist(imagePath)) {
                                arrayList.add(new ImageData(imagePath, "", 0, System.nanoTime()));
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                mImageToPdfViewModel.addNewListImage(this, arrayList);
                addNewImagesToListView();
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                String imagePath = RealPathUtil.getInstance().getRealPath(this, imageUri, FileUtils.FileType.type_IMAGE);
                if (FileUtils.checkFileExist(imagePath)) {
                    mImageToPdfViewModel.addNewImage(this, new ImageData(imagePath, "", 0, System.nanoTime()));
                    addNewImageToListView();
                }
            }
        } else if (requestCode == ImageToPdfActivity.CROP_IMAGE_CODE) {
            String path;
            if (data != null) {
                path = data.getStringExtra(ImageAdapter.INTENT_DATA_IMAGE);
                int position = data.getIntExtra(ImageAdapter.ADAPTER_POSITION, -1);
                if (!TextUtils.isEmpty(path) && position >= 0) {
                    ArrayList<ImageData> imageDatas = mImageToPdfViewModel.getListImage().getValue();
                    if (imageDatas != null && imageDatas.size() > position && imageDatas.get(position) != null) {
                        imageDatas.get(position).setImagePath(path);
                        mImageToPdfViewModel.getListImage().setValue(imageDatas);
                        mImageAdapter.setImageData(imageDatas);
                        mImageAdapter.notifyItemChanged(position);
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateNumberSelected() {
        int numberSelected = (mFileListSelectorAdapter == null ? 0 : mFileListSelectorAdapter.getNumberSelectedFile());
        mActivityBinding.mainImageToPdfDefaultLayout.importFileBtnImport.setText(getString(R.string.import_file) + " (" + numberSelected + ")");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_CREATE_FILE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.create_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        startCreatePdfActivity();
                        sweetAlertDialog.dismiss();
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.couldnt_create_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                }
                break;

            case REQUEST_EXTERNAL_PERMISSION_FOR_FILE_SELECTOR:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    requestForFileSelector();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsRequestFullPermission) {
            mIsRequestFullPermission = false;

            if (mRequestFullPermissionCode == REQUEST_EXTERNAL_PERMISSION_FOR_CREATE_FILE) {
                if (!notHaveStoragePermission()) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.create_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        startCreatePdfActivity();
                        sweetAlertDialog.dismiss();
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.couldnt_create_file_now));
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

    private void checkPermissionBeforeCreateFile() {
        if (notHaveStoragePermission()) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_create_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_CREATE_FILE);
            });
            mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                sweetAlertDialog.setContentText(getString(R.string.couldnt_create_file_now));
                sweetAlertDialog.setConfirmClickListener(Dialog::dismiss);
                sweetAlertDialog.showCancelButton(false);
                sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
            });
            mRequestPermissionDialog.show();
        } else {
            startCreatePdfActivity();
        }
    }

    private void addNewImageToListView() {
        int position = mImageAdapter.getItemCount();
        mImageAdapter.setImageData(mImageToPdfViewModel.getListImage().getValue());
        if (position > 0) position = position - 1;
        mImageAdapter.notifyDataSetChanged();
    }

    private void addNewImagesToListView() {
        mImageAdapter.setImageData(mImageToPdfViewModel.getListImage().getValue());
        mImageAdapter.notifyDataSetChanged();
    }

    public void removeImage(ImageData imageData, int position) {
        mImageToPdfViewModel.removeImage(imageData);
        mImageAdapter.setImageData(mImageToPdfViewModel.getListImage().getValue());
        mImageAdapter.notifyItemRemoved(position);
        mImageAdapter.notifyItemRangeChanged(position, mImageAdapter.getItemCount());
    }

    @Override
    protected void onDestroy() {
        mImageToPdfViewModel.deleteFolder(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        mImageAdapter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void submitForm(ImageToPDFOptions options) {
        if (mImageToPdfViewModel.getListImage().getValue() == null || mImageToPdfViewModel.getListImage().getValue().size() == 0) {
            return;
        }

        showDoneAdsBeforeAction(() -> {
            mImageToPDFOptions = options;

            ArrayList<String> listPath = new ArrayList<>();
            Intent intent = new Intent(this, ImageToPdfDoneActivity.class);
            for (ImageData imageData : mImageToPdfViewModel.getListImage().getValue()) {
                listPath.add(imageData.getImagePath());
            }
            mImageToPDFOptions.setImagesUri(listPath);
            Gson gson = new Gson();
            String json = gson.toJson(mImageToPDFOptions);
            intent.putExtra(ImageToPdfDoneActivity.INTENT_PDF_OPTION, json);
            startActivityForResult(intent, OPEN_CREATE_PDF_ACTIVITY);
        });
    }

    interface StartActivityInterface {
        void startActivityForResult(Intent intent, int requestCode);
    }
}