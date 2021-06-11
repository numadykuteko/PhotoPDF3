package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;

public class EditPageOptionDialog extends BottomSheetDialogFragment {
    private EditPageOptionListener mListener;

    public EditPageOptionDialog() {
        // Required empty public constructor
    }

    public EditPageOptionDialog(EditPageOptionListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.sheet_dialog_style);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_edit_pages_options, container, false);

        ConstraintLayout swapPages = v.findViewById(R.id.edit_pages_layout_swap);
        swapPages.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.swapPage();
            }
            dismiss();
        });
        ConstraintLayout deletePage = v.findViewById(R.id.edit_pages_layout_delete);
        deletePage.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.deletePage();
            }
            dismiss();
        });
        ConstraintLayout removeDup = v.findViewById(R.id.edit_pages_layout_remove_dup);
        removeDup.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.removeDuplicate();
            }
            dismiss();
        });
        ConstraintLayout resetChanges = v.findViewById(R.id.edit_pages_layout_reset_changes);
        resetChanges.setOnClickListener(view -> {
            if (mListener != null) {
                mListener.resetAllChange();
            }
            dismiss();
        });
        return v;
    }

    public interface EditPageOptionListener {
        void swapPage();
        void deletePage();
        void removeDuplicate();
        void resetAllChange();
    }
}
