package com.pdfconverter.jpg2pdf.pdf.converter.ui.theme;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnThemeItemClickListener;

public class ThemeViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private MaterialCheckBox mCheckBoxView;
    private TextView mNameView;

    public ThemeViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mCheckBoxView = itemView.findViewById(R.id.item_checkbox_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void bindView(int position, int currentSelected, OnThemeItemClickListener listener) {
        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });

        mContentView.setBackground(itemView.getContext().getDrawable(DataConstants.THEME_ITEM_LIST[position]));
        mNameView.setText(DataConstants.THEME_NAME_LIST[position]);

        mCheckBoxView.setChecked(currentSelected == position);
    }
}
