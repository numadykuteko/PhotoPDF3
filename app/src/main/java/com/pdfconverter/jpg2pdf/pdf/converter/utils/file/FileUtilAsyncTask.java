package com.pdfconverter.jpg2pdf.pdf.converter.utils.file;

import android.content.Context;
import android.os.AsyncTask;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;

import java.util.ArrayList;
import java.util.List;

public class FileUtilAsyncTask extends AsyncTask<Object, Object, Object> {

    private final Context mContext;
    private FileListener mListener;
    private int mOrder;
    private String mFileType;
    private boolean mIsLocked;
    private boolean mIsSupportLock;

    public FileUtilAsyncTask(Context context, FileListener listener, int order, String fileType, boolean isSupportLock, boolean isLocked) {
        mContext = context;
        mListener = listener;
        this.mFileType = fileType;
        this.mIsLocked = isLocked;
        this.mOrder = order;
        this.mIsSupportLock = isSupportLock;
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
            ArrayList<FileData> allData;
            if (mIsSupportLock) {
                if (mIsLocked) {
                    allData = FileUtils.getAllLockedFileList(mContext);
                } else {
                    allData = FileUtils.getAllUnlockedFileList(mContext);
                }
            } else {
                allData = FileUtils.getAllExternalFileList(mContext, mFileType, mOrder);
            }

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

    public interface FileListener {
        void loadDone(List<FileData> result);
    }
}
