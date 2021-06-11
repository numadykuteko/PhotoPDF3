package com.pdfconverter.jpg2pdf.pdf.converter.ui.more;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.NewPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.FragmentMoreBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ItemHomeFunctionBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingNewPdfDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.formatpdf.FormatPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MoreFragment extends BaseFragment<FragmentMoreBinding, MoreViewModel> implements MoreNavigator {
    private MoreViewModel mMoreViewModel;
    private FragmentMoreBinding mFragmentDiscoversBinding;

    private final ArrayList<ItemHomeFunctionBinding> mFunctionLayoutList = new ArrayList<>();
    private final ArrayList<Integer> mFunctionNameList = new ArrayList<>();
    private final ArrayList<Integer> mFunctionIconList = new ArrayList<>();

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_FUNCTION = 1;
    private SweetAlertDialog mRequestPermissionDialog;
    private NewPDFOptions mNewPDFOptions;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_more;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    @Override
    public MoreViewModel getViewModel() {
        mMoreViewModel = ViewModelProviders.of(this).get(MoreViewModel.class);
        return mMoreViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMoreViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentDiscoversBinding = getViewDataBinding();

        initCommonView();
        initView();
        setForClick();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static MoreFragment newInstance() {
        MoreFragment homeFragment = new MoreFragment();

        Bundle args = new Bundle();
        homeFragment.setArguments(args);
        homeFragment.setRetainInstance(true);

        return homeFragment;
    }

    private void initCommonView() {
        mFunctionLayoutList.add(AppConstants.FLAG_IMAGE_TO_PDF, mFragmentDiscoversBinding.imageToPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_WORD_TO_PDF, mFragmentDiscoversBinding.wordToPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_EXCEL_TO_PDF, mFragmentDiscoversBinding.excelToPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_NEW_PDF, mFragmentDiscoversBinding.newPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_PDF_TO_IMAGE, mFragmentDiscoversBinding.pdfToImage);
        mFunctionLayoutList.add(AppConstants.FLAG_PDF_TO_TEXT, mFragmentDiscoversBinding.pdfToText);
        mFunctionLayoutList.add(AppConstants.FLAG_EDIT_PDF, mFragmentDiscoversBinding.editPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_ADD_WATERMARK, mFragmentDiscoversBinding.addWaterMark);
        mFunctionLayoutList.add(AppConstants.FLAG_PROTECT_PDF, mFragmentDiscoversBinding.protectPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_UNLOCK_PDF, mFragmentDiscoversBinding.unlockPdf);
        mFunctionLayoutList.add(AppConstants.FLAG_MERGE_PDF, mFragmentDiscoversBinding.mergePdf);
        mFunctionLayoutList.add(AppConstants.FLAG_SPLIT_PDF, mFragmentDiscoversBinding.splitPdf);

        mFunctionIconList.add(AppConstants.FLAG_IMAGE_TO_PDF, R.drawable.ic_tool_img_to_pdf);
        mFunctionIconList.add(AppConstants.FLAG_WORD_TO_PDF, R.drawable.ic_tool_text_to_pdf);
        mFunctionIconList.add(AppConstants.FLAG_EXCEL_TO_PDF, R.drawable.ic_tool_pdf_to_excel);
        mFunctionIconList.add(AppConstants.FLAG_NEW_PDF, R.drawable.ic_tool_view_pdf);
        mFunctionIconList.add(AppConstants.FLAG_PDF_TO_IMAGE, R.drawable.ic_tool_pdf_to_img);
        mFunctionIconList.add(AppConstants.FLAG_PDF_TO_TEXT, R.drawable.ic_tool_pdf_to_text);
        mFunctionIconList.add(AppConstants.FLAG_EDIT_PDF, R.drawable.ic_tool_edit);
        mFunctionIconList.add(AppConstants.FLAG_ADD_WATERMARK, R.drawable.ic_tool_add_wm);
        mFunctionIconList.add(AppConstants.FLAG_PROTECT_PDF, R.drawable.ic_tool_protect);
        mFunctionIconList.add(AppConstants.FLAG_UNLOCK_PDF, R.drawable.ic_tool_unlock);
        mFunctionIconList.add(AppConstants.FLAG_MERGE_PDF, R.drawable.ic_tool_merge);
        mFunctionIconList.add(AppConstants.FLAG_SPLIT_PDF, R.drawable.ic_tool_split);

        mFunctionNameList.add(AppConstants.FLAG_IMAGE_TO_PDF, R.string.image_to_pdf);
        mFunctionNameList.add(AppConstants.FLAG_WORD_TO_PDF, R.string.word_to_pdf);
        mFunctionNameList.add(AppConstants.FLAG_EXCEL_TO_PDF, R.string.excel_to_pdf);
        mFunctionNameList.add(AppConstants.FLAG_NEW_PDF, R.string.new_pdf);
        mFunctionNameList.add(AppConstants.FLAG_PDF_TO_IMAGE, R.string.pdf_to_image);
        mFunctionNameList.add(AppConstants.FLAG_PDF_TO_TEXT, R.string.pdf_to_text);
        mFunctionNameList.add(AppConstants.FLAG_EDIT_PDF, R.string.edit_pdf);
        mFunctionNameList.add(AppConstants.FLAG_ADD_WATERMARK, R.string.add_watermark);
        mFunctionNameList.add(AppConstants.FLAG_PROTECT_PDF, R.string.protect_pdf);
        mFunctionNameList.add(AppConstants.FLAG_UNLOCK_PDF, R.string.unlock_pdf);
        mFunctionNameList.add(AppConstants.FLAG_MERGE_PDF, R.string.merge_pdf);
        mFunctionNameList.add(AppConstants.FLAG_SPLIT_PDF, R.string.split_pdf);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        if (mActivity != null) {
            Admod.getInstance().loadNativeFragment(mActivity, BuildConfig.native_id, mFragmentDiscoversBinding.nativeAds);
            mActivity.preloadTapFunctionAdsIfInit();
        }

        for (ItemHomeFunctionBinding itemFunction: mFunctionLayoutList) {
            int index = mFunctionLayoutList.indexOf(itemFunction);

            itemFunction.itemNameView.setText(getString(mFunctionNameList.get(index)));
            Drawable icon = mActivity.getDrawable(mFunctionIconList.get(index));
            itemFunction.itemImageView.setImageDrawable(icon);
            itemFunction.itemFunctionView.setOnClickListener(view -> {
                if (index != AppConstants.FLAG_NEW_PDF) {
                    if (mActivity != null) {
                        mActivity.gotoActivityWithFlag(index);
                    }
                } else {
                    if (mActivity.notHaveStoragePermission()) {
                        mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(mActivity, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
                        mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                            requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_FUNCTION);
                            sweetAlertDialog.dismiss();
                        });
                        mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                            sweetAlertDialog.setContentText(getString(R.string.reject_read_file));
                            sweetAlertDialog.setConfirmClickListener(Dialog::dismiss);
                            sweetAlertDialog.showCancelButton(false);
                            sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
                        });
                        mRequestPermissionDialog.show();

                        return;
                    }

                    if (mNewPDFOptions == null) {
                        mNewPDFOptions = new NewPDFOptions(0, FileUtils.getDefaultFileName(DataConstants.NEW_PDF_PREFIX_NAME), ImageToPdfConstants.DEFAULT_PAGE_SIZE, 1);
                    } else {
                        mNewPDFOptions.setFileName(FileUtils.getDefaultFileName(DataConstants.NEW_PDF_PREFIX_NAME));
                    }

                    SettingNewPdfDialog settingNewPdfDialog = new SettingNewPdfDialog(mNewPDFOptions, mOptions -> {
                        if (mOptions == null || mOptions.getFileName() == null || mOptions.getFileName().length() == 0 || mOptions.getNumberPage() < 1 || mOptions.getNumberPage() > 999) {
                            return;
                        }

                        mActivity.showTapFunctionAdsBeforeAction(() -> {
                            mNewPDFOptions = mOptions;

                            Intent imagePdfIntent = new Intent(mActivity, FormatPdfActivity.class);

                            Gson gson = new Gson();
                            String json = gson.toJson(mNewPDFOptions);
                            imagePdfIntent.putExtra(FormatPdfActivity.INTENT_PDF_OPTION, json);

                            startActivity(imagePdfIntent);

                            FirebaseUtils.sendEventFunctionUsed(mActivity, "Create new pdf", "From home");
                        });
                    });
                    settingNewPdfDialog.show(getChildFragmentManager(), settingNewPdfDialog.getTag());

                }
            });
        }
    }

    private void setForClick() {
    }
}
