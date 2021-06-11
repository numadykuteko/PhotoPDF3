package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;

public class PageSelectViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private TextView mNameView;
    private View mCheckView;

    public PageSelectViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mCheckView = itemView.findViewById(R.id.item_selected_view);
    }

    public void bindView(int position, String name, boolean isSelected, OnFileItemClickListener listener) {
        mNameView.setText(name);

        if (isSelected) {
            mCheckView.setVisibility(View.VISIBLE);
        } else {
            mCheckView.setVisibility(View.GONE);
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
