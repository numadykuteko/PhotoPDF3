package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class FileListSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FileListAdapter";
    private List<FileData> mFileList = new ArrayList<FileData>();
    private List<FileData> mSelectedList = new ArrayList<FileData>();

    public List<FileData> getListVideoData() {
        return mFileList;
    }

    private OnFileItemClickListener mListener;

    public FileListSelectAdapter(OnFileItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<FileData> videoList) {
        mFileList = new ArrayList<>();
        mFileList.addAll(videoList);
        notifyDataSetChanged();
    }

    public int getNumberSelectedFile() {
        return mSelectedList.size();
    }

    public List<FileData> getSelectedList() {
        return mSelectedList;
    }

    public void removeSelectedList() {
        mSelectedList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void revertData(int position) {
        if (mSelectedList.contains(mFileList.get(position))) {
            mSelectedList.remove(mFileList.get(position));
        } else {
            mSelectedList.add(mFileList.get(position));
        }

        notifyItemChanged(position);
    }

    public boolean isSelected(int position) {
        return mSelectedList.contains(mSelectedList.get(position));
    }

    public FileListSelectAdapter() {
        mSelectedList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_file_view, parent, false);
        return new FileListSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FileListSelectViewHolder) holder).bindView(position, mFileList.get(position), mSelectedList.contains(mFileList.get(position)), mListener);
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
