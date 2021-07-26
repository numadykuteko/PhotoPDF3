package com.pdfconverter.jpg2pdf.pdf.converter.utils.file;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.SavedData;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DateTimeUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.image.ImageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants.AUTHORITY_APP;

public class FileUtils {
    public enum FileType {
        type_PDF,
        type_TXT,
        type_EXCEL,
        type_WORD,
        type_IMAGE,
        type_PPT
    }

    private static final List<String> orderList = Arrays.asList(MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.SIZE);
    public static final int SORT_BY_DATE_ASC = 0;
    public static final int SORT_BY_DATE_DESC = 1;
    public static final int SORT_BY_NAME_ASC = 2;
    public static final int SORT_BY_NAME_DESC = 3;
    public static final int SORT_BY_SIZE_ASC = 4;
    public static final int SORT_BY_SIZE_DESC = 5;
    public static final int SORT_BY_TYPE = 6;

    @SuppressLint("DefaultLocale")
    public static String getFormattedSize(int size) {
        if (size < 100) {
            return String.format("%.2f B", (double) size);
        } if (size / 1024 < 100) {
            return String.format("%.2f KB", (double) size / (1024));
        } else {
            return String.format("%.2f MB", (double) size / (1024 * 1024));
        }
    }

    public static String getDefaultFileName(String functionName) {
        return functionName + DateTimeUtils.currentTimeToNaming();
    }

    public static boolean copyImageToDownload(Context context, String filePath) {
        try {
            String newFileName = FileUtils.getFileName(filePath);
            File outputDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outputFile = new File(outputDirectory, newFileName);

            InputStream in = new FileInputStream(filePath);
            OutputStream out = new FileOutputStream(outputFile);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                ImageUtils.saveImageToGallery(context, new File(outputFile.getAbsolutePath()));

                return true;
            } catch (Exception e) {

            } finally {
                in.close();
                out.close();
            }
        } catch (Exception ignored) {

        }

