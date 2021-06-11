package com.pdfconverter.jpg2pdf.pdf.converter.ui.split;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitFileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemSplitActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

public class SplitPdfViewHolder extends RecyclerView.ViewHolder {
    private TextView mNameView;
    private TextView mDescriptionView;
    private ImageView mOptionView;

    public SplitPdfViewHolder(@NonNull View itemView) {
        super(itemView);

        mNameView = itemView.findViewById(R.id.item_name_view);
        mOptionView = itemView.findViewById(R.id.item_option_view);
        mDescriptionView = itemView.findViewById(R.id.item_description_view);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public void bindView(int position, SplitFileData splitFileData, OnItemSplitActionListener listener) {
        mNameView.setText(splitFileData.getFileName());

        itemView.setOnClickListener(v -> listener.onClick(itemView, splitFileData.isCreated(), position));

        if (splitFileData.isCreated()) {
            mDescriptionView.setOnClickListener(v -> listener.onOption(splitFileData.isCreated(), position));
            mOptionView.setOnClickListener(view -> {});
        } else {
            mOptionView.setOnClickListener(v -> listener.onOption(splitFileData.isCreated(), position));
            mDescriptionView.setOnClickListener(view -> {});
        }
        if (splitFileData.isCreated()) {
            String folderPath = FileUtils.getFileDirectoryPath(splitFileData.getFilePath());

            mDescriptionView.setText(FileUtils.getMinimalDirectoryPath(folderPath, DataConstants.PDF_DIRECTORY));
            mOptionView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_split_checked));
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Page list: ");

            for (int i = 0; i < splitFileData.getPageList().size() && i < 7; i++) {
                stringBuilder.append(splitFileData.getPageList().get(i)).append(" ");
            }
            if (splitFileData.getPageList().size() > 7) {
                stringBuilder.append(" ...");
            }
            mDescriptionView.setText(stringBuilder.toString());
            mOptionView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_split_remove));
        }
    }
}
