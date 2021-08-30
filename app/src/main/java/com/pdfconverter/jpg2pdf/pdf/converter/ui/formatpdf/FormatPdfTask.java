package com.pdfconverter.jpg2pdf.pdf.converter.ui.formatpdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.NewPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FormatPdfTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "FormatPdfTask";
    private NewPDFOptions mNewPDFOptions;
    private OnPDFCreatedInterface mOnPDFCreatedInterface;
    private File mFilePdf;
    private Context mContext;

    public FormatPdfTask(Context context, NewPDFOptions imageToPDFOptions, OnPDFCreatedInterface onPDFCreatedInterface) {
        mContext = context;
        mNewPDFOptions = imageToPDFOptions;
        mOnPDFCreatedInterface = onPDFCreatedInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mOnPDFCreatedInterface.updateStatus(values[0]);
    }

    private void setupFile() {
        File dir = DirectoryUtils.getDefaultStorageFile();
        String name = TextUtils.isEmpty(mNewPDFOptions.getFileName()) ? FileUtils.getDefaultOutputName(DataConstants.NEW_PDF_PREFIX_NAME)
                : mNewPDFOptions.getFileName();
        mFilePdf = new File(dir, name + ImageToPdfConstants.pdfExtension);
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            setupFile();

            if (mNewPDFOptions.getPageSize() == null) {
                mNewPDFOptions.setPageSize(ImageToPdfConstants.DEFAULT_PAGE_SIZE);
            }
            Rectangle pageSize = new Rectangle(PageSize.getRectangle(mNewPDFOptions.getPageSize()));
            pageSize.setBackgroundColor(getBaseColor(ImageToPdfConstants.DEFAULT_PAGE_COLOR));
            Document document = new Document(pageSize, 0, 0, 0, 0);
            document.setMargins(0, 0,0, 0);
            Rectangle documentRect = document.getPageSize();
            publishProgress(1);

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mFilePdf.getAbsolutePath()));
            document.open();

            int size = mNewPDFOptions.getNumberPage();
            int increasePer = 100 / size;
            int percent = 0;
            int[] imageList = {R.drawable.blank, R.drawable.lined, R.drawable.grid,
                    R.drawable.graph, R.drawable.music, R.drawable.dotted,
                    R.drawable.isomatric_dotted};

            int drawableCode = imageList[mNewPDFOptions.getSelectedType()];
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable drawable = mContext.getResources().getDrawable (drawableCode);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

            for (int i = 0; i < size; i++) {
                try {
                    int quality = ImageToPdfConstants.DEFAULT_QUALITY;
                    Image image = Image.getInstance(bitmapData);
                    double qualityMod = quality * 0.09;
                    image.setCompressionLevel((int) qualityMod);
                    image.setBorder(Rectangle.BOX);

                    image.setAbsolutePosition(
                            (documentRect.getWidth() - image.getScaledWidth()) / 2,
                            (documentRect.getHeight() - image.getScaledHeight()) / 2);
                    document.add(image);
                    document.newPage();
                } catch (Exception ignored) {
                    percent = percent + increasePer;
                    publishProgress(percent);

                    continue;
                }

                percent = percent + increasePer;
                publishProgress(percent);
            }
            document.close();
            mOnPDFCreatedInterface.createPdfSuccess(mFilePdf.getAbsolutePath());
        } catch (Exception e) {
            mOnPDFCreatedInterface.createPdfFalse();
            cancel(true);
        }
        return null;
    }

    private BaseColor getBaseColor(int color) {
        return new BaseColor(
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    public interface OnPDFCreatedInterface {
        void updateStatus(int percent);
        void createPdfSuccess(String outputPath);
        void createPdfFalse();
    }
}
