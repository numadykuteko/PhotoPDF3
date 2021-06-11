package com.pdfconverter.jpg2pdf.pdf.converter.utils.nativeads;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.control.Admod;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;

public class NativeAdsViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "NativeAdsViewHolder";
    private boolean mIsLoaded = false;

    public NativeAdsViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bindView(boolean isReloaded) {
        if ((isReloaded || !mIsLoaded) && itemView.getContext() != null && itemView.getContext() instanceof BaseBindingActivity) {
            Admod.getInstance().loadSmallNativeFragment((BaseBindingActivity) itemView.getContext(), BuildConfig.native_id, itemView);
            mIsLoaded = true;
        }
    }
}
