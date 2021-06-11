package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftotext;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemTextActionListener;

import java.util.ArrayList;

public class PdfToTextAdapter extends RecyclerView.Adapter<PdfToTextViewHolder> {
    private ArrayList<TextExtractData> mListData = new ArrayList<>();
    private OnItemTextActionListener mListener;

    public PdfToTextAdapter(OnItemTextActionListener listener) {
        mListener = listener;
    }

    public void setFileData(ArrayList<TextExtractData> listFileData) {
        if (listFileData == null) return;
        mListData.clear();
        mListData.addAll(listFileData);
        notifyDataSetChanged();
    }

    public void addData(TextExtractData listData) {
        if (listData == null) return;
        mListData.add(listData);
        notifyItemInserted(mListData.size());
    }

    public void removeData(int position) {
        if (position < mListData.size()) {
            mListData.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeAllData() {
        mListData = new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PdfToTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exact_text, parent, false);
        return new PdfToTextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfToTextViewHolder holder, int position) {
        holder.bindView(position, mListData.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }
}
