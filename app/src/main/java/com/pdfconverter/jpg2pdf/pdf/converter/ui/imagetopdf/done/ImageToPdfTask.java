package com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.done;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.ExifInterface;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageToPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.WatermarkPageEvent;

import java.io.File;
import java.io.FileOutputStream;

public class ImageToPdfTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "CreatePdfTask";
    private ImageToPDFOptions mImageToPDFOptions;
    private OnPDFCreatedInterface mOnPDFCreatedInterface;
    private String mName;

    private File mFilePdf;

    public ImageToPdfTask(ImageToPDFOptions imageToPDFOptions, File file,
                          OnPDFCreatedInterface onPDFCreatedInterface) {
        mImageToPDFOptions = imageToPDFOptions;
        mFilePdf = file;
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


    @Override
    protected String doInBackground(String... strings) {

        try {
            if (mImageToPDFOptions.getPageSize() == null) {
                mImageToPDFOptions.setPageSize(ImageToPdfConstants.DEFAULT_PAGE_SIZE);
            }
            Rectangle pageSize = new Rectangle(PageSize.getRectangle(mImageToPDFOptions.getPageSize()));
            pageSize.setBackgroundColor(getBaseColor(ImageToPdfConstants.DEFAULT_PAGE_COLOR));
            Document document = new Document(pageSize,
                    mImageToPDFOptions.getMarginLeft(), mImageToPDFOptions.getMarginRight(),
                    mImageToPDFOptions.getMarginTop(), mImageToPDFOptions.getMarginBottom());
            document.setMargins(mImageToPDFOptions.getMarginLeft(), mImageToPDFOptions.getMarginRight(),
                    mImageToPDFOptions.getMarginTop(), mImageToPDFOptions.getMarginBottom());
            Rectangle documentRect = document.getPageSize();
            publishProgress(8);

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mFilePdf.getAbsolutePath()));
            if (mImageToPDFOptions.isPasswordProtected()) {
                writer.setEncryption(mImageToPDFOptions.getMasterPwd().getBytes(),
                        AppConstants.APP_PASSWORD.getBytes(),
                        PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128);
            }
            if (mImageToPDFOptions.isWatermarkAdded()) {
                WatermarkPageEvent watermarkPageEvent = new WatermarkPageEvent();
                watermarkPageEvent.setWatermark(mImageToPDFOptions.getWatermark());
                writer.setPageEvent(watermarkPageEvent);
            }
            publishProgress(10);
            document.open();

            int size = mImageToPDFOptions.getImagesUri().size();
            int increasePer = 90 / size;
            int percent = 10;
            for (int i = 0; i < size; i++) {

                try {
                    int quality = ImageToPdfConstants.DEFAULT_QUALITY;
                    Image image = Image.getInstance(mImageToPDFOptions.getImagesUri().get(i));
                    ExifInterface exif = new ExifInterface(mImageToPDFOptions.getImagesUri().get(i));
                    int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    if (rotation == ExifInterface.ORIENTATION_ROTATE_90) {
                        image.setRotationDegrees(-90);
                    }
                    double qualityMod = quality * 0.09;
                    image.setCompressionLevel((int) qualityMod);
                    image.setBorder(Rectangle.BOX);
                    image.setBorderWidth(mImageToPDFOptions.getBorderWidth());

                    float pageWidth = document.getPageSize().getWidth() - (mImageToPDFOptions.getMarginLeft() +
                            mImageToPDFOptions.getMarginRight());
                    float pageHeight = document.getPageSize().getHeight() - (mImageToPDFOptions.getMarginBottom() +
                            mImageToPDFOptions.getMarginTop());
                    if (mImageToPDFOptions.getImageScaleType() == null ||
                            mImageToPDFOptions.getImageScaleType().equals(ImageToPdfConstants.IMAGE_SCALE_TYPE_ASPECT_RATIO)) {
                        if (mImageToPDFOptions.getPageNumStyle() != null && mImageToPDFOptions.getPageNumStyle().length() > 0) {
                            image.scaleToFit(pageWidth, pageHeight - 50);
                        } else {
                            image.scaleToFit(pageWidth, pageHeight);
                        }
                    } else {
                        if (mImageToPDFOptions.getPageNumStyle() != null && mImageToPDFOptions.getPageNumStyle().length() > 0) {
                            image.scaleAbsolute(pageWidth, pageHeight - 50);
                        } else {
                            image.scaleAbsolute(pageWidth, pageHeight);
                        }
                    }
                    image.setAbsolutePosition(
                            (documentRect.getWidth() - image.getScaledWidth()) / 2,
                            (documentRect.getHeight() - image.getScaledHeight()) / 2);
                    addPageNumber(documentRect, writer);
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
            publishProgress(100);
        } catch (Exception e) {
            mOnPDFCreatedInterface.createPdfFalse();
            cancel(true);
        }
        return null;
    }

    private void addPageNumber(Rectangle documentRect, PdfWriter writer) {
        if (mImageToPDFOptions.getPageNumStyle() != null && mImageToPDFOptions.getPageNumStyle().length() > 0) {
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    getPhrase(writer, mImageToPDFOptions.getPageNumStyle(), mImageToPDFOptions.getImagesUri().size()),
                    ((documentRect.getRight() + documentRect.getLeft()) / 2),
                    documentRect.getBottom() + 5, 0);
        }
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    private Phrase getPhrase(PdfWriter writer, String pageNumStyle, int size) {
        Phrase phrase;
        Chunk chunk;
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.BOLD);

        switch (pageNumStyle) {
            case ImageToPdfConstants.PG_NUM_STYLE_PAGE_X_OF_N:
                chunk = new Chunk(String.format("Page %d of %d", writer.getPageNumber(), size), bold);
                break;
            case ImageToPdfConstants.PG_NUM_STYLE_X_OF_N:
                chunk = new Chunk(String.format("%d of %d", writer.getPageNumber(), size), bold);
                break;
            default:
                chunk = new Chunk(String.format("%d", writer.getPageNumber()), bold);
                break;
        }
        phrase = new Phrase(chunk);
        return phrase;
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

        void createPdfFalse();
    }
}
