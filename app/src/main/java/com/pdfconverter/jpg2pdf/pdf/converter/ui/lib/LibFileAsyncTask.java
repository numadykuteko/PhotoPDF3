package com.pdfconverter.jpg2pdf.pdf.converter.ui.lib;

import android.content.Context;
import android.os.AsyncTask;

import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.search.SearchFileListener;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.util.ArrayList;

public class LibFileAsyncTask extends AsyncTask<Object, Object, Object> {

    private final Context mContext;
    private SearchFileListener mListener;
    private int mOrder;

    public LibFileAsyncTask(Context context, SearchFileListener listener, int order) {
        mContext = context;
        mListener = listener;
        this.mOrder = order;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            ArrayList<FileData> allData = FileUtils.getAllExternalFileList(mContext, DataConstants.FILE_TYPE_PDF, mOrder);

            if (!isCancelled() && mListener != null) {
                mListener.loadDone(allData);
            }
        } catch (Exception e) {
            mListener.loadDone(new ArrayList<>());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
