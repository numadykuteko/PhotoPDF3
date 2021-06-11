package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdftoimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.ImageExtractData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PDFToImageOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.image.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

@SuppressLint("StaticFieldLeak")
public class PdfToImageManager extends AsyncTask<Object, Object, Object> {

    public static final int TYPE_EXACT = 1;
    public static final int TYPE_FIND = 2;

    private final Context mContext;
    private final PdfToImageListener mListener;
    private final PDFToImageOptions mOptions;

    private int mNumberSuccess = 0;
    private int mNumberError = 0;

    private String mTempPath;

    public PdfToImageManager(Context context, PdfToImageListener listener, PDFToImageOptions options) {
        mContext = context;
        mListener = listener;
        mOptions = options;

        mNumberSuccess = mNumberError = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null)
            mListener.onCreateStart(mOptions.getType());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mListener != null)
            mListener.onCreateFinish(mNumberSuccess, mNumberError, mOptions.getType());
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            if (mOptions.getType() == TYPE_FIND) {
                // create sub file from start to end
                mTempPath = FileUtils.getUniquePdfFileName(mContext, FileUtils.getLastReplacePath(mOptions.getInputFilePath(),
                        DataConstants.PDF_EXTENSION, "_temp_" + System.currentTimeMillis() + ".pdf"));

                PdfReader readerInput = new PdfReader(mOptions.getInputFilePath());
                PdfCopy copy;
                Document document;

                document = new Document();
                copy = new PdfCopy(document, new FileOutputStream(mTempPath));

                document.open();
                for (int page = mOptions.getStartPage(); page <= mOptions.getEndPage(); page++) {
                    copy.addPage(copy.getImportedPage(readerInput, page));
                }
                copy.close();
                document.close();

                PdfReader reader = new PdfReader(mTempPath);
                PdfObject obj;

                String directoryPath = DirectoryUtils.getImageStorageLocation(mContext,"image_find");

                int count = 1;
                for (int i = 1; i <= reader.getXrefSize(); i++) {
                    if (isCancelled()) break;

                    obj = reader.getPdfObject(i);

                    if (obj != null && obj.isStream()) {
                        PRStream stream = (PRStream) obj;
                        PdfObject type = stream.get(PdfName.SUBTYPE);

                        if (type != null && type.toString().equals(PdfName.IMAGE.toString())) {
                            try {
                                PdfImageObject pio = new PdfImageObject(stream);
                                byte[] image = pio.getImageAsBytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

                                String filename = FileUtils.generateImageExtractFileName(mOptions.getInputFileData().getDisplayName(), count);
                                String filePath = ImageUtils.saveImage(directoryPath, filename, bitmap);
                                if (filePath != null) {
                                    mNumberSuccess ++;
                                    if (mListener != null)
                                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, new ImageExtractData(filename, filePath, count), mOptions.getType());

                                    count ++;
                                } else {
                                    mNumberError ++;
                                    if (mListener != null)
                                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, null, mOptions.getType());
                                }
                            } catch (Exception e) {
                                mNumberError ++;
                                if (mListener != null)
                                    mListener.onUpdateProcess(mNumberSuccess, mNumberError, null, mOptions.getType());
                            }

                        }
                    }
                }

                FileUtils.deleteFileOnExist(mTempPath);
            } else {
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(new File(mOptions.getInputFilePath()), MODE_READ_ONLY);
                if (fileDescriptor == null) {
                    fileDescriptor =  mContext.getContentResolver().openFileDescriptor(mOptions.getInputFileData().getFileUri(), "r");
                }

                if (fileDescriptor != null) {
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    final int pageCount = renderer.getPageCount();

                    if (mOptions.getStartPage() > pageCount) {
                        ToastUtils.showMessageShort(mContext, mContext.getString(R.string.pdf_to_image_invalid_start_page));
                    } else {
                        String directoryPath = DirectoryUtils.getImageStorageLocation(mContext, "image_extract");

                        for (int i = mOptions.getStartPage() - 1; i < pageCount && i < mOptions.getEndPage(); i++) {
                            if (isCancelled()) break;

                            try {
                                PdfRenderer.Page page = renderer.openPage(i);
                                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                canvas.drawColor(Color.WHITE);
                                canvas.drawBitmap(bitmap, 0, 0, null);
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                                page.close();

                                String filename = FileUtils.generateImageExtractFileName(mOptions.getInputFileData().getDisplayName(), i + 1);
                                String filePath = ImageUtils.saveImage(directoryPath, filename, bitmap);
                                if (filePath != null) {
                                    mNumberSuccess ++;
                                    if (mListener != null)
                                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, new ImageExtractData(filename, filePath, i + 1), mOptions.getType());
                                } else {
                                    mNumberError ++;
                                    if (mListener != null)
                                        mListener.onUpdateProcess(mNumberSuccess, mNumberError, null, mOptions.getType());
                                }
                            } catch (Exception e) {
                                mNumberError ++;
                                if (mListener != null)
                                    mListener.onUpdateProcess(mNumberSuccess, mNumberError, null, mOptions.getType());
                            }
                        }
                    }
                }
            }

            if (!isCancelled()) {
                if (mOptions.getType() == TYPE_EXACT) {
                    if (mListener != null)
                        mListener.onCreateFinish(mNumberSuccess, mOptions.getEndPage() - mOptions.getStartPage() + 1 - mNumberSuccess, mOptions.getType());
                } else {
                    if (mListener != null)
                        mListener.onCreateFinish(mNumberSuccess, mNumberError, mOptions.getType());
                }
            }

        } catch (Exception e) {
            if (mOptions.getType() == TYPE_EXACT) {
                if (mListener != null)
                    mListener.onCreateFinish(mNumberSuccess, mOptions.getEndPage() - mOptions.getStartPage() + 1 - mNumberSuccess, mOptions.getType());
            } else {
                if (mListener != null)
                    mListener.onCreateFinish(mNumberSuccess, mNumberError, mOptions.getType());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}