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

public class ViewPdfOptionDialog extends BottomSheetDialogFragment {
    private FileOptionListener mListener;
    private boolean mBookmarked;
    private String mNameFile;
    private boolean mIsLocked;
    private boolean mIsSupportDelete;
    private TextView mBookmarkView;
    private ImageView mAddBookmarkImg;

    public ViewPdfOptionDialog() {
        // Required empty public constructor
    }

    public ViewPdfOptionDialog(boolean isBookmarked, boolean isLocked, boolean isSupportDelete, String nameFile, FileOptionListener listener) {
        this.mListener = listener;
        this.mBookmarked = isBookmarked;
        this.mNameFile = nameFile;
        this.mIsLocked = isLocked;
        this.mIsSupportDelete = isSupportDelete;
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

        View v = inflater.inflate(R.layout.dialog_view_pdf_option, container, false);

        setAutoExpanded();

        TextView nameFile = v.findViewById(R.id.more_name);
        nameFile.setText(mNameFile);

        ImageView upload = v.findViewById(R.id.more_upload);
        upload.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.uploadFile();
            }
            dismiss();
        });

        ImageView print = v.findViewById(R.id.more_print);
        if (mIsLocked) {
            print.setVisibility(View.GONE);
        }
        print.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.printFile();
            }
            dismiss();
        });

        mBookmarkView = v.findViewById(R.id.more_textview_name);
        mAddBookmarkImg = v.findViewById(R.id.more_imageview_name);
        setForBookmark();

        ConstraintLayout bookmarkOption = v.findViewById(R.id.more_layout_add_bm);
        bookmarkOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.optionBookmark();
            }
        });

        ConstraintLayout setPasswordOption = v.findViewById(R.id.more_layout_set_pass);
        ImageView passwordStateImg = v.findViewById(R.id.more_lock);
        ImageView setPasswordImg = v.findViewById(R.id.more_imageview_set_pass);
        TextView setPasswordTv = v.findViewById(R.id.more_textview_set_pass);
        if (!mIsLocked) {
            passwordStateImg.setVisibility(View.GONE);
            setPasswordImg.setImageDrawable(getActivity().getDrawable(R.drawable.ic_more_locked_file_lock));
            setPasswordTv.setText(R.string.more_set_pass);
        } else {
            setPasswordImg.setImageDrawable(getActivity().getDrawable(R.drawable.ic_more_locked_file_remove_pass));
            setPasswordTv.setText(R.string.more_locked_remove_pass);
        }
        setPasswordOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.optionPassword();
            }
            dismiss();
        });

        ConstraintLayout shareOption = v.findViewById(R.id.more_layout_share);
        shareOption.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.shareFile();
            }
            dismiss();
        });

        ConstraintLayout splitOption = v.findViewById(R.id.more_layout_split);
        ConstraintLayout editOption = v.findViewById(R.id.more_layout_edit_page);
        ConstraintLayout addWatermarkOption = v.findViewById(R.id.more_layout_add_wm);
        ConstraintLayout extractToTextOption = v.findViewById(R.id.more_layout_extract_text);
        ConstraintLayout extractToImageOption = v.findViewById(R.id.more_layout_extract_image);

        if (mIsLocked) {
            splitOption.setVisibility(View.GONE);
            editOption.setVisibility(View.GONE);
            addWatermarkOption.setVisibility(View.GONE);
            extractToTextOption.setVisibility(View.GONE);
            extractToImageOption.setVisibility(View.GONE);
        } else {
            splitOption.setOnClickListener(v1 -> {
                if (mListener != null) {
                    mListener.splitFile();
                }
                dismiss();
            });

            editOption.setOnClickListener(v1 -> {
                if (mListener != null) {
                    mListener.editFile();
                }
                dismiss();
            });

            addWatermarkOption.setOnClickListener(v1 -> {
                if (mListener != null) {
                    mListener.addWatermark();
                }
                dismiss();
            });

            extractToTextOption.setOnClickListener(v1 -> {
                if (mListener != null) {
                    mListener.extractToText();
                }
                dismiss();
            });

            extractToImageOption.setOnClickListener(v1 -> {
                if (mListener != null) {
                    mListener.extractToImage();
                }
                dismiss();
            });
        }

        ConstraintLayout deleteOption = v.findViewById(R.id.more_layout_delete);
        if (mIsSupportDelete) {
            deleteOption.setOnClickListener(v1 -> {
                if (mListener != null) {
                    mListener.deleteFile();
                }
                dismiss();
            });
        } else {
            deleteOption.setVisibility(View.GONE);
        }

        return v;
    }

    public void setBookmark(boolean isBookmarked) {
        mBookmarked = isBookmarked;
        setForBookmark();
    }

    public void setForBookmark() {
        if (mBookmarked) {
            mBookmarkView.setText(R.string.more_locked_remove_bm);
            mAddBookmarkImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_remove_bookmark));
        } else {
            mBookmarkView.setText(R.string.more_locked_add_bm);
            mAddBookmarkImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_more_locked_file_add_bm));
        }
    }

    public interface FileOptionListener {
        void openFile();
        void shareFile();
        void printFile();
        void uploadFile();
        void optionBookmark();
        void optionPassword();
        void splitFile();
        void editFile();
        void addWatermark();
        void extractToText();
        void extractToImage();
        void deleteFile();
    }
}
