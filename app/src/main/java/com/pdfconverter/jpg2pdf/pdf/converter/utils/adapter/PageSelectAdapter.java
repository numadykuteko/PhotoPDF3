package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class PageSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "PageSelectAdapter";
    private List<String> mPageList = new ArrayList<String>();
    private ArrayList<Integer> mSelectedList = new ArrayList<Integer>();

    public List<String> getListVideoData() {
        return mPageList;
    }

    private OnFileItemClickListener mListener;

    public PageSelectAdapter(OnFileItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<String> videoList) {
        mPageList = new ArrayList<>();
        mPageList.addAll(videoList);
        notifyDataSetChanged();
    }

    public int getNumberSelectedFile() {
        return mSelectedList.size();
    }

    public ArrayList<Integer> getSelectedList() {
        return mSelectedList;
    }

    public void removeSelectedList() {
        mSelectedList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void revertData(int position) {
        if (mSelectedList.contains(position)) {
            mSelectedList.remove(mSelectedList.indexOf(position));
        } else {
            mSelectedList.add(position);
        }

        notifyItemChanged(position);
    }

    public boolean isSelected(int position) {
        return mSelectedList.contains(position);
    }

    public PageSelectAdapter() {
        mSelectedList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_page_view, parent, false);
        return new PageSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((PageSelectViewHolder) holder).bindView(position, mPageList.get(position), mSelectedList.contains(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mPageList.size();
    }
}
