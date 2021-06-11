package com.pdfconverter.jpg2pdf.pdf.converter.utils.split;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitFileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SplitPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.FileOutputStream;

@SuppressLint("StaticFieldLeak")
public class SplitPdfManager extends AsyncTask<Object, Object, Object> {
    private final Context mContext;
    private final SplitPdfListener mListener;
    private final SplitPDFOptions mOptions;

    private int mNumberSuccess = 0;
    private int mNumberError = 0;

    public SplitPdfManager(Context context, SplitPdfListener listener, SplitPDFOptions options) {
        mContext = context;
        mListener = listener;
        mOptions = options;

        mNumberSuccess = mNumberError = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null)
            mListener.onCreateStart();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mListener != null)
            mListener.onCreateFinish(mNumberSuccess, mNumberError);
    }

    @Override
    protected Object doInBackground(Object... objects) {

        try {
            PdfReader reader = new PdfReader(mOptions.getInputFilePath());
            PdfCopy copy;
            Document document;

            for (SplitFileData splitFileData : mOptions.getOutputList()) {
                if (isCancelled()) break;

                String finalOutput = DirectoryUtils.getSplitStorageLocation(mOptions.getInputFileData().getDisplayName()) + splitFileData.getFileName() + ".pdf";

                try {
                    document = new Document();
                    copy = new PdfCopy(document, new FileOutputStream(finalOutput));

                    document.open();
                    for (int i = 0; i < splitFileData.getPageList().size(); i++) {
                        copy.addPage(copy.getImportedPage(reader, splitFileData.getPageList().get(i)));
                    }
                    document.close();

                    mNumberSuccess++;
                    if (mListener != null)
                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, new SplitFileData(splitFileData.getFileName() + ".pdf", finalOutput, splitFileData.getPageList(), true));
                } catch (Exception e) {
                    mNumberError++;
                    if (mListener != null)
                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, null);
                    FileUtils.deleteFileOnExist(finalOutput);
                }
            }

            if (!isCancelled() && mListener != null)
                mListener.onCreateFinish(mNumberSuccess, mNumberError);
        } catch (Exception e) {
            if (mListener != null)
                mListener.onCreateFinish(mNumberSuccess, mNumberError);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}