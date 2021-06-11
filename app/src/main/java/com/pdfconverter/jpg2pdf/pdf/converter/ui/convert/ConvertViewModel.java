package com.pdfconverter.jpg2pdf.pdf.converter.ui.convert;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.search.SearchFileAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class ConvertViewModel extends BaseViewModel<ConvertNavigator> {
    private int mType;
    private MutableLiveData<List<FileData>> mListFileLiveData = new MutableLiveData<>();
    public MutableLiveData<List<FileData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    private SearchFileAsyncTask mAsyncTask;

    public ConvertViewModel(@NonNull Application application) {
        super(application);
    }

    public void setType(int type) {
        mType = type;
    }

    public void getFileList() {
        String typeFile;

        if (mType == 0) {
            typeFile = DataConstants.FILE_TYPE_WORD;
        } else if (mType == 1) {
            typeFile = DataConstants.FILE_TYPE_EXCEL;
        } else {
            typeFile = DataConstants.FILE_TYPE_TXT;
        }
        mAsyncTask = new SearchFileAsyncTask(getApplication(), result -> {
            List<FileData> mListFile = new ArrayList<>(result);
            mListFileLiveData.postValue(mListFile);
        }, typeFile);
        mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
