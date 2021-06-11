package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemImageActionListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.image.ImageUtils;

import java.io.File;

public class PdfToImageViewHolder extends RecyclerView.ViewHolder {
    private ImageView mImageView;
    private ImageView mShareView;
    private ImageView mDownloadView;

    public PdfToImageViewHolder(@NonNull View itemView) {
        super(itemView);

        mShareView = itemView.findViewById(R.id.item_share_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mDownloadView = itemView.findViewById(R.id.item_download_view);
    }

    @SuppressLint("SetTextI18n")
    public void bindView(int position, ImageExtractData imageExtractData, OnItemImageActionListener listener) {
        itemView.setOnClickListener(v -> listener.onClick(position));

        ImageUtils.loadImageFromUriToView(itemView.getContext(), new File(imageExtractData.getFilePath()), mImageView);

        mDownloadView.setOnClickListener(view -> {
            listener.onDownload(position);
        });

        mShareView.setOnClickListener(view -> {
            listener.onShare(position);
        });
    }
}