package com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.FileOutputStream;
import java.util.ArrayList;

@SuppressLint("StaticFieldLeak")
public class EditPdfManager extends AsyncTask<Object, Object, Object> {

    private final String mPath;
    private final EditPdfListener mListener;
    private final Context mContext;
    private final boolean mIsSaveAs;
    private final String mOutputName;
    private final ArrayList<PageData> mListPage;

    public EditPdfManager(String path, Context context, boolean isSaveAsNewFile, String outputName, ArrayList<PageData> listPage, EditPdfListener listener) {
        this.mPath = path;
        this.mListener = listener;
        this.mContext = context;
        this.mIsSaveAs = isSaveAsNewFile;
        this.mListPage = listPage;
        this.mOutputName = outputName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onEditStart();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        String outputPath = mPath;

        if (mIsSaveAs) {
            outputPath = FileUtils.getUniquePdfFileName(mContext, mOutputName);
        }

        try {
            StringBuilder pageBuilder = new StringBuilder();
            for (PageData pageData : mListPage) {
                pageBuilder.append(pageData.getSourcePage()).append(",");
            }

            PdfReader reader = new PdfReader(mPath);
            reader.selectPages(pageBuilder.toString());
            if (reader.getNumberOfPages() == 0) {
                mListener.onEditFail();
                return false;
            }

            PdfStamper pdfStamper = new PdfStamper(reader, new FileOutputStream(outputPath));
            pdfStamper.close();

            mListener.onEditSuccess(outputPath);

        } catch (Exception e) {
            mListener.onEditFail();
            if (mIsSaveAs) {
                FileUtils.deleteFileOnExist(outputPath);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
