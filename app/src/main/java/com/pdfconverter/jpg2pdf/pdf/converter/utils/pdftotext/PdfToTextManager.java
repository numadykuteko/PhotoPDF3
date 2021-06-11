package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftotext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PDFToTextOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextExtractData;

@SuppressLint("StaticFieldLeak")
public class PdfToTextManager extends AsyncTask<Object, Object, Object> {

    private final Context mContext;
    private final PdfToTextListener mListener;
    private final PDFToTextOptions mOptions;

    private int mNumberSuccess = 0;
    private int mNumberError = 0;

    public PdfToTextManager(Context context, PdfToTextListener listener, PDFToTextOptions options) {
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
            int pageSize = reader.getNumberOfPages();
            for (int i = mOptions.getStartPage(); i <= pageSize && i <= mOptions.getEndPage(); i++) {
                if (isCancelled()) break;

                try {
                    TextExtractionStrategy locationTextExtractionStrategy = new LocationTextExtractionStrategy();
                    PdfTextExtractor.getTextFromPage(reader, i, locationTextExtractionStrategy);

                    String lineString = locationTextExtractionStrategy.getResultantText().trim();
                    mNumberSuccess ++;
                    if (mListener != null)
                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, new TextExtractData(lineString, i));
                } catch (Exception e) {
                    mNumberError++;
                    if (mListener != null)
                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, null);
                }
            }
            reader.close();

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