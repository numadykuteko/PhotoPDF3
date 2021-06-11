package com.pdfconverter.jpg2pdf.pdf.converter.ui.texttopdf;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemFileActionListener;

public class TextFileViewHolder extends RecyclerView.ViewHolder {

    private TextView mNameView;
    private ImageView mDragView;

    public TextFileViewHolder(@NonNull View itemView) {
        super(itemView);

        mNameView = itemView.findViewById(R.id.item_name_view);
        mDragView = itemView.findViewById(R.id.item_drag_view);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bindView(int position, DocumentData documentData, OnItemFileActionListener listener) {
        mNameView.setText(documentData.getDisplayName());

        itemView.setOnClickListener(v -> listener.onClick(position));
        mDragView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && listener != null) {
                // 2. When we detect touch-down event, we call the
                //    startDragging(...) method we prepared above
                listener.onClickSwap(TextFileViewHolder.this);
            }

            return false;
        });
    }
}
