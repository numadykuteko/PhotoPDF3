package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.NewPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.CommonUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.TypePdfOptionAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;
import java.util.Objects;

public class SettingNewPdfDialog extends BottomSheetDialogFragment {
    private OnDialogSubmit mListener;
    private NewPDFOptions mOptions;
    private EditText mNameFile;
    private EditText mNumberPage;
    private TextView mPageSize;
    private RecyclerView mTypeRecyclerView;
    private TypePdfOptionAdapter mTypeAdapter;

    private Button mCancelButton;
    private Button mSubmitButton;

    private String mSelectedPageSize;

    public SettingNewPdfDialog() {
        // Required empty public constructor
    }

    public SettingNewPdfDialog(NewPDFOptions options, OnDialogSubmit listener) {
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setAutoExpanded();

        View v = inflater.inflate(R.layout.dialog_setting_new_pdf, container, false);

        mTypeRecyclerView = v.findViewById(R.id.data_option_list);
        mNameFile = v.findViewById(R.id.import_file_file_name);
        mNumberPage = v.findViewById(R.id.import_file_number_page);
        mPageSize = v.findViewById(R.id.import_file_page_size);

        mCancelButton = v.findViewById(R.id.btn_cancel);
        mSubmitButton = v.findViewById(R.id.btn_convert);

        if (mOptions == null) {
            ToastUtils.showSystemIssueToast(getContext());
            dismiss();
        } else {
            String[] typeList = {getString(R.string.new_pdf_blank), getString(R.string.new_pdf_lined),
                    getString(R.string.new_pdf_grid), getString(R.string.new_pdf_graph),
                    getString(R.string.new_pdf_music), getString(R.string.new_pdf_dotted),
                    getString(R.string.new_pdf_iso_dotted)};

            int[] imageList = {R.drawable.blank, R.drawable.lined, R.drawable.grid,
                    R.drawable.graph, R.drawable.music, R.drawable.dotted,
                    R.drawable.isomatric_dotted};

            mTypeAdapter = new TypePdfOptionAdapter(typeList, imageList, mOptions.getSelectedType(), position -> mTypeAdapter.clickItem(position));
            mTypeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            mTypeRecyclerView.setAdapter(mTypeAdapter);
            mTypeRecyclerView.scrollToPosition(mOptions.getSelectedType());

            mNameFile.setText(mOptions.getFileName());
            mNumberPage.setText(mOptions.getNumberPage() + "");

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

            mCancelButton.setOnClickListener(view -> dismiss());

            mSubmitButton.setOnClickListener(view -> {
                String nameFile = mNameFile.getText().toString().trim();

                if (nameFile.length() == 0) {
                    ToastUtils.showMessageShort(getContext(), getString(R.string.please_input_name_text));
                    return;
                }

                if (mListener != null) {
                    mOptions.setFileName(nameFile);
                    String numberPageString = mNumberPage.getText().toString();

                    try {
                        int numberPage = Integer.parseInt(numberPageString);
                        if (numberPage < 1 || numberPage > 999) {
                            ToastUtils.showMessageShort(getContext(), getString(R.string.new_pdf_please_not_valid_number_page));
                            return;
                        }
                        mOptions.setNumberPage(numberPage);
                    } catch (Exception e) {
                        ToastUtils.showMessageShort(getContext(), getString(R.string.new_pdf_please_input_number_page));
                        return;
                    }
                    mOptions.setPageSize(mSelectedPageSize);
                    mOptions.setSelectedType(mTypeAdapter.getSelectedPosition());

                    String fileName = mOptions.getFileName() + ImageToPdfConstants.pdfExtension;
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DataConstants.PDF_DIRECTORY);

                    File file = new File(dir, fileName);
                    if (FileUtils.checkFileExist(file.getAbsolutePath())) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), getString(R.string.warning_text), getContext().getString(R.string.confirm_override_file), new ConfirmDialog.ConfirmListener() {
                            @Override
                            public void onSubmit() {
                                SettingNewPdfDialog.this.dismiss();
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
        mNumberPage.clearFocus();
        CommonUtils.hideKeyboard(getActivity());

        super.onDismiss(dialog);
    }

    public interface OnDialogSubmit {
        void submitForm(NewPDFOptions mOptions);
    }
}
