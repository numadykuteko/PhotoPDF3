package com.pdfconverter.jpg2pdf.pdf.converter.ui.split;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitFileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemSplitActionListener;

import java.util.ArrayList;

public class SplitPdfAdapter extends RecyclerView.Adapter<SplitPdfViewHolder> {
    private ArrayList<SplitFileData> mListData = new ArrayList<>();
    private OnItemSplitActionListener mListener;

    public SplitPdfAdapter(OnItemSplitActionListener listener) {
        mListener = listener;
    }

    public void setFileData(ArrayList<SplitFileData> listFileData) {
        if (listFileData == null) return;
        mListData.clear();
        mListData.addAll(listFileData);
        notifyDataSetChanged();
    }

    public void addDataList(ArrayList<SplitFileData> listFileData) {
        if (listFileData == null) return;
        mListData.addAll(listFileData);
        notifyDataSetChanged();
    }

    public void addData(SplitFileData listFileData) {
        if (listFileData == null) return;
        mListData.add(listFileData);
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
    public SplitPdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_split_file, parent, false);
        return new SplitPdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitPdfViewHolder holder, int position) {
        holder.bindView(position, mListData.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public interface ItemTouchListener {
        void onMove(int currentPosition, int newPosition);
    }
}
