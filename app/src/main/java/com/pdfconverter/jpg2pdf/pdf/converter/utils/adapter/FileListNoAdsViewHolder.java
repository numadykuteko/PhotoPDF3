package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;

public class FileListNoAdsViewHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private TextView mNameView;
    private TextView mDateTextView;
    private ConstraintLayout mContentView;

    public FileListNoAdsViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mDateTextView = itemView.findViewById(R.id.item_date_text_view);
    }

    public void bindView(int position, FileData fileData, int currentItem, OnFileItemClickListener listener) {
        mNameView.setText(fileData.getDisplayName());
        if (fileData.getTimeAdded() > 0) {
            mDateTextView.setVisibility(View.VISIBLE);
            mDateTextView.setText(DateTimeUtils.fromTimeUnixToDateString(fileData.getTimeAdded()));
        } else {
            mDateTextView.setVisibility(View.GONE);
        }

        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_PDF)) {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_all_pdf_file_full));
        } else if (fileData.getFileType().equals(DataConstants.FILE_TYPE_WORD)) {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_import_file_word_full));
        } else if (fileData.getFileType().equals(DataConstants.FILE_TYPE_TXT)) {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_import_file_word_full));
        } else {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_import_file_excel_full));
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
