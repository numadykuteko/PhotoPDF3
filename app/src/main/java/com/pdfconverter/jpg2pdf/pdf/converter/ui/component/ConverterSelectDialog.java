package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;

public class ConverterSelectDialog extends Dialog {
    private static final String TAG = "ConverterSelectDialog";
    private Context mContext;

    private ConverterSelectSubmit mListener;
    private int mCurrentSelect;

    private ConstraintLayout mImageLayout;
    private ConstraintLayout mTextLayout;
    private ConstraintLayout mExcelLayout;

    private ImageView mImageImg;
    private ImageView mTextImg;
    private ImageView mExcelImg;

    private ImageView mImageIcon;
    private ImageView mTextIcon;
    private ImageView mExcelIcon;

    private TextView mImageTv;
    private TextView mTextTv;
    private TextView mExcelTv;

    @SuppressLint("UseCompatLoadingForDrawables")
    public ConverterSelectDialog(@NonNull Context context, int currentSelect, ConverterSelectSubmit listener) {
        super(context);
        mListener = listener;
        mContext = context;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.gravity = Gravity.CENTER;
        getWindow().setAttributes(wlp);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_converter);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        mImageLayout = findViewById(R.id.select_converter_itp_layout);
        mTextLayout = findViewById(R.id.select_converter_ttp_layout);
        mExcelLayout = findViewById(R.id.select_converter_etp_layout);

        mImageImg = findViewById(R.id.select_converter_itp_checked);
        mTextImg = findViewById(R.id.select_converter_ttp_checked);
        mExcelImg = findViewById(R.id.select_converter_etp_checked);

        mImageIcon = findViewById(R.id.select_converter_itp_imageview);
        mTextIcon = findViewById(R.id.select_converter_ttp_imageview);
        mExcelIcon = findViewById(R.id.select_converter_etp_imageview);

        mImageTv = findViewById(R.id.select_converter_itp_textview);
        mTextTv = findViewById(R.id.select_converter_ttp_textview);
        mExcelTv = findViewById(R.id.select_converter_etp_textview);

        mCurrentSelect = currentSelect;

        mImageImg.setVisibility(View.GONE);
        mTextImg.setVisibility(View.GONE);
        mExcelImg.setVisibility(View.GONE);

        int selectedBgColor = ColorUtils.getColorFromResource(context, R.color.converter_selected);
        int selectedTextColor = ColorUtils.getColorFromResource(context, R.color.converter_text_selected);
        if (mCurrentSelect == 0) {
            mImageLayout.setBackgroundColor(selectedBgColor);
            mImageTv.setTextColor(selectedTextColor);
            mImageImg.setVisibility(View.VISIBLE);
            mImageIcon.setImageDrawable(context.getDrawable(R.drawable.ic_select_converter_image_to_pdf_selected));
        } else if (mCurrentSelect == 1) {
            mTextLayout.setBackgroundColor(selectedBgColor);
            mTextTv.setTextColor(selectedTextColor);
            mTextImg.setVisibility(View.VISIBLE);
            mTextIcon.setImageDrawable(context.getDrawable(R.drawable.ic_select_converter_text_to_pdf_selected));
        } else {
            mExcelLayout.setBackgroundColor(selectedBgColor);
            mExcelTv.setTextColor(selectedTextColor);
            mExcelImg.setVisibility(View.VISIBLE);
            mExcelIcon.setImageDrawable(context.getDrawable(R.drawable.ic_select_converter_excel_to_pdf_selected));
        }

        mImageLayout.setOnClickListener(view -> {
            if (mCurrentSelect != 0 && mListener != null) {
                dismiss();
                mListener.updateSelection(0);
            }
        });

        mTextLayout.setOnClickListener(view -> {
            if (mCurrentSelect != 1 && mListener != null) {
                dismiss();
                mListener.updateSelection(1);
            }
        });

        mExcelLayout.setOnClickListener(view -> {
            if (mCurrentSelect != 2 && mListener != null) {
                dismiss();
                mListener.updateSelection(2);
            }
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public interface ConverterSelectSubmit {
        void updateSelection(int newSelect);
    }
}

