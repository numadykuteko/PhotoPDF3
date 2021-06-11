package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnDataOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;

public class TypePdfViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private TextView mNameView;
    private ImageView mImageView;
    private View mSelectView;

    public TypePdfViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mSelectView = itemView.findViewById(R.id.item_selected_view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void bindView(int position, String nameOption, int imageOption, boolean isSelected, OnDataOptionClickListener listener) {
        mNameView.setText(nameOption);
        mImageView.setImageDrawable(itemView.getContext().getDrawable(imageOption));

        if (isSelected) {
            mSelectView.setVisibility(View.VISIBLE);
            mNameView.setTextColor(ColorUtils.getColorFromResource(itemView.getContext(), R.color.image_selected_border_color));
        } else {
            mSelectView.setVisibility(View.GONE);
            mNameView.setTextColor(ColorUtils.getColorFromResource(itemView.getContext(), R.color.black_totally));
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
