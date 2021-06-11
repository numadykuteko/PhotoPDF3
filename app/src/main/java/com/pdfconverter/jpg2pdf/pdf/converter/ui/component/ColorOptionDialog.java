package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.github.danielnilsson9.colorpickerview.view.ColorPickerView;
import com.pdfconverter.jpg2pdf.pdf.converter.R;

public class ColorOptionDialog extends BaseCenterDialog {
    private static final String TAG = "ColorOptionDialog";
    private Context mContext;
    private int mSelectedColor;
    private ColorOptionSubmit mListener;

    private Button mCancelButton;
    private Button mSubmitButton;
    private ColorPickerView mPickerView;

    public ColorOptionDialog(@NonNull Context context, int selectedColor, ColorOptionSubmit listener) {
        super(context);
        mListener = listener;
        mContext = context;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.gravity = Gravity.CENTER;
        getWindow().setAttributes(wlp);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_color_option);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        mCancelButton = findViewById(R.id.btn_cancel);
        mSubmitButton = findViewById(R.id.btn_ok);
        mPickerView = findViewById(R.id.color_picker_view);

        mSelectedColor = selectedColor;
        mPickerView.setColor(mSelectedColor);

        mCancelButton.setOnClickListener(v -> dismiss());

        mSubmitButton.setOnClickListener(v -> {
            int newColor = mPickerView.getColor();
            if (mListener != null) {
                mListener.updateNewColor(newColor);
            }

            dismiss();
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

    public interface ColorOptionSubmit {
        void updateNewColor(int newColor);
    }
}

