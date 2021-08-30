package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;
import java.util.Objects;

public class SettingTextToPdfDialog extends BottomSheetDialogFragment {
    private static final String TAG = "SettingTextToPdfDialog";

    private OnDialogSubmit mListener;
    private TextToPDFOptions mOptions;

    private EditText mNameFile;
    private EditText mFontSize;
    private TextView mFontFamily;
    private TextView mPageSize;
    private Button mCancelButton;
    private Button mSubmitButton;

    private String mSelectedFontFamily;
    private String mSelectedPageSize;

    public SettingTextToPdfDialog() {
        // Required empty public constructor
    }

    public SettingTextToPdfDialog(TextToPDFOptions options, OnDialogSubmit listener) {
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

        View v = inflater.inflate(R.layout.dialog_setting_text_to_pdf, container, false);
        mNameFile = v.findViewById(R.id.import_file_file_name);
        mFontSize = v.findViewById(R.id.import_file_font_size);
        mFontFamily = v.findViewById(R.id.import_file_font_family);
        mPageSize = v.findViewById(R.id.import_file_page_size);
        mCancelButton = v.findViewById(R.id.btn_cancel);
        mSubmitButton = v.findViewById(R.id.btn_convert);

        if (mOptions == null) {
            ToastUtils.showSystemIssueToast(getContext());
            dismiss();
        } else {
            mNameFile.setText(mOptions.getOutFileName());
            mFontSize.setText("" + mOptions.getFontSize());

            int indexOfFontFamily = 0;
            for (int i = 0; i < OptionConstants.LIST_FONT_FAMILY.length; i++) {
                if (OptionConstants.LIST_FONT_FAMILY[i].equals(mOptions.getFontFamily())) {
                    indexOfFontFamily = i;
                }
            }
            mSelectedFontFamily = OptionConstants.LIST_FONT_FAMILY_NAME[indexOfFontFamily];
            mFontFamily.setText(mSelectedFontFamily);

            mSelectedPageSize = mOptions.getPageSize();
            mPageSize.setText(mSelectedPageSize);

            mPageSize.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.page_size), OptionConstants.LIST_PAGE_SIZE, mSelectedPageSize, newOption -> {
                        mSelectedPageSize = OptionConstants.LIST_PAGE_SIZE[newOption];
                        mPageSize.setText(mSelectedPageSize);
                    });
                    dataOptionDialog.show();
                }
            });

            mFontFamily.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.font_family), OptionConstants.LIST_FONT_FAMILY_NAME, mSelectedFontFamily, newOption -> {
                        mSelectedFontFamily = OptionConstants.LIST_FONT_FAMILY_NAME[newOption];
                        mFontFamily.setText(mSelectedFontFamily);
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
                    int indexOfFont = 0;
                    for (int i = 0; i < OptionConstants.LIST_FONT_FAMILY_NAME.length; i++) {
                        if (OptionConstants.LIST_FONT_FAMILY_NAME[i].equals(mSelectedFontFamily)) {
                            indexOfFont = i;
                            break;
                        }
                    }

                    mOptions.setFontFamily(OptionConstants.LIST_FONT_FAMILY[indexOfFont]);

                    CharSequence textFontSize = mFontSize.getText();
                    int outputFontSize = OptionConstants.DEFAULT_FONT_SIZE;
                    if (textFontSize != null) {
                        try {
                            outputFontSize = Integer.parseInt(textFontSize.toString());
                        } catch (Exception ignored) {
                        }
                    }

                    mOptions.setFontSize(outputFontSize);
                    mOptions.setPageSize(mSelectedPageSize);

                    mOptions.setPasswordProtected(false);

                    String fileName = mOptions.getOutFileName() + ImageToPdfConstants.pdfExtension;
                    File dir = DirectoryUtils.getDefaultStorageFile();

                    File file = new File(dir, fileName);
                    if (FileUtils.checkFileExist(file.getAbsolutePath())) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "Warning", getContext().getString(R.string.confirm_override_file), new ConfirmDialog.ConfirmListener() {
                            @Override
                            public void onSubmit() {
                                SettingTextToPdfDialog.this.dismiss();
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

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        mNameFile.clearFocus();
        CommonUtils.hideKeyboard(getActivity());

        super.onDismiss(dialog);
    }

    public interface OnDialogSubmit {
        public void submitForm(TextToPDFOptions textToPDFOptions);
    }
}
