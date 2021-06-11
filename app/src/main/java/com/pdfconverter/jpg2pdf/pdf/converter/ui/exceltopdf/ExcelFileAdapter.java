package com.pdfconverter.jpg2pdf.pdf.converter.ui.exceltopdf;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemFileActionListener;

import java.util.ArrayList;
import java.util.Collections;

public class ExcelFileAdapter extends RecyclerView.Adapter<ExcelFileViewHolder> {
    private ArrayList<DocumentData> mListData = new ArrayList<>();
    private OnItemFileActionListener mListener;

    private ItemTouchListener mItemTouchListener = (currentPosition, newPosition) -> {
        mListener.onSwap(currentPosition, newPosition);
        if (currentPosition < newPosition) {
            for (int i = currentPosition; i < newPosition; i++) {
                Collections.swap(mListData, i, i + 1);
            }
        } else if (currentPosition > newPosition) {
            for (int i = currentPosition; i > newPosition; i--) {
                Collections.swap(mListData, i, i - 1);
            }
        }
        notifyItemMoved(currentPosition, newPosition);
    };

    public ExcelFileAdapter(OnItemFileActionListener listener) {
        mListener = listener;
    }

    public ExcelFileAdapter.ItemTouchListener getItemTouchListener() {
        return mItemTouchListener;
    }

    public void setFileData(ArrayList<DocumentData> listFileData) {
        if (listFileData == null) return;
        mListData.clear();
        mListData.addAll(listFileData);
        notifyDataSetChanged();
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
    public ExcelFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_excel_file, parent, false);
        return new ExcelFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcelFileViewHolder holder, int position) {
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
