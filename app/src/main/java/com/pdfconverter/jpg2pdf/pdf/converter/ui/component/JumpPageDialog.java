package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;

public class JumpPageDialog extends BaseCenterDialog {

    private Context mContext;
    private int mNumberPage;
    private SplitRangeListener mListener;

    @SuppressLint("SetTextI18n")
    public JumpPageDialog(@NonNull Context context, int numberPage, SplitRangeListener listener) {
        super(context);
        mContext = context;
        mNumberPage = numberPage;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_jump_to_page);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = findViewById(R.id.btn_no);
        Button submitBtn = findViewById(R.id.btn_yes);

        EditText startPageEdt = findViewById(R.id.enter_page_edt);
        startPageEdt.setText("1");

        cancelBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }
            dismiss();
        });
        submitBtn.setOnClickListener(v -> {
            try {
                int toPage = Integer.parseInt(startPageEdt.getText().toString());

                if (toPage <= 0 || toPage > mNumberPage) {
                    ToastUtils.showMessageShort(mContext, mContext.getString(R.string.view_pdf_jump_to_page_error));
                    return;
                }

                if (mListener != null) {
                    mListener.onSubmitRange(toPage);
                }
                startPageEdt.clearFocus();

                dismiss();
            } catch (Exception e) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.view_pdf_jump_to_page_error));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface SplitRangeListener {
        void onSubmitRange(int page);
        void onCancel();
    }
}
