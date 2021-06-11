package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SavedData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;

public class SaveListViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private ImageView mImageView;
    private ImageView mShareView;
    private ImageView mMoreView;
    private TextView mNameView;
    private TextView mDateTextView;

    public SaveListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mShareView = itemView.findViewById(R.id.item_share_view);
        mMoreView = itemView.findViewById(R.id.item_more_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mDateTextView = itemView.findViewById(R.id.item_date_text_view);
    }

    @SuppressLint("StaticFieldLeak")
    public void bindView(int position, SavedData fileData, int currentItem, OnFileItemWithOptionClickListener listener) {

        if (fileData.getFilePath() != null) {
            mNameView.setText(fileData.getDisplayName());

            if (fileData.getTimeAdded() > 0) {
                String text = DateTimeUtils.fromTimeUnixToDateTimeString(fileData.getTimeAdded());
                mDateTextView.setText(text);
            } else {
                mDateTextView.setText("");
            }
        }
        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });

        mMoreView.setOnClickListener(view -> {
            listener.onOptionItem(position);
        });

        mShareView.setOnClickListener(view -> {
            listener.onShareItem(position);
        });
    }
}
