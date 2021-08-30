package com.pdfconverter.jpg2pdf.pdf.converter.utils.file;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.PdfUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DirectoryUtils {
    private Context mContext;
    private ArrayList<String> mFilePaths;

    public DirectoryUtils(Context context) {
        mContext = context;
        mFilePaths = new ArrayList<>();
    }

    private static boolean isPDFAndNotDirectory(File file) {
        return !file.isDirectory() && file.getName().toLowerCase().endsWith(".pdf");
    }

    ArrayList<String> getAllPDFsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Collections.singletonList(DataConstants.PDF_EXTENSION));
        return mFilePaths;
    }

    ArrayList<String> getAllLockedPDFsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDirFindLockedPdf(Environment.getExternalStorageDirectory());
        return mFilePaths;
    }

    ArrayList<String> getAllUnlockedPDFsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDirFindUnlockedPdf(Environment.getExternalStorageDirectory());
        return mFilePaths;
    }

    ArrayList<String> getAllExcelsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.EXCEL_EXTENSION, DataConstants.EXCEL_WORKBOOK_EXTENSION));
        return mFilePaths;
    }

    ArrayList<String> getAllWordsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.DOC_EXTENSION, DataConstants.DOCX_EXTENSION));
        return mFilePaths;
    }

    ArrayList<String> getAllTextsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.DOC_EXTENSION, DataConstants.DOCX_EXTENSION, DataConstants.TEXT_EXTENSION));
        return mFilePaths;
    }

    ArrayList<String> getAllTxtsOnDevice() {
        mFilePaths = new ArrayList<>();
        walkDir(Environment.getExternalStorageDirectory(), Arrays.asList(DataConstants.TEXT_EXTENSION));
        return mFilePaths;
    }

    private void walkDir(File dir, List<String> extensions) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDir(aListFile, extensions);
                } else {
                    for (String extension: extensions) {
                        if (aListFile.getName().toLowerCase().endsWith(extension)) {
                            //Do what ever u want
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private void walkDirFindLockedPdf(File dir) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDirFindLockedPdf(aListFile);
                } else {
                    if (aListFile.getName().toLowerCase().endsWith(".pdf")) {
                        if (PdfUtils.isPDFEncrypted(aListFile.getAbsolutePath())) {
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private void walkDirFindUnlockedPdf(File dir) {
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    walkDirFindUnlockedPdf(aListFile);
                } else {
                    if (aListFile.getName().toLowerCase().endsWith(".pdf")) {
                        if (!PdfUtils.isPDFEncrypted(aListFile.getAbsolutePath())) {
                            mFilePaths.add(aListFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public static File getDefaultStorageFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            try {
                boolean isDirectoryCreated = dir.mkdir();
                if (!isDirectoryCreated) {
                    return Environment.getExternalStorageDirectory();
                }
            } catch (Exception e) {
                return Environment.getExternalStorageDirectory();
            }
        }
        return dir;
    }

    public static String getDefaultStorageLocation() {
        return getDefaultStorageFile().getAbsolutePath() + "/";
    }

    public static String getSplitStorageLocation(String rootFileName) {
        File dir = getDefaultStorageFile();
        return dir.getAbsolutePath() + "/";
    }

    public static String getImageStorageLocation(Context context, String folderName) {
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DataConstants.IMAGE_DIRECTORY);
        if (!dir.exists()) {
            try {
                boolean isDirectoryCreated = dir.mkdir();
                if (!isDirectoryCreated) {
                    Log.e("Error", "Directory could not be created");
                }
            } catch (Exception e) {

            }
        }
        return dir.getAbsolutePath() + "/";
    }

    public static void cleanImageStorage(Context context) {
        try {
            File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DataConstants.IMAGE_DIRECTORY);
            if (folder.isDirectory())
            {
                String[] children = folder.list();
                for (String child : children) {
                    new File(folder, child).delete();
                }
            }
        } catch (Exception ignored) {

        }
    }
}
