package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;
import java.util.Objects;

public class SettingExcelToPdfDialog extends BottomSheetDialogFragment {
    private OnDialogSubmit mListener;
    private ExcelToPDFOptions mOptions;
    private EditText mNameFile;
    private CheckBox mCheckBox;
    private TextView mCheckBoxTv;
    private TextView mOrientation;
    private TextView mPageSize;
    private Button mCancelButton;
    private Button mSubmitButton;

    private String mSelectedPageSize;
    private String mSelectedOrientation;

    public SettingExcelToPdfDialog() {
        // Required empty public constructor
    }

    public SettingExcelToPdfDialog(ExcelToPDFOptions options, OnDialogSubmit listener) {
        this.mListener = listener;
        this.mOptions = options;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.sheet_dialog_style);
    }

    private void setAutoExpanded() {
        if (getDialog() != null) {
            getDialog().setOnShowListener(dialog -> {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheetInternal != null) {
                    BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setAutoExpanded();

        View v = inflater.inflate(R.layout.dialog_setting_excel_to_pdf, container, false);
        mNameFile = v.findViewById(R.id.import_file_file_name);
        mCheckBox = v.findViewById(R.id.import_file_check_one_sheet);
        mCheckBoxTv = v.findViewById(R.id.import_file_one_sheet_tv);
        mOrientation = v.findViewById(R.id.import_file_orientation);
        mPageSize = v.findViewById(R.id.import_file_page_size);
        mCancelButton = v.findViewById(R.id.btn_cancel);
        mSubmitButton = v.findViewById(R.id.btn_convert);

        if (mOptions == null) {
            ToastUtils.showSystemIssueToast(getContext());
            dismiss();
        } else {
            mNameFile.setText(mOptions.getOutFileName());
            mCheckBox.setChecked(mOptions.isOneSheetOnePage());
            mCheckBoxTv.setOnClickListener(view -> {
                mCheckBox.setChecked(!mCheckBox.isChecked());
            });

            mSelectedOrientation = mOptions.getPageOrientation();
            mOrientation.setText(mSelectedOrientation);

            int indexOfPageSize = 0;
            for (int i = 0 ; i < OptionConstants.LIST_PAGE_SIZE_EXCEL_VALUE.length; i++) {
                if (OptionConstants.LIST_PAGE_SIZE_EXCEL_VALUE[i] == mOptions.getPageSize()) {
                    indexOfPageSize = i;
                    break;
                }
            }

            mSelectedPageSize = OptionConstants.LIST_PAGE_SIZE[indexOfPageSize];
            mPageSize.setText(mSelectedPageSize);

            mPageSize.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.page_size), OptionConstants.LIST_PAGE_SIZE_EXCEL, mSelectedPageSize, newOption -> {
                        mSelectedPageSize = OptionConstants.LIST_PAGE_SIZE_EXCEL[newOption];
                        mPageSize.setText(mSelectedPageSize);
                    });
                    dataOptionDialog.show();
                }
            });

            mOrientation.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.orientation), OptionConstants.LIST_PAGE_ORIENTATION, mSelectedOrientation, newOption -> {
                        mSelectedOrientation = OptionConstants.LIST_PAGE_ORIENTATION[newOption];
                        mOrientation.setText(mSelectedOrientation);
                    });
                    dataOptionDialog.show();
                }
            });

            mCancelButton.setOnClickListener(view -> dismiss());

            mSubmitButton.setOnClickListener(view -> {
                String nameFile = mNameFile.getText().toString().trim();
                if (nameFile.length() == 0) {
                    ToastUtils.showMessageShort(getContext(), getString(R.string.please_input_name_text));
                    return;
                }

                if (mListener != null) {
                    mOptions.setOutFileName(nameFile);

                    mOptions.setOneSheetOnePage(mCheckBox.isChecked());
                    mOptions.setPageOrientation(mSelectedPageSize);

                    int pageSizeIndex = 0;
                    for (int i = 0 ; i < OptionConstants.LIST_PAGE_SIZE_EXCEL.length; i++) {
                        if (OptionConstants.LIST_PAGE_SIZE_EXCEL[i].equals(mSelectedPageSize)){
                            pageSizeIndex = i;
                            break;
                        }
                    }

                    mOptions.setPageSize(OptionConstants.LIST_PAGE_SIZE_EXCEL_VALUE[pageSizeIndex]);
                    mOptions.setPasswordProtected(false);

                    String fileName = mOptions.getOutFileName() + ImageToPdfConstants.pdfExtension;
                    File dir = DirectoryUtils.getDefaultStorageFile();

                    File file = new File(dir, fileName);
                    if (FileUtils.checkFileExist(file.getAbsolutePath())) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "Warning", getContext().getString(R.string.confirm_override_file), new ConfirmDialog.ConfirmListener() {
                            @Override
                            public void onSubmit() {
                                SettingExcelToPdfDialog.this.dismiss();
                                mListener.submitForm(mOptions);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        confirmDialog.show();
                    } else {
                        dismiss();
                        mListener.submitForm(mOptions);
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        mNameFile.clearFocus();
        CommonUtils.hideKeyboard(getActivity());

        super.onDismiss(dialog);
    }

    public interface OnDialogSubmit {
        void submitForm(ExcelToPDFOptions mOptions);
    }
}
