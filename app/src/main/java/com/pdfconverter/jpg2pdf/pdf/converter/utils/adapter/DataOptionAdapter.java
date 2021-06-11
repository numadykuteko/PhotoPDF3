package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnDataOptionClickListener;

public class DataOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DataOptionAdapter";
    private String[] mOptionList;
    private int mSelectedPosition;

    public String[] getListVideoData() {
        return mOptionList;
    }

    private OnDataOptionClickListener mListener;

    public DataOptionAdapter(OnDataOptionClickListener listener, String[] optionList, int selectedPosition) {
        this.mListener = listener;
        mOptionList = optionList;
        mSelectedPosition = selectedPosition;
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

    public DataOptionAdapter() {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_option, parent, false);
        return new DataOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((DataOptionViewHolder) holder).bindView(position, mOptionList[position], mSelectedPosition == position, mListener);
    }

    @Override
    public int getItemCount() {
        return mOptionList.length;
    }
}