        return false;
    }

    public static ArrayList<FileData> getExternalFileList(Context context, String fileType, int order) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");

        String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATE_MODIFIED, MediaStore.Files.FileColumns.SIZE};
        String selectionMimeType;
        String[] selectionArgsPdf;
        if (fileType.equals(DataConstants.FILE_TYPE_WORD)) {
            selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "= ? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "= ?";
            selectionArgsPdf = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc"), MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")};
        } else if (fileType.equals(DataConstants.FILE_TYPE_EXCEL)) {
            selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "= ? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "= ?";
            selectionArgsPdf = new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls"), MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")};
        } else {
            selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileType);
            selectionArgsPdf = new String[]{mimeType};
        }

        String orderBy;
        if (order == 1) {
            orderBy = orderList.get(order)+ " ASC";
        } else {
            orderBy = orderList.get(order)+ " DESC";
        }

        Cursor cursor = cr.query(uri, projection, selectionMimeType, selectionArgsPdf, orderBy);
        ArrayList<FileData> fileList = new ArrayList<>();
        if (cursor != null) {

            while (cursor.moveToNext()) {

                int columnIdIndex = cursor.getColumnIndex(projection[0]);
                int columnNameIndex = cursor.getColumnIndex(projection[1]);
                int columnDateIndex = cursor.getColumnIndex(projection[2]);
                int columnSizeIndex = cursor.getColumnIndex(projection[3]);

                long fileId = -1;
                try {
                    fileId = cursor.getLong(columnIdIndex);
                } catch (Exception e) {
                    continue;
                }

                Uri fileUri = Uri.parse(uri.toString() + "/" + fileId);

                String displayName = cursor.getString(columnNameIndex);
                if (displayName == null || displayName.length() == 0) {
                    displayName = "No name";
                }

                int dateAdded;
                try {
                    dateAdded = Integer.parseInt(cursor.getString(columnDateIndex));
                } catch (Exception e) {
                    dateAdded = -1;
                }

                int size = 0;
                try {
                    size = Integer.parseInt(cursor.getString(columnSizeIndex));
                } catch (Exception e) {
                    size = -1;
                }

                fileList.add(new FileData(displayName, null, fileUri, dateAdded, size, fileType));
            }
            cursor.close();
        }
        return fileList;
    }

    /**
     *     Uri selectedFileUri = data.getData();
     *     String selectedImagePath = FileUtils.getRealPathV3(selectedFileUri, context);
     */
    public static String getRealPathV3(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String[] split = DocumentsContract.getDocumentId(uri).split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                String uriId = DocumentsContract.getDocumentId(uri);
                if (uriId.startsWith("raw:/")) {
                    return uriId.replace("raw:/", "");
                } else if (uriId.startsWith("msf:")) {
                    return null;
                } else {
                    try {
                        long idLong = Long.parseLong(uriId);
                        final Uri downloadContentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), idLong);

                        return getDataColumn(context, downloadContentUri, null, null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return getIndexAsString(cursor, column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static String getIndexAsString(Cursor c, int i) {
        if (i == -1)
            return null;
        if (c.isNull(i)) {
            return null;
        }
        switch (c.getType(i)) {
            case Cursor.FIELD_TYPE_STRING:
                return c.getString(i);
            case Cursor.FIELD_TYPE_FLOAT: {
                return Double.toString(c.getDouble(i));
            }
            case Cursor.FIELD_TYPE_INTEGER: {
                return Long.toString(c.getLong(i));
            }
            default:
            case Cursor.FIELD_TYPE_NULL:
            case Cursor.FIELD_TYPE_BLOB:
                throw new IllegalStateException("data null");
        }
    }

    public static ArrayList<FileData> getAllExternalFileList(Context context, String fileType, int order) {
        DirectoryUtils directoryUtils = new DirectoryUtils(context);
        ArrayList<String> fileList = new ArrayList<>();

        switch (fileType) {
            case DataConstants.FILE_TYPE_WORD:
                fileList = directoryUtils.getAllWordsOnDevice();
                break;
            case DataConstants.FILE_TYPE_TEXT:
                fileList = directoryUtils.getAllTextsOnDevice();
                break;
            case DataConstants.FILE_TYPE_TXT:
                fileList = directoryUtils.getAllTxtsOnDevice();
                break;
            case DataConstants.FILE_TYPE_PDF:
                fileList = directoryUtils.getAllPDFsOnDevice();
                break;
            case DataConstants.FILE_TYPE_EXCEL:
                fileList = directoryUtils.getAllExcelsOnDevice();
                break;
        }

        ArrayList<FileData> resultList = new ArrayList<>();
        for (String filePath: fileList) {
            File file = new File(filePath);
            Uri uri = Uri.fromFile(file);
            int size = Integer.parseInt(String.valueOf(file.length()));

            FileData fileData = new FileData(getFileName(filePath), filePath, uri, (int) (file.lastModified() / 1000), size, fileType);
            resultList.add(fileData);
        }

        FileSortUtils.performSortOperation(order, resultList);

        return resultList;
    }

    public static ArrayList<FileData> getAllLockedFileList(Context context) {
        DirectoryUtils directoryUtils = new DirectoryUtils(context);
        ArrayList<String> fileList = new ArrayList<>();

        fileList = directoryUtils.getAllLockedPDFsOnDevice();

        ArrayList<FileData> resultList = new ArrayList<>();
        for (String filePath: fileList) {
            File file = new File(filePath);
            Uri uri = Uri.fromFile(file);
            int size = Integer.parseInt(String.valueOf(file.length()));

            FileData fileData = new FileData(getFileName(filePath), filePath, uri, (int) (file.lastModified() / 1000), size, DataConstants.FILE_TYPE_PDF);
            resultList.add(fileData);
        }

        FileSortUtils.performSortOperation(FileUtils.SORT_BY_DATE_DESC, resultList);

        return resultList;
    }

    public static ArrayList<FileData> getAllUnlockedFileList(Context context) {
        DirectoryUtils directoryUtils = new DirectoryUtils(context);
        ArrayList<String> fileList;

        fileList = directoryUtils.getAllUnlockedPDFsOnDevice();

        ArrayList<FileData> resultList = new ArrayList<>();
        for (String filePath: fileList) {
            File file = new File(filePath);
            Uri uri = Uri.fromFile(file);
            int size = Integer.parseInt(String.valueOf(file.length()));

            FileData fileData = new FileData(getFileName(filePath), filePath, uri, (int) (file.lastModified() / 1000), size, DataConstants.FILE_TYPE_PDF);
            resultList.add(fileData);
        }

        FileSortUtils.performSortOperation(FileUtils.SORT_BY_DATE_DESC, resultList);

        return resultList;
    }


    public static ArrayList<FileData> getFileListOfDirectory(FileData dirFileData) {
        String dirPath = dirFileData.getFilePath();

        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return new ArrayList<>();
        }

        File[] listFile = dir.listFiles();
        ArrayList<FileData> resultList = new ArrayList<>();

        if (listFile != null) {
            for (File file : listFile) {
                FileData fileData = new FileData();
                fileData.setFilePath(file.getAbsolutePath());
                fileData.setParentFile(dirFileData);
                fileData.setDateAdded((int) (file.lastModified() / 1000));
                fileData.setDisplayName(getFileName(file.getAbsolutePath()));

                if (file.isDirectory()) {
                    fileData.setFileType(DataConstants.FILE_TYPE_DIRECTORY);
                } else {
                    if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        continue;
                    }

                    fileData.setSize(Integer.parseInt(String.valueOf(file.length())));
                    fileData.setFileUri(Uri.fromFile(file));
                    fileData.setFileType(DataConstants.FILE_TYPE_PDF);
                }

                resultList.add(fileData);
            }
        }

        FileSortUtils.performSortOperation(SORT_BY_TYPE, resultList);

        return resultList;
    }

    public static void printFile(Context context, final File file) {
        final PrintDocumentAdapter mPrintDocumentAdapter = new PrintDocumentAdapterHelper(file);

        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        String jobName = context.getString(R.string.app_name) + " Print Document";

        try {
            if (printManager != null) {
                printManager.print(jobName, mPrintDocumentAdapter, null);
            } else {
                ToastUtils.showMessageShort(context, "Can not print file now.");
            }
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not print file now.");
        }
    }

    public static void shareFile(Context context, File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
            Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(uri);

            shareFileWithType(context, uris, context.getString(R.string.pdf_type));
        } else if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
            context.startActivity(Intent.createChooser(sharingIntent, "Share file with"));
        }
    }

    public static void shareMultipleFiles(Context context, List<File> files) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : files) {
            Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
            uris.add(uri);
        }
        shareFileWithType(context, uris, context.getString(R.string.pdf_type));
    }

    private static void shareFileWithType(Context context, ArrayList<Uri> uris, String type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_file_title));
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(type);

        try {
            context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_chooser)));
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not share file now.");
        }
    }

    public static void uploadFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Document")
                .setType("application/pdf")
                .setStream(uri)
                .getIntent();

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    public static void uploadTxtFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Document")
                .setType("application/txt")
                .setStream(uri)
                .getIntent()
                .setPackage("com.google.android.apps.docs");

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    public static void uploadImageFile(Activity context, File file) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);
        Intent uploadIntent = ShareCompat.IntentBuilder.from(context)
                .setText("Share Image")
                .setType("image/png")
                .setStream(uri)
                .getIntent()
                .setPackage("com.google.android.apps.docs");

        try {
            context.startActivity(uploadIntent);
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, "Can not upload file now.");
        }
    }

    public static void openFile(Context context, String path, FileType fileType) {
        if (path == null) {
            // TODO show error
            return;
        }
        String fileMimeType = "";

        if (fileType == FileType.type_PDF) {
            fileMimeType = context.getString(R.string.pdf_type);
        } else if (fileType == FileType.type_WORD) {
            fileMimeType = context.getString(R.string.word_type);
        } else if (fileType == FileType.type_TXT) {
            fileMimeType = context.getString(R.string.txt_type);
        } else if (fileType == FileType.type_EXCEL) {
            fileMimeType = context.getString(R.string.excel_type);
        }  else if (fileType == FileType.type_IMAGE) {
            fileMimeType = context.getString(R.string.image_type);
        }

        openFileInternal(context, path, fileMimeType);
    }

    public static void openFolder(Context context, String path) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(path);
            intent.setDataAndType(uri, "*/*");
            context.startActivity(Intent.createChooser(intent, "Open folder"));
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, context.getString(R.string.can_not_open_folder));
        }
    }

    private static void openFileInternal(Context context, String path, String dataType) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            Uri uri = FileProvider.getUriForFile(context, AUTHORITY_APP, file);

            target.setDataAndType(uri, dataType);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(target, context.getString(R.string.open_file)));
        } catch (Exception e) {
            ToastUtils.showMessageShort(context, context.getString(R.string.open_file_error));
        }
    }

    public static String getFileName(Context context, Uri uri) {
        String fileName = "File name";
        String scheme = uri.getScheme();

        if (scheme == null)
            return null;

        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }

        return fileName;
    }

    public static String getFileName(String path) {
        if (path == null)
            return "File name";

        int index = path.lastIndexOf("/");
        return index < path.length() ? path.substring(index + 1) : "File name";
    }

    public static String getDefaultOutputName(String fileType) {
        return fileType + "_" + DateTimeUtils.currentTimeToNaming();
    }

    public static int getNumberPages(String filePath) {
        ParcelFileDescriptor fileDescriptor = null;
        try {
            if (filePath != null)
                fileDescriptor = ParcelFileDescriptor.open(new File(filePath), MODE_READ_ONLY);
            if (fileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                return renderer.getPageCount();
            }
        } catch (Exception ignored) {}

        return 0;
    }

    public static String generateSplitFileName(String rootFileName, int index) {
        if (rootFileName.contains(".")) {
            rootFileName = rootFileName.substring(0, rootFileName.lastIndexOf("."));
        }

        if (rootFileName.length() > 8) {
            return rootFileName.substring(0, 7) + "_" + DateTimeUtils.currentTimeToNaming() + "_split_" + index;
        } else {
            return rootFileName + "_" + DateTimeUtils.currentTimeToNaming() + "_split_" + index;
        }
    }

    public static String generateImageExtractFileName(String rootFileName, int index) {
        return "Pdf to image_" + DateTimeUtils.currentTimeToNaming() + "_image_" + index;
    }

    public static String getFileNameWithoutExtension(String path) {
        if (path == null || path.lastIndexOf("/") == -1)
            return path;

        String filename = path.substring(path.lastIndexOf("/") + 1);
        filename = filename.replace(DataConstants.PDF_EXTENSION, "");

        return filename;
    }

    public static String getFileDirectoryPath(String path) {
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    public static String getMinimalDirectoryPath(String directoryPath, String firstIndicator) {
        if (directoryPath.contains(firstIndicator)) {
            return directoryPath.substring(directoryPath.indexOf(firstIndicator));
        }

        return directoryPath;
    }

    public static String getUniquePdfFileName(Context context, String fileName) {
        return getUniqueOtherFileName(context, fileName, DataConstants.PDF_EXTENSION);
    }

    public static String getUniqueOtherFileName(Context context, String fileName, String extension) {

        String outputFileName = fileName;
        File file = new File(outputFileName);

        if (!file.exists())
            return outputFileName;

        File parentFile = file.getParentFile();
        if (parentFile != null) {
            File[] listFiles = parentFile.listFiles();

            if (listFiles != null) {
                int append = checkRepeat(outputFileName, Arrays.asList(listFiles), extension);
                outputFileName = outputFileName.replace(extension, "(" + append + ")" + extension);
            }
        }

        return outputFileName;
    }

    public static String getLastReplacePath(String filePath, String source, String replacement) {
        int lastIndex = filePath.lastIndexOf(source);
        if (lastIndex == -1) return filePath;

        return filePath.substring(0, lastIndex) + replacement;
    }

    private static int checkRepeat(String finalOutputFile, final List<File> mFile, String extension) {
        boolean flag = true;
        int append = 0;
        while (flag) {
            append++;
            String name = finalOutputFile.replace(extension,
                    "(" + append + ")" + extension);
            flag = mFile.contains(new File(name));
        }

        return append;
    }

    public static void deleteFileOnExist(String path) {
        if (path == null) return;

        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean checkFileExist(String path) {
        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception ignored) {
        }

        return false;
    }

    public static boolean checkFileExistAndType(String path, FileType fileType) {
        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }

            path = path.toLowerCase();

            switch (fileType) {
                case type_PDF:
                    return path.endsWith(".pdf");
                case type_WORD:
                    return path.endsWith(".txt") || path.endsWith(".doc") || path.endsWith(".docx");
                case type_EXCEL:
                    return path.endsWith(".xls") || path.endsWith(".xlsx");
                case type_IMAGE:
                    return path.endsWith(".jpeg") || path.endsWith(".jpg") || path.endsWith(".png");
                case type_PPT:
                    return path.endsWith(".ppt") || path.endsWith(".pptx");

            }
        } catch (Exception ignored) {
        }

        return false;
    }

    public static int renameFile(SavedData fileData, String newName) {
        try {
            File currentFile = new File(fileData.getFilePath());

            if (!fileData.getFilePath().contains(fileData.getDisplayName())) {
                return -2;
            }

            String newDir = fileData.getFilePath().replace(fileData.getDisplayName(), newName);
            File newFile = new File(newDir);

            if (newFile.exists()) {
                return -1;
            }

            if (!currentFile.exists()) {
                return -2;
            }

            if (rename(currentFile, newFile)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return -2;
        }
    }

    public static int renameFile(FileData fileData, String newName) {
        try {
            File currentFile = new File(fileData.getFilePath());

            if (!fileData.getFilePath().contains(fileData.getDisplayName())) {
                return -2;
            }

            String newDir = fileData.getFilePath().replace(fileData.getDisplayName(), newName);
            File newFile = new File(newDir);

            if (newFile.exists()) {
                return -1;
            }

            if (!currentFile.exists()) {
                return -2;
            }

            if (rename(currentFile, newFile)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return -2;
        }
    }

    private static boolean rename(File from, File to) {
        try {
            return from.getParentFile() != null && from.getParentFile().exists() && from.exists() && from.renameTo(to);
        } catch (Exception e) {
            return false;
        }
    }
}
