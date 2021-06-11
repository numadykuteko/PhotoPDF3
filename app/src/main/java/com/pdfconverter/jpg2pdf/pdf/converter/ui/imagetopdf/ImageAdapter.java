package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ItemImageViewBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.cropimage.CropImageActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DeminUtils;

import java.util.ArrayList;
import java.util.Collections;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private static final String TAG = "ImageAdapter";
    private ArrayList<ImageData> mListData = new ArrayList<>();
    public static final int NUMBER_COLUMN = 2;
    public static final String ADAPTER_POSITION = "AdapterPosition";
    public static final String INTENT_DATA_IMAGE = "data_image_path";
    private ImageToPdfViewModel mImageToPdfViewModel;
    private ImageToPdfActivity.StartActivityInterface mStartActivity;
    private OnAddImageListener mOnAddImageListener;
    private boolean mIsStretch = false;

    private ItemTouchListener mItemTouchListener = (currentPosition, newPosition) -> {
        Log.d(TAG, "currentPosition : " + currentPosition + " newPosition : " + newPosition);
        if (currentPosition == mListData.size() - 1 || newPosition == mListData.size() - 1) return;
        mImageToPdfViewModel.swapImageItem(currentPosition, newPosition);
        if (currentPosition < newPosition) {
            for (int i = currentPosition; i < newPosition; i++) {
                Collections.swap(mListData, i, i + 1);
            }
        } else if (currentPosition > newPosition) {
            for (int i = currentPosition; i > newPosition; i--) {
                Collections.swap(mListData, i, i - 1);
            }
        }
        if (currentPosition >= 0 && currentPosition < mListData.size() && newPosition >= 0 && newPosition < mListData.size()) {
            notifyItemMoved(currentPosition, newPosition);
        }
    };

    public ImageAdapter(ImageToPdfViewModel viewModel, ImageToPdfActivity.StartActivityInterface startActivityInterface, OnAddImageListener onAddImageListener) {
        mImageToPdfViewModel = viewModel;
        mStartActivity = startActivityInterface;
        mIsStretch = false;
        mOnAddImageListener = onAddImageListener;
    }

    public ItemTouchListener getItemTouchListener() {
        return mItemTouchListener;
    }

    public void onDestroy() {
        mImageToPdfViewModel = null;
    }

    public void setImageData(ArrayList<ImageData> listImageData) {
        if (listImageData == null) return;
        mListData.clear();
        if (listImageData.size() > 0) {
            for (ImageData imageData : listImageData) {
                mListData.add(new ImageData(imageData.getImagePath(), "", 0, imageData.getId()));
            }
            mListData.add(new ImageData("", "", 0, System.nanoTime()));
        }
    }

    public void clearData() {
        if (mListData != null) {
            mListData.clear();
            notifyDataSetChanged();
        }
    }

    public ArrayList<ImageData> getImageData() {
        return mListData;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemImageViewBinding itemImageViewBinding =
                DataBindingUtil.inflate(inflater, R.layout.item_image_view, parent, false);
        ImageViewHolder imageViewHolder = new ImageViewHolder(itemImageViewBinding);

        int width = (parent.getMeasuredWidth() - DeminUtils.dpToPx(12, parent.getContext())) / NUMBER_COLUMN;
        int height = width * 6 / 5;
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) imageViewHolder.itemView.getLayoutParams();
        params.height = Math.round(height);
        imageViewHolder.itemView.setLayoutParams(params);

        return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Context context = holder.mItemImageViewBinding.getRoot().getContext();
        if (TextUtils.isEmpty(mListData.get(position).getImagePath())) {
            holder.mItemImageViewBinding.setImageData(new ImageData("", "", 0, System.nanoTime()));
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener((v) -> {
                if (mOnAddImageListener != null) {
                    mOnAddImageListener.onAddImage();
                }
            });
        } else {
            ImageData imageData = mListData.get(position);
            holder.mItemImageViewBinding.setImageData(imageData);
            holder.itemView.setClickable(false);
            holder.mItemImageViewBinding.itemCropView.setOnClickListener((v) -> {
                Intent intent = new Intent(v.getContext(), CropImageActivity.class);
                intent.putExtra(ADAPTER_POSITION, holder.getAdapterPosition());
                intent.putExtra(INTENT_DATA_IMAGE, mListData.get(holder.getAdapterPosition()).getImagePath());
                mStartActivity.startActivityForResult(intent, ImageToPdfActivity.CROP_IMAGE_CODE);
            });
            if (mIsStretch) {
                holder.mItemImageViewBinding.thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                holder.mItemImageViewBinding.thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            holder.mItemImageViewBinding.itemDeleteView.setOnClickListener((v) ->
                    ((ImageToPdfActivity) context).removeImage(imageData, holder.getAdapterPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public ArrayList<ImageData> getData() {
        return mListData;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        private ItemImageViewBinding mItemImageViewBinding;

        public ImageViewHolder(@NonNull ItemImageViewBinding itemView) {
            super(itemView.getRoot());
            this.mItemImageViewBinding = itemView;
        }
    }

    public interface ItemTouchListener {
        void onMove(int currentPosition, int newPosition);
    }

    public interface OnAddImageListener {
        void onAddImage();
    }
}
