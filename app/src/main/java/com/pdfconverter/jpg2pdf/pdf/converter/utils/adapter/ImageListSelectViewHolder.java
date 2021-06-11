package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;

public class ImageListSelectViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout mContentView;
    private ImageView mImageView;
    private ImageView mCameraView;
    private View mSelectedView;
    private CardView mOrderView;
    private TextView mOrderTextView;

    public ImageListSelectViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mCameraView = itemView.findViewById(R.id.item_camera_view);
        mSelectedView = itemView.findViewById(R.id.item_selected_view);
        mOrderView = itemView.findViewById(R.id.item_order_view);
        mOrderTextView = itemView.findViewById(R.id.item_order_tv);
    }

    @SuppressLint("SetTextI18n")
    public void bindView(int position, ImageData imageData, int orderSelected, OnFileItemClickListener listener) {

        mSelectedView.setVisibility(View.GONE);
        mOrderView.setVisibility(View.GONE);
        if (position == 0) {
            mCameraView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
        } else {
            mCameraView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);

            Glide.with(itemView.getContext())
                    .load(imageData.getImagePath())
                    .into(mImageView);

            if (orderSelected >= 0) {
                mOrderTextView.setText("" + (orderSelected + 1));
                mOrderView.setVisibility(View.VISIBLE);
                mSelectedView.setVisibility(View.VISIBLE);
            }
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
