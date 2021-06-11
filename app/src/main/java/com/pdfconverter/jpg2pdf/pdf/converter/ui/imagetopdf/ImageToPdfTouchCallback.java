package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ImageToPdfTouchCallback extends ItemTouchHelper.Callback {

    private ImageAdapter.ItemTouchListener mItemTouchListener;

    public ImageToPdfTouchCallback(ImageAdapter.ItemTouchListener itemTouchListener) {
        mItemTouchListener = itemTouchListener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        ImageAdapter imageAdapter = (ImageAdapter) recyclerView.getAdapter();
        int size = imageAdapter.getItemCount();
        if (size - 1 == viewHolder.getAdapterPosition()) {
            return makeMovementFlags(0, 0);
        }
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        // get the viewHolder's and target's positions in your adapter data, swap them
        // Collections.swap(/*RecyclerView.Adapter's data collection*/, viewHolder.getAdapterPosition(), target.getAdapterPosition());
        // and notify the adapter that its dataset has changed
        //_adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        mItemTouchListener.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
