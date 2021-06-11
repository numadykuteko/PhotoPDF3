package com.pdfconverter.jpg2pdf.pdf.converter.ui.theme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnThemeItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.nativeads.NativeAdsViewHolder;

public class ThemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ThemeAdapter";
    private int mCurrentItem = 1;

    private OnThemeItemClickListener mListener;

    public static final int ADS_INDEX = 1;

    public ThemeAdapter(OnThemeItemClickListener listener) {
        this.mListener = listener;
    }

    public void setCurrentItem(int position) {
        int temp = mCurrentItem;

        mCurrentItem = position;
        notifyItemChanged(temp);
        notifyItemChanged(mCurrentItem);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == ADS_INDEX) {
            return 1;
        } else {
            return super.getItemViewType(position);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false);
            return new ThemeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_small_native_control, parent, false);
            return new NativeAdsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((ThemeViewHolder) holder).bindView(position, mCurrentItem, mListener);
        } else {
            ((NativeAdsViewHolder) holder).bindView(true);
        }
    }

    @Override
    public int getItemCount() {
        return DataConstants.THEME_ITEM_LIST.length;
    }
}
