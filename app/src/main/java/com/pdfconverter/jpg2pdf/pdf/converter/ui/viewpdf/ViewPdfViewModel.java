package com.pdfconverter.jpg2pdf.pdf.converter.ui.viewpdf;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;

public class ViewPdfViewModel extends BaseViewModel<ViewPdfNavigator> {
    private boolean mIsBookmarked = false;
    private final  MutableLiveData<Boolean> mIsBookmarkedLiveData = new MutableLiveData<>();
    public ViewPdfViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Boolean> getIsBookmarkedLiveData() {
        return mIsBookmarkedLiveData;
    }

    public void startCheckIsBookmarked(String filePath) {
        getCompositeDisposable().add(getDataManager()
                .getBookmarkByPath(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null && response.getFilePath().equals(filePath)) {
                        mIsBookmarked = true;
                        mIsBookmarkedLiveData.postValue(true);
                    } else {
                        mIsBookmarked = false;
                        mIsBookmarkedLiveData.postValue(false);
                    }
                }, throwable -> {

                })
        );
    }

    public void revertBookmarked(String filePath) {

        if (mIsBookmarked) {
            getCompositeDisposable().add(getDataManager()
                    .clearBookmarkByPath(filePath)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(response -> {
                        mIsBookmarkedLiveData.postValue(false);
                        mIsBookmarked = false;
                    }, throwable -> {

                    })
            );
        } else {
            getCompositeDisposable().add(getDataManager()
                    .saveBookmark(filePath)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(response -> {
                        mIsBookmarkedLiveData.postValue(true);
                        mIsBookmarked = true;
                    }, throwable -> {

                    })
            );
        }
    }
}
