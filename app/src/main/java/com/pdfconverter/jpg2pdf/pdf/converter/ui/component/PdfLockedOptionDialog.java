package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;

public class PdfLockedOptionDialog extends BottomSheetDialogFragment {
    private LockedFileOptionListener mListener;
    private boolean mBookmarked;
    private String mNameFile;
    private int mPosition;
    private long mDate;

    public PdfLockedOptionDialog() {
        // Required empty public constructor
    }

    public PdfLockedOptionDialog(boolean isBookmarked, String nameFile, long date, int position, LockedFileOptionListener listener) {
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_pdf_locked_option, container, false);
        TextView nameFile = v.findViewById(R.id.more_locked_name);
        nameFile.setText(mNameFile);
        nameFile.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.openFile(mPosition);
            }
            dismiss();
        });

        TextView dateFile = v.findViewById(R.id.more_locked_date);
        if (mDate != -1) {
            dateFile.setText(DateTimeUtils.fromTimeUnixToDateString(mDate));
        } else {
            dateFile.setText("Locked file");
        }

        ImageView upload = v.findViewById(R.id.more_locked_upload);
        upload.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.uploadFile(mPosition);
            }
            dismiss();
        });

        TextView addBookmarkTv = v.findViewById(R.id.more_locked_textview_name);
        ImageView addBookmarkImg = v.findViewById(R.id.more_locked_imageview_name);
        if (mBookmarked) {
            addBookmarkTv.setText(R.string.more_locked_remove_bm);
            addBookmarkImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_remove_bookmark));
        } else {
            addBookmarkTv.setText(R.string.more_locked_add_bm);
            addBookmarkImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_more_locked_file_add_bm));
        }

        ConstraintLayout bookmarkOption = v.findViewById(R.id.more_locked_layout_add_bm);
        bookmarkOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.optionBookmark(mPosition, !mBookmarked);
            }
            dismiss();
        });

        ConstraintLayout removePasswordOption = v.findViewById(R.id.more_locked_layout_remove_pass);
        removePasswordOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.setPassword(mPosition);
            }
            dismiss();
        });

        ConstraintLayout shareOption = v.findViewById(R.id.more_locked_layout_share);
        shareOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.shareFile(mPosition);
            }
            dismiss();
        });

        ConstraintLayout renameOption = v.findViewById(R.id.more_locked_layout_rename);
        renameOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.renameFile(mPosition);
            }
            dismiss();
        });

        ConstraintLayout deleteOption = v.findViewById(R.id.more_locked_layout_delete);
        deleteOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.deleteFile(mPosition);
            }
            dismiss();
        });

        return v;
    }

    public interface LockedFileOptionListener {
        void openFile(int position);
        void shareFile(int position);
        void uploadFile(int position);
        void optionBookmark(int position, boolean isAdd);
        void setPassword(int position);
        void renameFile(int position);
        void deleteFile(int position);
    }
}
