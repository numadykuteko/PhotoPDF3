package com.pdfconverter.jpg2pdf.pdf.converter.ui.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.addwatermark.AddWaterMarkActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf.EditPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage.PdfToImageActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext.PdfToTextActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.split.SplitPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.SnackBarUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;

public abstract class BaseFragment<T extends ViewDataBinding, V extends BaseViewModel> extends Fragment {

    protected BaseBindingActivity mActivity;
    private View mRootView;
    private T mViewDataBinding;
    private V mViewModel;

    protected boolean mIsRequestFullPermission = false;
    protected int mRequestFullPermissionCode = -1;

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * do something need
     */
    public abstract
    void reloadEasyChangeData();

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseBindingActivity) {
            BaseBindingActivity activity = (BaseBindingActivity) context;
            this.mActivity = activity;
            activity.onFragmentAttached();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        mRootView = mViewDataBinding.getRoot();
        return mRootView;
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.setLifecycleOwner(this);
        mViewDataBinding.executePendingBindings();
    }

    public BaseBindingActivity getBaseActivity() {
        return mActivity;
    }

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    public boolean isNetworkConnected() {
        return mActivity != null && mActivity.isNetworkConnected();
    }


    public interface Callback {

        void onFragmentAttached();

        void onFragmentDetached(String tag);
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity() != null) {
            return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void requestReadStoragePermissionsSafely(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        }
    }

    protected void extractToImagePdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(mActivity, mActivity.getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(mActivity, PdfToImageActivity.class);
        intent.putExtra(mActivity.EXTRA_FILE_PATH, filePath);
        mActivity.startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(mActivity, "Pdf To Image", "From function");
    }

    protected void extractToTextPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(mActivity, mActivity.getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(mActivity, PdfToTextActivity.class);
        intent.putExtra(mActivity.EXTRA_FILE_PATH, filePath);
        mActivity.startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(mActivity, "Pdf To Text", "From function");
    }

    protected void addWatermarkPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(mActivity, mActivity.getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(mActivity, AddWaterMarkActivity.class);
        intent.putExtra(mActivity.EXTRA_FILE_PATH, filePath);
        mActivity.startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(mActivity, "Add watermark Pdf", "From function");
    }

    protected void editPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(mActivity, mActivity.getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(mActivity, EditPdfActivity.class);
        intent.putExtra(mActivity.EXTRA_FILE_PATH, filePath);
        mActivity.startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(mActivity, "Edit Pdf", "From function");
    }

    protected void splitPdf(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            ToastUtils.showMessageShort(mActivity, mActivity.getString(R.string.add_watermark_file_is_encrypted_before));
            return;
        }

        Intent intent = new Intent(mActivity, SplitPdfActivity.class);
        intent.putExtra(mActivity.EXTRA_FILE_PATH, filePath);
        mActivity.startActivity(intent);

        FirebaseUtils.sendEventFunctionUsed(mActivity, "Split Pdf", "From function");
    }

    protected void removePasswordPdf(String filePath) {
        mActivity.gotoUnlockPasswordActivity(filePath, null);
    }

    protected void setPasswordPdf(String filePath) {
        mActivity.gotoProtectPasswordActivity(filePath);
    }

    protected void optionBookmarkPdf(String filePath, boolean isAdd) {
        if (isAdd) {
            getViewModel().saveBookmarked(filePath);
            SnackBarUtils.getSnackbar(mActivity, mActivity.getString(R.string.bookmark_save_success)).show();
        } else {
            getViewModel().clearBookmarked(filePath);
            SnackBarUtils.getSnackbar(mActivity, mActivity.getString(R.string.bookmark_remove_success)).show();
        }
    }

    protected void printPdfFile(String filePath) {
        if (PdfUtils.isPDFEncrypted(filePath)) {
            SnackBarUtils.getSnackbar(mActivity, mActivity.getString(R.string.view_pdf_can_not_print_protected_file)).show();
            return;
        }
        FileUtils.printFile(mActivity, new File(filePath));
    }

}