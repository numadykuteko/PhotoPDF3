package com.pdfconverter.jpg2pdf.pdf.converter.ui.search;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends BaseViewModel<SearchNavigator> {
    private int mTypeSearch;
    private String mKeyword;
    private String mCurrentKeyword = null;
    private boolean mIsLoadingList = false;
    private boolean mIsPushingList = false;

    private ArrayList<FileData> mListFile = new ArrayList<>();
    private MutableLiveData<List<FileData>> mListFileLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLoadListLiveData = new MutableLiveData<>();

    private SearchFileAsyncTask mAsyncTask;

    public SearchViewModel(@NonNull Application application) {
        super(application);
    }

    public void setTypeSearch(int typeSearch, String keyword) {
        mTypeSearch = typeSearch;
        if (keyword != null) {
            mKeyword = keyword.toLowerCase().trim();
        } else {
            mKeyword = "";
        }
    }

    public List<FileData> getListFile() {
        return mListFile;
    }

    public MutableLiveData<List<FileData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public MutableLiveData<Boolean> getLoadListLiveData() {
        return mLoadListLiveData;
    }

    public void startSeeding(boolean needReloadList, boolean pullRefresh) {

        if (mIsLoadingList) return;

        if (needReloadList) {
            String fileType;

            mIsLoadingList = true;
            mLoadListLiveData.postValue(true);

            if (mTypeSearch == DataConstants.INDEX_TYPE_PDF) {
                fileType = DataConstants.FILE_TYPE_PDF;
            } else if (mTypeSearch == DataConstants.INDEX_TYPE_WORD) {
                fileType = DataConstants.FILE_TYPE_WORD;
            } else {
                fileType = DataConstants.FILE_TYPE_EXCEL;
            }

            mAsyncTask = new SearchFileAsyncTask(getApplication(), result -> {
                mListFile = new ArrayList<>(result);

                mLoadListLiveData.postValue(true);
                mIsLoadingList = false;

                pushResult(pullRefresh);
            }, fileType);
            mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            pushResult(pullRefresh);
        }
    }

    private void pushResult(boolean pullRefresh) {
        if (mIsPushingList && !pullRefresh) {
            return;
        }

        mIsPushingList = true;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mKeyword);

        mCurrentKeyword = stringBuilder.toString();

        ArrayList<FileData> outputList = new ArrayList<>();

        if (mListFile.size() > 0) {
            for (FileData fileData : mListFile) {
                if (mCurrentKeyword.length() > 0 && fileData.getDisplayName().toLowerCase().equals("no name")) {
                    continue;
                }

                if (fileData.getDisplayName().toLowerCase().contains(mCurrentKeyword)) {
                    outputList.add(fileData);
                }
            }
        }

        mListFileLiveData.postValue(outputList);
        mIsPushingList = false;

        if (!mCurrentKeyword.equals(mKeyword)) {
            pushResult(pullRefresh);
        }
    }
}
