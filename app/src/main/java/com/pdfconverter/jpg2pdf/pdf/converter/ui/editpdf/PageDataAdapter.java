package com.pdfconverter.jpg2pdf.pdf.converter.ui.editpdf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnEditPageItemClickListener;

import java.util.ArrayList;
import java.util.Collections;

public class PageDataAdapter extends RecyclerView.Adapter<PageDataViewHolder> {
    private ArrayList<PageData> mListData = new ArrayList<>();
    private OnEditPageItemClickListener mListener;

    public PageDataAdapter(OnEditPageItemClickListener listener) {
        mListener = listener;
    }

    public void setPageData(ArrayList<PageData> pageData) {
        if (pageData == null) return;
        mListData.clear();
        mListData.addAll(pageData);
        notifyDataSetChanged();
    }

    public void addData(PageData pageData) {
        if (pageData == null) return;
        mListData.add(pageData);
        notifyItemInserted(mListData.size());
    }

    public void addData(int position, PageData pageData) {
        if (pageData == null) return;
        mListData.add(position, pageData);
        notifyItemInserted(position);
    }

    public void removeData(int position) {
        if (position < mListData.size()) {
            mListData.remove(position);
            notifyDataSetChanged();
        }
    }

    public void swapData(int oldPosition, int newPosition) {
        Collections.swap(mListData, oldPosition, newPosition);
        notifyItemChanged(newPosition);
        notifyItemChanged(oldPosition);
    }

    public void removeAllData() {
        mListData = new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PageDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_pdf, parent, false);

        return new PageDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageDataViewHolder holder, int position) {
        holder.bindView(position, mListData.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }
}
