package com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnEditPageItemClickListener;

public class PageDataViewHolder extends RecyclerView.ViewHolder {
    private TextView mDestinationPageView;
    private ImageView mDescriptionView;
    private ImageView mOptionDelete;

    public PageDataViewHolder(@NonNull View itemView) {
        super(itemView);

        mDestinationPageView = itemView.findViewById(R.id.item_destination_page_view);
        mOptionDelete = itemView.findViewById(R.id.item_option_delete);
        mDescriptionView = itemView.findViewById(R.id.item_description_view);
    }

    @SuppressLint("SetTextI18n")
    public void bindView(int position, PageData pageData, OnEditPageItemClickListener listener) {
        mDestinationPageView.setText("" + (position + 1));
        mDescriptionView.setImageBitmap(pageData.getBitmap());

        mOptionDelete.setOnClickListener(v -> listener.onDeleteItem(position));
    }
}
