package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageData;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class ImageToPdfViewModel extends BaseViewModel {

    private static final String TAG = "ImageToPdfViewModel";
    private static final String PREFIX_URI = "content://com.pdfconverter.jpg2pdf.pdf.converter.provider/images";
    private MutableLiveData<ArrayList<ImageData>> mListImage = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ImageData>> getListImage() {
        return mListImage;
    }

    private MutableLiveData<ArrayList<ImageData>> mListLocalImage = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ImageData>> getListLocalImage() {
        return mListLocalImage;
    }

    public ImageToPdfViewModel(@NonNull Application application) {
        super(application);
    }

    @SuppressLint("Recycle")
    public void startGetLocalImage() {
        AsyncTask.execute(() -> {
            Cursor cursor = null;

            try {
                Uri uri;
                int column_index_data;
                ArrayList<ImageData> listOfAllImages = new ArrayList<ImageData>();
                String absolutePathOfImage;
                uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                String[] projection = { MediaStore.MediaColumns.DATA};

                String orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";

                cursor = getApplication().getContentResolver().query(uri, projection, null,
                        null, orderBy);

                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                while (cursor.moveToNext()) {
                    absolutePathOfImage = cursor.getString(column_index_data);
                    ImageData imageData = new ImageData();
                    imageData.setImagePath(absolutePathOfImage);
                    listOfAllImages.add(imageData);
                }

                mListLocalImage.postValue(listOfAllImages);
            } catch (Exception e) {
                mListLocalImage.postValue(new ArrayList<>());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }

    public void addNewImage(Context context, ImageData imageData) {
        ArrayList<ImageData> arrayList = mListImage.getValue();
        if (arrayList == null) arrayList = new ArrayList<>();
        copyFile(context, imageData);
        arrayList.add(imageData);
        mListImage.setValue(arrayList);
    }

    public void addNewListImage(Context context, ArrayList<ImageData> imageDatas) {
        ArrayList<ImageData> arrayList = mListImage.getValue();
        if (arrayList == null) arrayList = new ArrayList<>();
        for (ImageData data : imageDatas) {
            copyFile(context, data);
        }
        arrayList.addAll(imageDatas);
        mListImage.setValue(arrayList);
    }

    private void copyFile(Context context, ImageData imageData) {
        if (imageData.getImagePath() == null) {
            return;
        }

        try {
            Uri oldFile;
            if (imageData.getImagePath().contains("content:/") || FileUtils.checkFileExist(imageData.getImagePath())) {
                oldFile = Uri.parse(imageData.getImagePath());
            } else {
                oldFile = Uri.fromFile(new File(imageData.getImagePath()));
            }

            if (oldFile == null) return;

            File imageFile = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File newFile;
            String id = String.valueOf(imageData.getId()) != null && String.valueOf(imageData.getId()).length() > 3 ? String.valueOf(imageData.getId()) : String.valueOf(System.nanoTime());
            newFile = File.createTempFile(id, ".jpg", imageFile);
            createFileFromStream(context, oldFile, newFile);

            imageData.setImagePath(newFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeImage(ImageData imageData) {
        ArrayList<ImageData> arrayList = mListImage.getValue();
        if (arrayList == null) arrayList = new ArrayList<>();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (arrayList.get(i).getId() == imageData.getId()) {
                arrayList.remove(i);
                break;
            }
        }
        mListImage.setValue(arrayList);
        Log.d(TAG, "removeImage " + arrayList.size());
    }

    public void removeAllImage() {
        mListImage.setValue(new ArrayList<>());
    }

    public void deleteFolder(File folder) {
        if (folder == null) return;
        try {
            if (folder.isDirectory()) {
                if (folder.listFiles() != null) {
                    for (File ct : folder.listFiles()) {
                        deleteFolder(ct);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void createFileFromStream(Context context, Uri sourceUri, File destination) throws Exception {
        try (InputStream ins = context.getContentResolver().openInputStream(sourceUri)) {
            OutputStream os = new FileOutputStream(destination);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        }
    }

    public void swapImageItem(int currentPosition, int newPosition) {
        if (mListImage.getValue() != null) {
            if (currentPosition < newPosition) {
                for (int i = currentPosition; i < newPosition; i++) {
                    Collections.swap(mListImage.getValue(), i, i + 1);
                }
            } else if (currentPosition > newPosition) {
                for (int i = currentPosition; i > newPosition; i--) {
                    Collections.swap(mListImage.getValue(), i, i - 1);
                }
            }
        }
    }

    public void createPdfFile() {
    }
}
