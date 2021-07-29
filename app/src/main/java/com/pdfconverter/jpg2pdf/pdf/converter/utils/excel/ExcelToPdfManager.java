package com.pdfconverter.jpg2pdf.pdf.converter.utils.excel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.aspose.cells.LoadOptions;
import com.aspose.cells.PageOrientationType;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.DocumentData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ExcelToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("StaticFieldLeak")
public class ExcelToPdfManager extends AsyncTask<Object, Object, Object> {
    private final Context mContext;
    private final ExcelToPdfListener mListener;
    private final ExcelToPDFOptions mOptions;
    private String mTempPath;
    private int mNumberSuccess = 0;

    public ExcelToPdfManager(Context context, ExcelToPdfListener listener, ExcelToPDFOptions options) {
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
        mNumberSuccess = 0;
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

            for (int i = 0; i < mOptions.getPathList().size(); i++) {
                try {
                    DocumentData documentData = mOptions.getPathList().get(i);

                    if (isCancelled()) break;

                    String path = documentData.getFilePath();
                    LoadOptions loadOptions = new LoadOptions();
                    loadOptions.setPaperSize(mOptions.getPageSize());

                    final Workbook workbook = new Workbook(path, loadOptions);

                    for (int j = 0; j < workbook.getWorksheets().getCount(); j++) {
                        Worksheet worksheet = workbook.getWorksheets().get(j);
                        worksheet.getPageSetup().setPaperSize(mOptions.getPageSize());
                        worksheet.getPageSetup().setOrientation(mOptions.getPageOrientation().toLowerCase().equals("portrait") ? PageOrientationType.PORTRAIT : PageOrientationType.LANDSCAPE);

                        worksheet.getPageSetup().setFitToPagesWide(1);
                        worksheet.getPageSetup().setFitToPagesTall(1);
                        worksheet.autoFitColumns();
                        worksheet.autoFitRows();
                    }

                    mTempPath = FileUtils.getUniquePdfFileName(mContext, FileUtils.getLastReplacePath(finalOutput,
                            DataConstants.PDF_EXTENSION, "_temp_" + System.currentTimeMillis() + ".pdf"));

                    PdfSaveOptions options = new PdfSaveOptions(SaveFormat.PDF);
                    options.setAllColumnsInOnePagePerSheet(true);
                    options.setOptimizationType(1);
                    options.setOnePagePerSheet(mOptions.isOneSheetOnePage());
                    options.setDefaultFont("arial");
                    options.setCheckWorkbookDefaultFont(false);

                    workbook.save(mTempPath, options);

                    PdfReader pdfreader;
                    pdfreader = new PdfReader(mTempPath);
                    // Get the number of pages of the pdf file
                    int numOfPages = pdfreader.getNumberOfPages();
                    for (int page = 1; page <= numOfPages; page++)
                        copy.addPage(copy.getImportedPage(pdfreader, page));

                    FileUtils.deleteFileOnExist(mTempPath);

                    if (mListener != null) {
                        mListener.onUpdateProcess(Math.round((i + 1) / (float) mOptions.getPathList().size() * 100));
                    }
                    mNumberSuccess++;
                } catch (Exception e) {
                    FileUtils.deleteFileOnExist(mTempPath);

                    if (mListener != null) {
                        mListener.onUpdateProcess(Math.round((i + 1) / (float) mOptions.getPathList().size() * 100));
                    }
                }
            }

            document.close();

            if (!isCancelled()) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            if (mNumberSuccess > 0) {
                                mListener.onCreateSuccess(finalOutput);
                            } else {
                                mListener.onCreateError();
                                FileUtils.deleteFileOnExist(mTempPath);
                                FileUtils.deleteFileOnExist(finalOutput);
                            }
                        }
                    }
                }, 800);
            }

        } catch (Exception e) {
            if (mListener != null) {
                mListener.onCreateError();
            }
            FileUtils.deleteFileOnExist(mTempPath);
            FileUtils.deleteFileOnExist(finalOutput);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}