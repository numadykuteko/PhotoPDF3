package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnDataOptionClickListener;

public class TypePdfOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DataOptionAdapter";
    private String[] mOptionList;
    private int[] mImageList;
    private int mSelectedPosition;

    private OnDataOptionClickListener mListener;

    public TypePdfOptionAdapter(String[] optionList, int[] imageList, int selectedPosition, OnDataOptionClickListener listener) {
        this.mListener = listener;
        mOptionList = optionList;
        mSelectedPosition = selectedPosition;
        mImageList = imageList;
    }

    public void clickItem(int position) {
        int temp = mSelectedPosition;

        mSelectedPosition = position;
        notifyItemChanged(temp);
        notifyItemChanged(position);
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public TypePdfOptionAdapter() {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type_pdf, parent, false);
        return new TypePdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TypePdfViewHolder) holder).bindView(position, mOptionList[position], mImageList[position], mSelectedPosition == position, mListener);
    }

    @Override
    public int getItemCount() {
        return mOptionList.length;
    }
}
