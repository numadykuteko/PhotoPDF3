package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;

public class PdfOptionDialog extends BottomSheetDialogFragment {
    private FileOptionListener mListener;
    private boolean mBookmarked;
    private String mNameFile;
    private int mPosition;
    private long mDate;

    public PdfOptionDialog() {
        // Required empty public constructor
    }

    public PdfOptionDialog(boolean isBookmarked, String nameFile, long date, int position, FileOptionListener listener) {
        this.mListener = listener;
        this.mBookmarked = isBookmarked;
        mNameFile = nameFile;
        mPosition = position;
        mDate = date;
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setAutoExpanded();

        View v = inflater.inflate(R.layout.dialog_pdf_option, container, false);
        TextView nameFile = v.findViewById(R.id.more_name);
        nameFile.setText(mNameFile);
        nameFile.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.openFile(mPosition);
            }
            dismiss();
        });

        TextView dateFile = v.findViewById(R.id.more_date);
        if (mDate != -1) {
            dateFile.setText(DateTimeUtils.fromTimeUnixToDateString(mDate));
        } else {
            dateFile.setText("Locked file");
        }

        ImageView upload = v.findViewById(R.id.more_upload);
        upload.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.uploadFile(mPosition);
            }
            dismiss();
        });

        ImageView print = v.findViewById(R.id.more_print);
        print.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.printFile(mPosition);
            }
            dismiss();
        });

        TextView addBookmarkTv = v.findViewById(R.id.more_textview_name);
        ImageView addBookmarkImg = v.findViewById(R.id.more_imageview_name);
        if (mBookmarked) {
            addBookmarkTv.setText(R.string.more_locked_remove_bm);
            addBookmarkImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_remove_bookmark));
        } else {
            addBookmarkTv.setText(R.string.more_locked_add_bm);
            addBookmarkImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_more_locked_file_add_bm));
        }

        ConstraintLayout bookmarkOption = v.findViewById(R.id.more_layout_add_bm);
        bookmarkOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.optionBookmark(mPosition, !mBookmarked);
            }
            dismiss();
        });

        ConstraintLayout setPasswordOption = v.findViewById(R.id.more_layout_set_pass);
        setPasswordOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.setPassword(mPosition);
            }
            dismiss();
        });

        ConstraintLayout splitOption = v.findViewById(R.id.more_layout_split);
        splitOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.splitFile(mPosition);
            }
            dismiss();
        });

        ConstraintLayout editOption = v.findViewById(R.id.more_layout_edit_page);
        editOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.editFile(mPosition);
            }
            dismiss();
        });

        ConstraintLayout addWatermarkOption = v.findViewById(R.id.more_layout_add_wm);
        addWatermarkOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.addWatermark(mPosition);
            }
            dismiss();
        });

        ConstraintLayout extractToTextOption = v.findViewById(R.id.more_layout_extract_text);
        extractToTextOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.extractToText(mPosition);
            }
            dismiss();
        });

        ConstraintLayout extractToImageOption = v.findViewById(R.id.more_layout_extract_image);
        extractToImageOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.extractToImage(mPosition);
            }
            dismiss();
        });

        ConstraintLayout shareOption = v.findViewById(R.id.more_layout_share);
        shareOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.shareFile(mPosition);
            }
            dismiss();
        });

        ConstraintLayout renameOption = v.findViewById(R.id.more_layout_rename);
        renameOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.renameFile(mPosition);
            }
            dismiss();
        });

        ConstraintLayout deleteOption = v.findViewById(R.id.more_layout_delete);
        deleteOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.deleteFile(mPosition);
            }
            dismiss();
        });

        return v;
    }

    public interface FileOptionListener {
        void openFile(int position);
        void shareFile(int position);
        void printFile(int position);
        void uploadFile(int position);
        void optionBookmark(int position, boolean isAdd);
        void setPassword(int position);
        void splitFile(int position);
        void editFile(int position);
        void addWatermark(int position);
        void extractToText(int position);
        void extractToImage(int position);
        void renameFile(int position);
        void deleteFile(int position);
    }
}
