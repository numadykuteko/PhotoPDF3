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

public class SetPasswordFileDialog extends BaseCenterDialog {

    private Context mContext;
    private SetPasswordFileListener mListener;
    private boolean mIsShowPassword = false, mIsShowConfirm = false;
    private EditText passwordEdt;
    private EditText confirmPasswordEdt;
    private ImageView turnOnViewPassword;
    private ImageView turnOnViewConfirmPassword;


    public SetPasswordFileDialog(@NonNull Context context,SetPasswordFileListener listener) {
        super(context);
        mContext = context;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_password);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT);

        Button cancelBtn = findViewById(R.id.btn_no);
        Button submitBtn = findViewById(R.id.btn_yes);
        passwordEdt = findViewById(R.id.enter_password_edt);
        confirmPasswordEdt = findViewById(R.id.enter_confirm_password_edt);
        turnOnViewPassword = findViewById(R.id.turn_on_off_pass_1);
        turnOnViewConfirmPassword = findViewById(R.id.turn_on_off_pass_2);

        setForShowPassword();
        setForShowConfirm();

        turnOnViewPassword.setOnClickListener(view -> {
            mIsShowPassword = !mIsShowPassword;
            setForShowPassword();
        });

        turnOnViewConfirmPassword.setOnClickListener(view -> {
            mIsShowConfirm = !mIsShowConfirm;
            setForShowConfirm();
        });

        cancelBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }
            dismiss();
        });
        submitBtn.setOnClickListener(v -> {
            String password = passwordEdt.getText().toString();
            String confirmPassword = confirmPasswordEdt.getText().toString();

            if (password.length() == 0) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.protect_pdf_please_enter_password));
            } else if (confirmPassword.length() == 0) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.protect_pdf_please_reenter_password));
            } else if (!confirmPassword.equals(password)) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.protect_pdf_password_not_match));
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForShowConfirm() {
        if (mIsShowConfirm) {
            confirmPasswordEdt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            turnOnViewConfirmPassword.setImageDrawable(mContext.getDrawable(R.drawable.ic_password_on));
        } else {
            confirmPasswordEdt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            turnOnViewConfirmPassword.setImageDrawable(mContext.getDrawable(R.drawable.ic_password_off));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface SetPasswordFileListener {
        void onSubmitPassword(String password);
        void onCancel();
    }
}
