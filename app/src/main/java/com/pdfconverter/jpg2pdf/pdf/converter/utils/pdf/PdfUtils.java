package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf;

import android.content.Context;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.OptionConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.Watermark;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.DirectoryUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PdfUtils {
    private static final int DEFAULT_DISTANCE = 50;

    public static String setPasswordPdfFile(Context context, String path, String password) throws IOException, DocumentException {

        String appPassword = AppConstants.APP_PASSWORD;
        String fileName = FileUtils.getFileName(path);
        String newPath = DirectoryUtils.getDefaultStorageLocation() + "/" + fileName;

        String finalOutputFile = FileUtils.getUniquePdfFileName(context, FileUtils.getLastReplacePath(newPath, DataConstants.PDF_EXTENSION,
                context.getString(R.string.protect_pdf_encrypted_file_name)));

        PdfReader reader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
        stamper.setEncryption(password.getBytes(), appPassword.getBytes(),
                PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128);
        stamper.close();
        reader.close();
        return finalOutputFile;
    }

    public static String removePassword(Context context, String path, String inputPassword) {
        String finalOutputFile;
        String fileName = FileUtils.getFileName(path);
        String newPath = DirectoryUtils.getDefaultStorageLocation() + "/" + fileName;

        finalOutputFile = FileUtils.getUniquePdfFileName(context, FileUtils.getLastReplacePath(newPath, DataConstants.PDF_EXTENSION,
                context.getString(R.string.unlock_pdf_encrypted_file_name)));

        if (!removePasswordUsingDefMasterPassword(path, finalOutputFile, inputPassword)) {
            if (!removePasswordUsingInputMasterPassword(path, finalOutputFile, inputPassword)) {
                return null;
            }
        }

        return finalOutputFile;
    }

    public static String addWatermark(Context context, String path, Watermark watermark) throws IOException, DocumentException {
        String finalOutputFile = FileUtils.getUniquePdfFileName(context, FileUtils.getLastReplacePath(path, DataConstants.PDF_EXTENSION,
                context.getString(R.string.add_watermark_output_file_name)));

        PdfReader reader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));

        BaseFont baseFont = BaseFont.createFont(watermark.getFontFamily(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont, watermark.getTextSize(), watermark.getFontStyle(), watermark.getTextColor());
        Phrase p = new Phrase(watermark.getWatermarkText(), font);

        PdfContentByte over;
        Rectangle pageSize;
        float x, y;
        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
            pageSize = reader.getPageSizeWithRotation(i);
            x = getXForWaterMark(watermark.getPosition(), pageSize);
            y = getYForWaterMark(watermark.getPosition(), pageSize);
            over = stamper.getOverContent(i);

            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, x, y, watermark.getRotationAngle());
        }

        stamper.close();
        reader.close();
        return finalOutputFile;
    }

    public static float getXForWaterMark(int position, Rectangle pageSize) {
        switch (position) {
            case OptionConstants.POSITION_CENTER:
            case OptionConstants.POSITION_TOP_CENTER:
            case OptionConstants.POSITION_BOTTOM_CENTER:
                return (pageSize.getLeft() + pageSize.getRight()) / 2;
            case OptionConstants.POSITION_TOP_LEFT:
            case OptionConstants.POSITION_BOTTOM_LEFT:
                return pageSize.getLeft() + DEFAULT_DISTANCE;
            case OptionConstants.POSITION_TOP_RIGHT:
            case OptionConstants.POSITION_BOTTOM_RIGHT:
                return pageSize.getRight() - DEFAULT_DISTANCE;
        }
        return pageSize.getLeft() + DEFAULT_DISTANCE;
    }

    public static float getYForWaterMark(int position, Rectangle pageSize) {
        switch (position) {
            case OptionConstants.POSITION_CENTER:
                return (pageSize.getTop() + pageSize.getBottom()) / 2;
            case OptionConstants.POSITION_TOP_LEFT:
            case OptionConstants.POSITION_TOP_RIGHT:
            case OptionConstants.POSITION_TOP_CENTER:
                return pageSize.getTop() - DEFAULT_DISTANCE;
            case OptionConstants.POSITION_BOTTOM_LEFT:
            case OptionConstants.POSITION_BOTTOM_RIGHT:
            case OptionConstants.POSITION_BOTTOM_CENTER:
                return pageSize.getBottom() + DEFAULT_DISTANCE;
        }
        return pageSize.getTop() + DEFAULT_DISTANCE;
    }

    private static boolean removePasswordUsingDefMasterPassword(final String path,
                                                                final String finalOutputFile,
                                                                final String inputPassword) {
        try {
            String masterPwd = AppConstants.APP_PASSWORD;
            PdfReader reader = new PdfReader(path, masterPwd.getBytes());
            byte[] password;

            password = reader.computeUserPassword();
            byte[] input = inputPassword.getBytes();
            if (Arrays.equals(input, password)) {
                PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
                stamper.close();
                reader.close();

                return true;
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean removePasswordUsingInputMasterPassword(final String file,
                                                                  final String finalOutputFile,
                                                                  final String inputPassword) {
        try {
            PdfReader reader = new PdfReader(file, inputPassword.getBytes());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(finalOutputFile));
            stamper.close();
            reader.close();

            return true;

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isPDFEncrypted(String path) {
        boolean isEncrypted;
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(path);
            isEncrypted = pdfReader.isEncrypted();
        } catch (BadPasswordException e) {
            isEncrypted = true;
        } catch (IOException | OutOfMemoryError e) {
            isEncrypted = false;
        } catch (Exception e) {
            isEncrypted = false;
        } finally {
            if (pdfReader != null) pdfReader.close();
        }
        return isEncrypted;
    }

    public static boolean isPasswordValid(String filePath, byte[] password) {
        try {
            PdfReader pdfReader = new PdfReader(filePath, password);
            return true;
        } catch (BadPasswordException e) {
            return  false;
        } catch (IOException e) {
            return true;
        }
    }
}
