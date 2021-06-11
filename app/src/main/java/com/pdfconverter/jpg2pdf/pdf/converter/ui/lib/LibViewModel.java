package com.pdfconverter.jpg2pdf.pdf.converter.ui.lib;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class LibViewModel extends BaseViewModel<LibNavigator> {

    private LibFileAsyncTask mAsyncTask;

    private List<FileData> mListFile;
    private MutableLiveData<List<FileData>> mListFileLiveData = new MutableLiveData<>();
    public MutableLiveData<List<FileData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public LibViewModel(@NonNull Application application) {
        super(application);
    }

    public void getFileList(int order) {
        mAsyncTask = new LibFileAsyncTask(getApplication(), result -> {
            mListFile = new ArrayList<>(result);

            mListFileLiveData.postValue(mListFile);
            }, order);
        mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
