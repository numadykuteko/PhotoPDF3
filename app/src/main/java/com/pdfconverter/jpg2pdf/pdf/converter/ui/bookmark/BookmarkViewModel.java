package com.pdfconverter.jpg2pdf.pdf.converter.ui.bookmark;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.BookmarkData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class BookmarkViewModel extends BaseViewModel<BookmarkNavigator> {

    private MutableLiveData<List<BookmarkData>> mListFileLiveData = new MutableLiveData<>();
    public MutableLiveData<List<BookmarkData>> getListFileLiveData() {
        return mListFileLiveData;
    }

    public BookmarkViewModel(@NonNull Application application) {
        super(application);
    }

    public void getFileList() {
        getCompositeDisposable().add(getDataManager()
                .getListBookmark()
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
