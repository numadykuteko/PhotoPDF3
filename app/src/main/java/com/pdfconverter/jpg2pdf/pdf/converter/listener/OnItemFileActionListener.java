package com.pdfconverter.jpg2pdf.pdf.converter.listener;

import androidx.recyclerview.widget.RecyclerView;

public interface OnItemFileActionListener {
    void onClick(int position);
    void onDelete(int position);
    void onClickSwap(RecyclerView.ViewHolder viewHolder);
    void onSwap(int oldPosition, int newPosition);
}
