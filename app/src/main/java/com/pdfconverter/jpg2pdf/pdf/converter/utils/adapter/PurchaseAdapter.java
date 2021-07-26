package com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pdfconverter.jpg2pdf.pdf.converter.R;

import java.util.ArrayList;
import java.util.List;

public class PurchaseAdapter extends PagerAdapter {
    private List<Integer> mDescriptionList;
    private List<Integer> mImageList;

    public PurchaseAdapter() {
        this.mDescriptionList = new ArrayList<>();
        this.mImageList = new ArrayList<>();

        mImageList.add(R.drawable.ic_purchase_1);
        mImageList.add(R.drawable.ic_purchase_2);
        mImageList.add(R.drawable.ic_purchase_3);

        mDescriptionList.add(R.string.purchase_introduce_1);
        mDescriptionList.add(R.string.purchase_introduce_2);
        mDescriptionList.add(R.string.purchase_introduce_3);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        Context context = collection.getContext();
        LayoutInflater inflater = (LayoutInflater) collection.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_purchase, null);

        ImageView imageView = view.findViewById(R.id.item_image_view);
        TextView textView = view.findViewById(R.id.item_name_view);

        imageView.setImageDrawable(context.getDrawable(mImageList.get(position)));
        textView.setText(context.getString(mDescriptionList.get(position)));

        collection.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mDescriptionList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}