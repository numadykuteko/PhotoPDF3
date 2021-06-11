package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageData;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ImageListSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FileListAdapter";
    private List<ImageData> mFileList = new ArrayList<ImageData>();
    private List<Integer> mSelectedList = new ArrayList<Integer>();

    public List<ImageData> getListVideoData() {
        return mFileList;
    }

    private OnFileItemClickListener mListener;

    public ImageListSelectAdapter(OnFileItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<ImageData> imageList) {
        mFileList = new ArrayList<>();
        mFileList.addAll(imageList);
        notifyDataSetChanged();
    }

    public int getNumberSelectedFile() {
        return mSelectedList.size();
    }

    public List<Integer> getSelectedList() {
        return mSelectedList;
    }

    public void removeSelectedList() {
        mSelectedList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addToFirstPosition(ImageData imageData) {
        if (mFileList.size() == 0) {
            mFileList.add(0, new ImageData());
        }

        mFileList.add(1, imageData);
        if (mSelectedList.size() > 0) {
            for (int i = 0; i < mSelectedList.size(); i++) {
                mSelectedList.set(i, mSelectedList.get(i) + 1);
            }
        }

        mSelectedList.add(1);
        notifyDataSetChanged();
    }

    public void revertData(int position) {
        if (mSelectedList.contains(position)) {
            int indexOfPosition = mSelectedList.indexOf(position);
            mSelectedList.remove(indexOfPosition);
            for (int i = 0; i < mSelectedList.size(); i++) {
                notifyItemChanged(mSelectedList.get(i));
            }
            notifyItemChanged(position);
        } else {
            mSelectedList.add(position);
            notifyItemChanged(position);
        }
    }

    public ImageListSelectAdapter() {
        mSelectedList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_selector, parent, false);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width = layoutParams.width = (int) (parent.getWidth() / 3.1);
        layoutParams.height = (int) (width * 1.0);

        view.setLayoutParams(layoutParams);

        return new ImageListSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ImageListSelectViewHolder) holder).bindView(position, mFileList.get(position), mSelectedList.indexOf(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
