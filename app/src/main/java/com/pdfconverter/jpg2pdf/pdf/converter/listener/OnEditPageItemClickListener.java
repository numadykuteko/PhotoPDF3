package com.pdfconverter.jpg2pdf.pdf.converter.listener;

public interface OnEditPageItemClickListener {
    void onDeleteItem(int position);
    void onSwap(int oldPosition, int newPosition);
}
