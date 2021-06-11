package com.pdfconverter.jpg2pdf.pdf.converter.ui.recent;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class RecentViewModel extends BaseViewModel<RecentNavigator> {

    private ArrayList<RecentData> mListFile = new ArrayList<>();
    private MutableLiveData<List<RecentData>> mListFileLiveData = new MutableLiveData<>();

    public RecentViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<RecentData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public void getFileList() {
        getCompositeDisposable().add(getDataManager()
                .getListRecent()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null && response.size() > 0) {
                        mListFileLiveData.postValue(response);
                    } else {
                        mListFileLiveData.postValue(new ArrayList<>());
                    }
                }, throwable -> {
                    mListFileLiveData.postValue(new ArrayList<>());
                })
        );
    }
}
