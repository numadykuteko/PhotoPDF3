package com.pdfconverter.jpg2pdf.pdf.converter.utils.text;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.TextToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("StaticFieldLeak")
public class TextToPdfManager extends AsyncTask<Object, Object, Object>  {
    private final Context mContext;
    private final TextToPdfListener mListener;
    private final TextToPDFOptions mOptions;
    private final TextFileReader mTextFileReader;

    public TextToPdfManager(Context context, TextToPdfListener listener, TextToPDFOptions options) {
        mContext = context;
        mListener = listener;
        mOptions = options;
        mTextFileReader = new TextFileReader(mContext);
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
            if (mListener != null)
                mListener.onUpdateProcess(1);
            Rectangle pageSize = new Rectangle(PageSize.getRectangle(mOptions.getPageSize()));
            Document document = new Document(pageSize);

            if (mListener != null)
                mListener.onUpdateProcess(6);

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(finalOutput));
            writer.setPdfVersion(PdfWriter.VERSION_1_7);
            if (mOptions.isPasswordProtected()) {
                writer.setEncryption(mOptions.getPassword().getBytes(),
                        AppConstants.APP_PASSWORD.getBytes(),
                        PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128);
            }

            document.open();

            BaseFont baseFont = BaseFont.createFont(mOptions.getFontFamily(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font customFont = new Font(baseFont, mOptions.getFontSize());
            document.add(new Paragraph("\n"));

            List<DocumentData> listFileData = mOptions.getInputFileUri();

            if (mListener != null)
                mListener.onUpdateProcess(20);

            for (DocumentData fileData : listFileData) {
                if (isCancelled()) break;

                String fileExtension = DataConstants.TEXT_EXTENSION;
                String fileName = fileData.getDisplayName();

                if (fileName != null) {
                    fileName = fileName.toLowerCase();
                    if (fileName.toLowerCase().endsWith(DataConstants.TEXT_EXTENSION))
                        fileExtension = DataConstants.TEXT_EXTENSION;
                    else if (fileName.toLowerCase().endsWith(DataConstants.DOCX_EXTENSION))
                        fileExtension = DataConstants.DOCX_EXTENSION;
                    else if (fileName.toLowerCase().endsWith(DataConstants.DOC_EXTENSION))
                        fileExtension = DataConstants.DOC_EXTENSION;
                    else {
                        // TODO show error
                    }
                }

                mTextFileReader.read(fileData.getFileUri(), document, fileExtension, customFont);
                int currentIndex = listFileData.indexOf(fileData);

                if (currentIndex < listFileData.size() - 1) {
                    document.newPage();
                }

                int percentage = 20 + ((currentIndex + 1) / listFileData.size() * 80);

                if (mListener != null)
                    mListener.onUpdateProcess(percentage);
            }

            document.close();

            if (!isCancelled()) {
                Timer timer = new Timer();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mListener != null)
                            mListener.onCreateSuccess(finalOutput);
                    }
                }, 800);
            }
        } catch (Exception e) {
            if (mListener != null)
                mListener.onCreateError();
            FileUtils.deleteFileOnExist(finalOutput);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
