package com.pdfconverter.jpg2pdf.pdf.converter.utils.merge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.MergePDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("StaticFieldLeak")
public class MergePdfManager extends AsyncTask<Object, Object, Object> {
    private final Context mContext;
    private final MergePdfListener mListener;
    private final MergePDFOptions mOptions;

    public MergePdfManager(Context context, MergePdfListener listener, MergePDFOptions options) {
        mContext = context;
        mListener = listener;
        mOptions = options;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null) {
            mListener.onCreateStart();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mListener != null)
            mListener.onCreateError();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        String outputName = "";
        if (mOptions.getOutFileName() == null || mOptions.getOutFileName().length() == 0) {
            outputName = FileUtils.getDefaultOutputName(DataConstants.FILE_TYPE_PDF);
        } else {
            outputName = mOptions.getOutFileName();
        }

        String finalOutput = DirectoryUtils.getDefaultStorageLocation() + outputName + ".pdf";

        try {
            if (mListener != null) {
                mListener.onUpdateProcess(1);
            }

            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(finalOutput));

            if (mOptions.isPasswordProtected()) {
                copy.setEncryption(mOptions.getPassword().getBytes(),
                        AppConstants.APP_PASSWORD.getBytes(),
                        PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128);
            }

            document.open();

            int numOfPages;
            PdfReader pdfreader;

            for (int i = 0; i < mOptions.getPathList().size(); i++) {
                DocumentData pdfPath = mOptions.getPathList().get(i);
                if (isCancelled()) break;

                pdfreader = new PdfReader(pdfPath.getFilePath());
                numOfPages = pdfreader.getNumberOfPages();
                for (int page = 1; page <= numOfPages; page++)
                    copy.addPage(copy.getImportedPage(pdfreader, page));

                if (mListener != null) {
                    mListener.onUpdateProcess(Math.round((i + 1) / (float) mOptions.getPathList().size() * 100));
                }
            }
            document.close();

            if (!isCancelled()) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onCreateSuccess(finalOutput);
                        }
                    }
                }, 800);
            }

        } catch (Exception e) {
            if (mListener != null) {
                mListener.onCreateError();
            }
            FileUtils.deleteFileOnExist(finalOutput);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}