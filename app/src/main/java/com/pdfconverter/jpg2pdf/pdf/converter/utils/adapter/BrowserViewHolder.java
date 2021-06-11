package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

public class BrowserViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private ImageView mImageView;
    private ImageView mShareView;
    private ImageView mMoreView;
    private TextView mNameView;
    private TextView mDateTextView;

    public BrowserViewHolder(@NonNull View itemView) {
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

    @SuppressLint({"StaticFieldLeak", "UseCompatLoadingForDrawables", "SetTextI18n"})
    public void bindView(int position, FileData fileData, int currentItem, OnFileItemWithOptionClickListener listener) {

        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_DIRECTORY)) {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_folder));
            mNameView.setText(fileData.getDisplayName());
            mShareView.setVisibility(View.GONE);
            mMoreView.setVisibility(View.GONE);

            mContentView.setLongClickable(false);

            if (fileData.getTimeAdded() > 0) {
                mDateTextView.setVisibility(View.VISIBLE);
                mDateTextView.setText(DateTimeUtils.fromTimeUnixToDateTimeString(fileData.getTimeAdded()));
            } else {
                mDateTextView.setVisibility(View.GONE);
            }
        } else {
            mShareView.setVisibility(View.VISIBLE);
            mMoreView.setVisibility(View.VISIBLE);

            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_all_pdf_file_locked_full));

            if (fileData.getFilePath() != null) {
                mNameView.setText(fileData.getDisplayName());
            }

            mMoreView.setOnClickListener(view -> {
                listener.onOptionItem(position);
            });

            mShareView.setVisibility(View.GONE);

            mContentView.setLongClickable(true);
            mContentView.setOnLongClickListener(v -> {
                listener.onOptionItem(position);
                return true;
            });

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
    }
}
