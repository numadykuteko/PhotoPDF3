package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnDataOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.DataOptionAdapter;

public class DataOptionDialog extends Dialog implements OnDataOptionClickListener {
    private static final String TAG = "FontFamilyOptionDialog";
    private Context mContext;
    private String mTitle;
    private String[] mOptionList;
    private String mSelectedOption;
    private DataOptionSubmit mListener;

    private Button mCancelButton;
    private Button mSubmitButton;
    private RecyclerView mRecyclerView;
    private TextView mNameView;
    private DataOptionAdapter mAdapter;

    public DataOptionDialog(@NonNull Context context, String title, String[] optionList, String selectedOption, DataOptionSubmit listener) {
        super(context);
        mListener = listener;
        mContext = context;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.gravity = Gravity.CENTER;
        getWindow().setAttributes(wlp);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_data_option);

        int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.8);
        getWindow().setLayout(width, height);

        mCancelButton = findViewById(R.id.btn_cancel);
        mSubmitButton = findViewById(R.id.btn_ok);
        mRecyclerView = findViewById(R.id.data_option_list);
        mNameView = findViewById(R.id.name_data_option);

        mTitle = title;
        mOptionList = optionList;
        mSelectedOption = selectedOption;

        mNameView.setText(mTitle);
        setForRecyclerView();

        mCancelButton.setOnClickListener(v -> dismiss());

        mSubmitButton.setOnClickListener(v -> {
            int selectedItemIndex = mAdapter.getSelectedPosition();
            if (mListener != null) {
                mListener.updateNewOption(selectedItemIndex);
            }

            dismiss();
        });
    }

    private void setForRecyclerView() {
        int currentIndex = 0;
        for (int i = 0; i < mOptionList.length; i++) {
            if (mOptionList[i].equals(mSelectedOption)) {
                currentIndex = i;
            }
        }
        mAdapter = new DataOptionAdapter(this, mOptionList, currentIndex);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClickItem(int position) {
        if (mAdapter != null) {
            mAdapter.clickItem(position);
        }
    }

    public interface DataOptionSubmit {
        void updateNewOption(int newOption);
    }
}

