package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

public class RemovePasswordFileDialog extends BaseCenterDialog {

    private Context mContext;
    private RemovePasswordFileListener mListener;
    private String mFilePath;
    private boolean mIsShowPassword = false;
    private EditText passwordEdt;
    private ImageView turnOnViewPassword;

    public RemovePasswordFileDialog(@NonNull Context context, String filePath, RemovePasswordFileListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        mFilePath = filePath;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_remove_password);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = findViewById(R.id.btn_no);
        Button submitBtn = findViewById(R.id.btn_yes);
        passwordEdt = findViewById(R.id.enter_password_edt);
        turnOnViewPassword = findViewById(R.id.turn_on_off_pass);

        setForShowPassword();

        turnOnViewPassword.setOnClickListener(view -> {
            mIsShowPassword = !mIsShowPassword;
            setForShowPassword();
        });

        cancelBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }
            dismiss();
        });
        submitBtn.setOnClickListener(v -> {
            String password = passwordEdt.getText().toString();

            if (password.length() == 0) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.protect_pdf_please_enter_password));
            } else if (!PdfUtils.isPasswordValid(mFilePath, password.getBytes())) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.unlock_pdf_enter_wrong_password));
            } else {
                mListener.onSubmitPassword(password);
                dismiss();
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForShowPassword() {
        if (mIsShowPassword) {
            passwordEdt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            turnOnViewPassword.setImageDrawable(mContext.getDrawable(R.drawable.ic_password_on));
        } else {
            passwordEdt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            turnOnViewPassword.setImageDrawable(mContext.getDrawable(R.drawable.ic_password_off));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface RemovePasswordFileListener {
        void onSubmitPassword(String password);
        void onCancel();
    }
}
