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

public class FileListNoAdsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FileListAdapter";
    private List<FileData> mFileList = new ArrayList<FileData>();
    private int mCurrentItem = -1;

    public List<FileData> getListData() {
        return mFileList;
    }

    private OnFileItemClickListener mListener;

    public FileListNoAdsAdapter(OnFileItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<FileData> videoList) {
        mFileList = new ArrayList<>();
        mCurrentItem = -1;
        mFileList.addAll(videoList);
        notifyDataSetChanged();
    }

    public void setCurrentItem(int position) {
        int temp = mCurrentItem;
        mCurrentItem = position;

        notifyItemChanged(temp);
        notifyItemChanged(mCurrentItem);
    }

    public void clearAllData() {
        mFileList.clear();
        notifyDataSetChanged();
    }

    public void clearData(int position) {
        if (position < 0 || position > mFileList.size())   return;
        if (mCurrentItem == position) mCurrentItem = -1;
        mFileList.remove(position);
        notifyDataSetChanged();
    }

    public void updateData(int position, FileData fileData) {
        mFileList.set(position, fileData);
        notifyItemChanged(position);
    }

    public FileListNoAdsAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_view_no_option, parent, false);
        return new FileListNoAdsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((FileListNoAdsViewHolder) holder).bindView(position, mFileList.get(position), mCurrentItem, mListener);
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
