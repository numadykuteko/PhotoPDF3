package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemTextActionListener;

public class PdfToTextViewHolder extends RecyclerView.ViewHolder {
    private TextView mNameView;
    private TextView mDescriptionView;
    private ImageView mOptionView;

    public PdfToTextViewHolder(@NonNull View itemView) {
        super(itemView);

        mNameView = itemView.findViewById(R.id.item_name_view);
        mDescriptionView = itemView.findViewById(R.id.item_description_view);
        mOptionView = itemView.findViewById(R.id.item_option_view);
    }

    @SuppressLint("SetTextI18n")
    public void bindView(int position, TextExtractData textExtractData, OnItemTextActionListener listener) {
        mNameView.setText("Page " + textExtractData.getPage());
        itemView.setOnClickListener(v -> listener.onClick(position));
        mOptionView.setOnClickListener(v -> listener.onOption(position));

        if (textExtractData.getTextContent() != null && textExtractData.getTextContent().trim().length() > 0) {
            mDescriptionView.setText(textExtractData.getTextContent());
            mDescriptionView.setOnClickListener(v -> {
                if (mDescriptionView.getMaxLines() == 5) {
                    mDescriptionView.setMaxLines(11);
                } else {
                    mDescriptionView.setMaxLines(5);
                }
            });
        } else {
            mDescriptionView.setText(R.string.pdf_to_text_no_text_for_this_page);
        }
    }
}