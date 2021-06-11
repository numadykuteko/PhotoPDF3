package com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.PageData;

import java.io.File;
import java.util.ArrayList;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

@SuppressLint("StaticFieldLeak")
public class PdfToBitmapManager extends AsyncTask<String, String, ArrayList<PageData>> {

    private final Uri mUri;
    private final String mPath;
    private final PdfToBitmapListener mListener;
    private final Context mContext;


    public PdfToBitmapManager(Uri uri, String path, Context context, PdfToBitmapListener listener) {
        this.mUri = uri;
        this.mPath = path;
        this.mListener = listener;
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onReadStart();
    }

    @Override
    protected ArrayList<PageData> doInBackground(String... strings) {
        ArrayList<PageData> pageDataList = new ArrayList<>();
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (mUri != null)
                fileDescriptor = mContext.getContentResolver().openFileDescriptor(mUri, "r");
            else if (mPath != null)
                fileDescriptor = ParcelFileDescriptor.open(new File(mPath), MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                pageDataList = getPageData(renderer);
                renderer.close();
            }
        } catch (Exception e) {
            // donothing
        }
        return pageDataList;
    }

    private ArrayList<PageData> getPageData(PdfRenderer renderer) {
        ArrayList<PageData> pageDataList = new ArrayList<>();
        final int pageCount = renderer.getPageCount();
        for (int i = 0; i < pageCount; i++) {
            PdfRenderer.Page page = renderer.openPage(i);

            Bitmap bitmap;
            int width, height;
            if (page.getHeight() > 300) {
                height = 150;
            } else {
                height = page.getHeight() / 2;
            }

            if (page.getWidth() > 350) {
                width = 175;
            } else {
                width = page.getWidth() / 2;
            }
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            pageDataList.add(new PageData(i + 1, bitmap));
            page.close();
        }
        return pageDataList;
    }

    @Override
    protected void onPostExecute(ArrayList<PageData> pageDataList) {
        super.onPostExecute(pageDataList);
        if (pageDataList != null && !pageDataList.isEmpty()) {
            mListener.onReadSuccess(pageDataList);
        } else {
            mListener.onReadFail();
        }
    }
}
