package com.pdfconverter.jpg2pdf.pdf.converter.ui.pdftoimage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnItemImageActionListener;

import java.util.ArrayList;

public class PdfToImageAdapter extends RecyclerView.Adapter<PdfToImageViewHolder> {
    private ArrayList<ImageExtractData> mListData = new ArrayList<>();
    private OnItemImageActionListener mListener;

    public PdfToImageAdapter(OnItemImageActionListener listener) {
        mListener = listener;
    }

    public void setFileData(ArrayList<ImageExtractData> listFileData) {
        if (listFileData == null) return;
        mListData.clear();
        mListData.addAll(listFileData);
        notifyDataSetChanged();
    }

    public void addData(ImageExtractData listFileData) {
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
    public PdfToImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_extract_image_file, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width = layoutParams.width = parent.getWidth() / 2;
        layoutParams.height = (int) (width * 1.2);

        view.setLayoutParams(layoutParams);

        return new PdfToImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfToImageViewHolder holder, int position) {
        holder.bindView(position, mListData.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }
}
