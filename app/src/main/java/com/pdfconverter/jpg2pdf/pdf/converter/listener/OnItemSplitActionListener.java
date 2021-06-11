package com.pdfconverter.jpg2pdf.pdf.converter.listener;

import android.view.View;

public interface OnItemSplitActionListener {
    void onClick(View view, boolean isCreated, int position);
    void onOption(boolean isCreated, int position);
}
