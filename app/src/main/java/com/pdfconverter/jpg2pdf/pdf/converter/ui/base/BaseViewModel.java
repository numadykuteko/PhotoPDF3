package com.pdfconverter.jpg2pdf.pdf.converter.ui.base;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.RecentData;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtilAsyncTask;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.scheduler.SchedulerProvider;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.scheduler.SchedulerProviderInterface;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseViewModel<N> extends AndroidViewModel implements FileUtilAsyncTask.FileListener {

    private DataManager mDataManager;
    private CompositeDisposable mCompositeDisposable;
    private WeakReference<N> mNavigator;
    private ObservableBoolean mIsLoading = new ObservableBoolean();
    private SchedulerProviderInterface mSchedulerProviderInterface;

    private MutableLiveData<List<FileData>> mListFileSelectorLiveData = new MutableLiveData<>();
    public MutableLiveData<List<FileData>> getListFileSelectorLiveData() {
        return mListFileSelectorLiveData;
    }

    public BaseViewModel(@NonNull Application application) {
        super(application);
        mCompositeDisposable = new CompositeDisposable();
        mSchedulerProviderInterface = new SchedulerProvider();

        mDataManager = DataManager.getInstance(application);
    }

    @Override
    protected void onCleared() {
        mCompositeDisposable.dispose();
        super.onCleared();
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public SchedulerProviderInterface getSchedulerProvider() {
        return mSchedulerProviderInterface;
    }

    public void setDataManager(DataManager appDataManager) {
        this.mDataManager = appDataManager;
    }

    public DataManager getDataManager() {
        return mDataManager;
    }

    public N getNavigator() {
        return mNavigator.get();
    }

    public void setNavigator(N navigator) {
        this.mNavigator = new WeakReference<>(navigator);
    }

    public ObservableBoolean getIsLoading() {
        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading.set(isLoading);
    }

    public void getFileList(String fileType, int order) {
        FileUtilAsyncTask asyncTask = new FileUtilAsyncTask(getApplication(), this, order, fileType, false, false);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getUnlockedFileList() {
        FileUtilAsyncTask asyncTask = new FileUtilAsyncTask(getApplication(), this, 0, DataConstants.FILE_TYPE_PDF, true,false);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getLockedFileList() {
        FileUtilAsyncTask asyncTask = new FileUtilAsyncTask(getApplication(), this, 0, DataConstants.FILE_TYPE_PDF, true,true);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void saveRecent(String filePath, String type) {
        getCompositeDisposable().add(getDataManager()
                .saveRecent(filePath, type)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void clearBookmarked(String filePath) {
        getCompositeDisposable().add(getDataManager()
                .clearBookmarkByPath(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void saveBookmarked(String filePath) {
        getCompositeDisposable().add(getDataManager()
                .saveBookmark(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void saveRecent(RecentData recentData) {
        getCompositeDisposable().add(getDataManager()
                .saveRecent(recentData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void clearRecent(String filePath) {
        getCompositeDisposable().add(getDataManager()
                .clearRecent(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {

                }, throwable -> {

                })
        );
    }

    public void clearSavedData(String filePath) {
        try {
            clearBookmarked(filePath);
            clearRecent(filePath);
        } catch (Exception ignored) {

        }
    }

    public void updateSavedData(String filePath, String newFilePath) {
        getCompositeDisposable().add(getDataManager()
                .getBookmarkByPath(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null && response.getFilePath().equals(filePath)) {
                        clearBookmarked(filePath);
                        saveBookmarked(newFilePath);
                    }
                }, throwable -> {

                })
        );

        getCompositeDisposable().add(getDataManager()
                .getRecentByPath(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null && response.getFilePath().equals(filePath)) {
                        clearRecent(filePath);
                        saveRecent(newFilePath, response.getActionType());
                    }
                }, throwable -> {

                })
        );
    }

    public void startCheckIsFileBookmarked(String filePath, int position, OnCheckBookmarkListener listener) {
        getCompositeDisposable().add(getDataManager()
                .getBookmarkByPath(filePath)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null && response.getFilePath().equals(filePath)) {
                        listener.onResult(position, true);
                    } else {
                        listener.onResult(position, false);
                    }
                }, throwable -> {
                    listener.onResult(position, false);
                })
        );
    }

    @Override
    public void loadDone(List<FileData> result) {
        mListFileSelectorLiveData.postValue(result);
    }

    public interface OnCheckBookmarkListener {
        void onResult(int position, boolean isBookmarked);
    }
}
