package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.PageSelectAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SplitPageSelectDialog extends BaseCenterDialog {
    private static final String TAG = "FontFamilyOptionDialog";
    private Context mContext;
    private int mNumberPage;
    private SplitPageSelectSubmit mListener;

    private ImageView mCancelButton;
    private Button mSubmitButton;
    private RecyclerView mRecyclerView;
    private PageSelectAdapter mAdapter;

    public SplitPageSelectDialog(@NonNull Context context, int numberPage, SplitPageSelectSubmit listener) {
        super(context);
        mListener = listener;
        mContext = context;
        this.mNumberPage = numberPage;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.gravity = Gravity.CENTER;
        getWindow().setAttributes(wlp);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_split_page_select);

        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        mCancelButton = findViewById(R.id.toolbar_btn_back);
        mSubmitButton = findViewById(R.id.btn_ok);
        mRecyclerView = findViewById(R.id.data_option_list);

        setForRecyclerView();

        mCancelButton.setOnClickListener(v -> dismiss());

        mSubmitButton.setOnClickListener(v -> {
            int numberSelected = mAdapter.getNumberSelectedFile();
            if (numberSelected == 0) {
                ToastUtils.showMessageShort(mContext, mContext.getString(R.string.split_pdf_please_choose_page));
            } else {
                ArrayList<Integer> selectedList = mAdapter.getSelectedList();
                Collections.sort(selectedList, (integer, t1) -> {
                    if (integer > t1) {
                        return 1;
                    } else if (t1 > integer) {
                        return -1;
                    }
                    return 0;
                });

                for (int i = 0; i < selectedList.size(); i ++) {
                    int oldVal = selectedList.get(i);
                    int newVal = oldVal + 1;
                    selectedList.set(i, newVal);
                }

                if (mListener != null) {
                    mListener.updateNewOption(selectedList);
                }
                dismiss();
            }
        });
    }

    private void setForRecyclerView() {
        List<String> mOptionList = new ArrayList<>();
        for (int i = 0; i < mNumberPage ; i++) {
            mOptionList.add("" + (i + 1));
        }
        mAdapter = new PageSelectAdapter(position -> mAdapter.revertData(position));
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setData(mOptionList);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public interface SplitPageSelectSubmit {
        void updateNewOption(ArrayList<Integer> output);
    }
}

