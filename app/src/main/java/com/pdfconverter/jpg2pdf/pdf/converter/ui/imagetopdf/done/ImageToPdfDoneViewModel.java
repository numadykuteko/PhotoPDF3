package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.done;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseViewModel;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.File;

public class ImageToPdfDoneViewModel extends BaseViewModel {

    public static final int CREATE_PDF_SUCCESS = 1;
    public static final int CREATING_PDF_FILE = 0;
    public static final int CREATE_PDF_FALSE = -1;
    private static final String TAG = "CreatePdfViewModel";

    private MutableLiveData<Integer> mStatusCreatePDF = new MutableLiveData<>(0);
    private MutableLiveData<Integer> mStatusPercent = new MutableLiveData<>();
    private File mFilePdf;
    private ImageToPDFOptions mImageToPDFOptions;

    public void setImageToPDFOptions(ImageToPDFOptions imageToPDFOptions) {
        this.mImageToPDFOptions = imageToPDFOptions;
    }

    public MutableLiveData<Integer> getStatusCreatePDF() {
        return mStatusCreatePDF;
    }

    public MutableLiveData<Integer> getStatusPercent() {
        return mStatusPercent;
    }

    public File getOutputFile() {
        return mFilePdf;
    }

    public void createPdf() {
        mStatusCreatePDF.postValue(CREATING_PDF_FILE);
        mStatusPercent.postValue(0);
        setupFile();
        ImageToPdfTask imageToPdfTask = new ImageToPdfTask(mImageToPDFOptions, mFilePdf,
                new ImageToPdfTask.OnPDFCreatedInterface() {
                    @Override
                    public void updateStatus(int percent) {
                        mStatusPercent.postValue(percent);
                        if (percent == 100) {
                            mStatusCreatePDF.postValue(CREATE_PDF_SUCCESS);
                            saveRecent(mFilePdf.getAbsolutePath(), getApplication().getString(R.string.image_to_pdf));
                        }
                    }
                    @Override
                    public void createPdfFalse() {
                        mStatusCreatePDF.postValue(CREATE_PDF_FALSE);
                    }
                });
        imageToPdfTask.execute();
    }

    private void setupFile() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DataConstants.PDF_DIRECTORY);
        if (!dir.exists()) {
            boolean isDirectoryCreated = dir.mkdir();
            if (!isDirectoryCreated) {
                Log.e("Error", "Directory could not be created");
            }
        }
        String name = TextUtils.isEmpty(mImageToPDFOptions.getOutFileName()) ? FileUtils.getDefaultOutputName(DataConstants.FILE_TYPE_PDF)
                : mImageToPDFOptions.getOutFileName();
        mFilePdf = new File(dir, name + ImageToPdfConstants.pdfExtension);
    }

    public ImageToPdfDoneViewModel(@NonNull Application application) {
        super(application);
    }
}
