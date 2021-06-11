package com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;

import java.util.ArrayList;

public class FindDuplicateManager extends AsyncTask<Void, Void, Void> {
    private final FindDuplicateListener mListener;
    private final ArrayList<PageData> mPageList;
    private ArrayList<String> mDuplicateFound;

    public FindDuplicateManager(ArrayList<PageData> pageList, FindDuplicateListener listener) {
        mDuplicateFound = new ArrayList<>();
        this.mPageList = pageList;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onFindStart();
    }


    @Override
    protected Void doInBackground(Void... voids) {
        mDuplicateFound = new ArrayList<>();
        for (int i = mPageList.size() - 1; i >= 1; i --) {
            Bitmap currentBitmap = mPageList.get(i).getBitmap();

            for (int j = i - 1; j >= 0; j --) {
                Bitmap compareBitmap = mPageList.get(j).getBitmap();

                if (currentBitmap.sameAs(compareBitmap)) {
                    mDuplicateFound.add(i + "," + j);
                    break;
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void avoid) {
        super.onPostExecute(avoid);
        mListener.onFindSuccess(mDuplicateFound);
    }
}