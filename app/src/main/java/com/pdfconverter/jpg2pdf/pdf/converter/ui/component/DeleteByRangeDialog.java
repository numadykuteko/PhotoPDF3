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

public class DeleteByRangeDialog extends BaseCenterDialog {

    private Context mContext;
    private int mNumberPage;
    private DeleteRangeListener mListener;

    @SuppressLint("SetTextI18n")
    public DeleteByRangeDialog(@NonNull Context context, int numberPage, DeleteRangeListener listener) {
        super(context);
        mContext = context;
        mNumberPage = numberPage;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_delete_in_range);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = findViewById(R.id.btn_no);
        Button submitBtn = findViewById(R.id.btn_yes);

        EditText startPageEdt = findViewById(R.id.enter_start_page_edt);
        EditText endPageEdt = findViewById(R.id.enter_end_page_edt);
        startPageEdt.setText("1");
        endPageEdt.setText("" + mNumberPage);

        cancelBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }
            dismiss();
        });
        submitBtn.setOnClickListener(v -> {
            try {
                int fromPage = Integer.parseInt(startPageEdt.getText().toString());
                int toPage = Integer.parseInt(endPageEdt.getText().toString());

                if (fromPage <= 0 || toPage <= 0 || fromPage > toPage) {
                    ToastUtils.showMessageShort(mContext, mContext.getString(R.string.split_pdf_option_1_error));
                    return;
                } else if (toPage > mNumberPage) {
                    ToastUtils.showMessageShort(mContext, mContext.getString(R.string.split_pdf_option_1_error));
                    return;
                }

                if (mListener != null) {
                    mListener.onSubmitRange(fromPage, toPage);
                }
                dismiss();
            } catch (Exception e) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.split_pdf_option_1_error));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface DeleteRangeListener {
        void onSubmitRange(int start, int end);
        void onCancel();
    }
}
