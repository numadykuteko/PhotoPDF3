package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.R;

public class ChooseMethodDialog extends BottomSheetDialogFragment {
    private ChooseMethodOptionListener mListener;

    public ChooseMethodDialog() {
        // Required empty public constructor
    }

    public ChooseMethodDialog(ChooseMethodOptionListener listener) {
        this.mListener = listener;
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
        View v = inflater.inflate(R.layout.dialog_choose_method, container, false);

        ConstraintLayout takePicture = v.findViewById(R.id.take_picture);
        ConstraintLayout scanDocument = v.findViewById(R.id.scan_document);

        takePicture.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.takePicture();
            }

            dismiss();
        });

        scanDocument.setOnClickListener(v1 -> {
            if (mListener != null) {
                mListener.scanDocument();
            }

            dismiss();
        });
        return v;
    }

    public interface ChooseMethodOptionListener {
        void takePicture();
        void scanDocument();
    }
}
