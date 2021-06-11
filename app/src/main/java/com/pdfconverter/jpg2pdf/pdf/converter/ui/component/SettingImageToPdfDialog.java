package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;
import java.util.Objects;

public class SettingImageToPdfDialog extends BottomSheetDialogFragment {
    private OnDialogSubmit mListener;
    private ImageToPDFOptions mOptions;
    private EditText mNameFile;
    private EditText mWatermark;
    private TextView mScaleType;
    private TextView mPageSize;
    private TextView mPageNumber;

    private Button mCancelButton;
    private Button mSubmitButton;

    private String mSelectedScaleType;
    private String mSelectedPageSize;
    private String mSelectedPageNumber;

    public SettingImageToPdfDialog() {
        // Required empty public constructor
    }

    public SettingImageToPdfDialog(ImageToPDFOptions options, OnDialogSubmit listener) {
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

        View v = inflater.inflate(R.layout.dialog_setting_image_to_pdf, container, false);
        mNameFile = v.findViewById(R.id.import_file_file_name);
        mWatermark = v.findViewById(R.id.import_file_add_watermark);
        mScaleType = v.findViewById(R.id.import_file_scale_type);
        mPageNumber = v.findViewById(R.id.import_file_page_number);
        mPageSize = v.findViewById(R.id.import_file_page_size);

        mCancelButton = v.findViewById(R.id.btn_cancel);
        mSubmitButton = v.findViewById(R.id.btn_convert);

        if (mOptions == null) {
            ToastUtils.showSystemIssueToast(getContext());
            dismiss();
        } else {
            mNameFile.setText(mOptions.getOutFileName());
            mWatermark.setText(mOptions.getWatermark());

            mSelectedPageSize = mOptions.getPageSize();
            mPageSize.setText(mSelectedPageSize);
            mPageSize.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.page_size), ImageToPdfConstants.PAGE_SIZE_TYPE, mSelectedPageSize, newOption -> {
                        mSelectedPageSize = ImageToPdfConstants.PAGE_SIZE_TYPE[newOption];
                        mPageSize.setText(mSelectedPageSize);
                    });
                    dataOptionDialog.show();
                }
            });

            mSelectedScaleType = mOptions.getImageScaleType();
            mScaleType.setText(mSelectedScaleType);
            mScaleType.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.scale_type), ImageToPdfConstants.SCALE_TYPE, mSelectedScaleType, newOption -> {
                        mSelectedScaleType = ImageToPdfConstants.SCALE_TYPE[newOption];
                        mScaleType.setText(mSelectedScaleType);
                    });
                    dataOptionDialog.show();
                }
            });

            int indexOfPageNumber = 0;
            for (int i = 0; i < ImageToPdfConstants.PAGE_NUMBER_TYPE_VALUE.length; i++) {
                if (ImageToPdfConstants.PAGE_NUMBER_TYPE_VALUE[i].equals(mOptions.getPageSize())) {
                    indexOfPageNumber = i;
                    break;
                }
            }

            mSelectedPageNumber = ImageToPdfConstants.PAGE_NUMBER_TYPE[indexOfPageNumber];
            mPageNumber.setText(mSelectedPageNumber);

            mPageNumber.setOnClickListener(view -> {
                if (getContext() != null) {
                    DataOptionDialog dataOptionDialog = new DataOptionDialog(Objects.requireNonNull(getContext()), getString(R.string.page_number), ImageToPdfConstants.PAGE_NUMBER_TYPE, mSelectedPageNumber, newOption -> {
                        mSelectedPageNumber = ImageToPdfConstants.PAGE_NUMBER_TYPE[newOption];
                        mPageNumber.setText(mSelectedPageNumber);
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
                    String watermark = mWatermark.getText().toString().trim();
                    if (watermark != null && watermark.length() > 0) {
                        mOptions.setWatermark(watermark);
                        mOptions.setWatermarkAdded(true);
                    } else {
                        mOptions.setWatermark("");
                        mOptions.setWatermarkAdded(false);
                    }
                    mOptions.setPageSize(mSelectedPageSize);
                    mOptions.setImageScaleType(mSelectedScaleType);
                    mOptions.setMarginLeft(0);
                    mOptions.setMarginRight(0);
                    mOptions.setMarginTop(0);
                    mOptions.setMarginBottom(0);

                    int indexOfSelectedPageNumber = 0;
                    for (int i = 0; i < ImageToPdfConstants.PAGE_NUMBER_TYPE.length; i++) {
                        if (ImageToPdfConstants.PAGE_NUMBER_TYPE[i].equals(mSelectedPageNumber)) {
                            indexOfSelectedPageNumber = i;
                            break;
                        }
                    }

                    mOptions.setPageNumStyle(ImageToPdfConstants.PAGE_NUMBER_TYPE_VALUE[indexOfSelectedPageNumber]);
                    mOptions.setPasswordProtected(false);

                    String fileName = mOptions.getOutFileName() + ImageToPdfConstants.pdfExtension;
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DataConstants.PDF_DIRECTORY);

                    File file = new File(dir, fileName);
                    if (FileUtils.checkFileExist(file.getAbsolutePath())) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "Warning", getContext().getString(R.string.confirm_override_file), new ConfirmDialog.ConfirmListener() {
                            @Override
                            public void onSubmit() {
                                SettingImageToPdfDialog.this.dismiss();
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
        mWatermark.clearFocus();
        CommonUtils.hideKeyboard(getActivity());

        super.onDismiss(dialog);
    }

    public interface OnDialogSubmit {
        void submitForm(ImageToPDFOptions mOptions);
    }
}
