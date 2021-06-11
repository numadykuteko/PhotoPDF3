package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemWithOptionClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.nativeads.NativeAdsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class FileListNoAdsWithOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FileListAdapter";
    private List<FileData> mFileList = new ArrayList<FileData>();
    private int mCurrentItem = -1;

    public List<FileData> getListVideoData() {
        return mFileList;
    }

    private OnFileItemWithOptionClickListener mListener;

    public FileListNoAdsWithOptionAdapter(OnFileItemWithOptionClickListener listener) {
        this.mListener = listener;
    }

    public static final int ADS_INDEX = -1;

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

    public FileListNoAdsWithOptionAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        if (position == ADS_INDEX) {
            return 1;
        } else {
            return super.getItemViewType(position);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_view, parent, false);
            return new FileListViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_small_native_control, parent, false);
            return new NativeAdsViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((FileListViewHolder) holder).bindView(position, mFileList.get(position), mCurrentItem, mListener);
        } else {
            ((NativeAdsViewHolder) holder).bindView(false);
        }
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
