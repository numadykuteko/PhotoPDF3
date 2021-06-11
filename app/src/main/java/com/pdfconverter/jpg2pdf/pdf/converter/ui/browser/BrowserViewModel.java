package com.pdfconverter.jpg2pdf.pdf.converter.ui.browser;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class BrowserViewModel extends BaseViewModel<BrowserNavigator> {
    private FileData mCurrentPath;
    private BrowserFileAsyncTask mAsyncTask;

    private List<FileData> mListFile;
    private MutableLiveData<List<FileData>> mListFileLiveData = new MutableLiveData<>();
    public MutableLiveData<List<FileData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public BrowserViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCurrentPath(FileData currentPath) {
        this.mCurrentPath = currentPath;
    }

    public void getFileList() {
        mAsyncTask = new BrowserFileAsyncTask(getApplication(), result -> {
            mListFile = new ArrayList<>(result);

            mListFileLiveData.postValue(mListFile);
            }, mCurrentPath);
        mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
