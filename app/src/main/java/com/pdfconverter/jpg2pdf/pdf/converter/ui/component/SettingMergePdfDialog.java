package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.MergePDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;

public class SettingMergePdfDialog extends BottomSheetDialogFragment {
    private OnDialogSubmit mListener;
    private MergePDFOptions mOptions;
    private EditText mNameFile;
    private Button mCancelButton;
    private Button mSubmitButton;

    public SettingMergePdfDialog() {
        // Required empty public constructor
    }

    public SettingMergePdfDialog(MergePDFOptions options, OnDialogSubmit listener) {
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

        View v = inflater.inflate(R.layout.dialog_setting_merge_pdf, container, false);
        mNameFile = v.findViewById(R.id.import_file_file_name);
        mCancelButton = v.findViewById(R.id.btn_cancel);
        mSubmitButton = v.findViewById(R.id.btn_convert);

        if (mOptions == null) {
            ToastUtils.showSystemIssueToast(getContext());
            dismiss();
        } else {
            mNameFile.setText(mOptions.getOutFileName());

            mCancelButton.setOnClickListener(view -> dismiss());

            mSubmitButton.setOnClickListener(view -> {
                String nameFile = mNameFile.getText().toString().trim();

                if (nameFile.length() == 0) {
                    ToastUtils.showMessageShort(getContext(), getString(R.string.please_input_name_text));
                    return;
                }

                if (mListener != null) {
                    mOptions.setOutFileName(nameFile);
                    mOptions.setPasswordProtected(false);

                    String fileName = mOptions.getOutFileName() + ImageToPdfConstants.pdfExtension;
                    File dir = DirectoryUtils.getDefaultStorageFile();

                    File file = new File(dir, fileName);
                    if (FileUtils.checkFileExist(file.getAbsolutePath())) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "Warning", getContext().getString(R.string.confirm_override_file), new ConfirmDialog.ConfirmListener() {
                            @Override
                            public void onSubmit() {
                                SettingMergePdfDialog.this.dismiss();
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
        void submitForm(MergePDFOptions mOptions);
    }
}
