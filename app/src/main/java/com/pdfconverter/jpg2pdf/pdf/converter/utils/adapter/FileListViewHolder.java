package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

public class FileListViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private ImageView mImageView;
    private ImageView mShareView;
    private ImageView mMoreView;
    private TextView mNameView;
    private TextView mDateTextView;

    public FileListViewHolder(@NonNull View itemView) {
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
    public void bindView(int position, FileData fileData, int currentItem, OnFileItemWithOptionClickListener listener) {

        if (fileData.getFilePath() != null) {
            mNameView.setText(fileData.getDisplayName());

            if (fileData.getTimeAdded() > 0) {
                String text = itemView.getContext().getString(R.string.full_detail_file, DateTimeUtils.fromTimeUnixToDateTimeString(fileData.getTimeAdded()), FileUtils.getFormattedSize(fileData.getSize()));
                mDateTextView.setText(text);
            } else {
                mDateTextView.setText(FileUtils.getFormattedSize(fileData.getSize()));
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
