package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.R;

public class BindingAdapterItemView {

    private static final String TAG = "BindingAdapterItemView";

    @SuppressLint("CheckResult")
    @BindingAdapter("bind:imageUrl")
    public static void loadImage(ImageView view, String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = view.getContext().getDrawable(R.drawable.ic_add);
            view.setImageDrawable(drawable);
        } else {
            RequestOptions options = new RequestOptions();
            options.fitCenter();
            Glide.with(view.getContext())
                    .load(imagePath)
                    .apply(options)
                    .into(view);
        }
        Log.d(TAG, "loadImage");
    }
}
