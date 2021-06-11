package com.pdfconverter.jpg2pdf.pdf.converter.utils.text;

import android.content.Context;
import android.net.Uri;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextFileReader {

    Context mContext;

    public TextFileReader(Context context) {
        mContext = context;
    }

    void read(Uri uri, Document outputDocument, String extension, Font customFont) {
        try {
            InputStream inputStream;
            inputStream = mContext.getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return;
            createDocumentFromStream(uri, outputDocument, extension, customFont, inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDocumentFromStream(Uri uri, Document document, String extension, Font customFont, InputStream inputStream) throws Exception {
        if (extension.equals(DataConstants.DOC_EXTENSION)) {
            HWPFDocument doc = new HWPFDocument(inputStream);
            WordExtractor extractor = new WordExtractor(doc);
            String fileData = extractor.getText();

            Paragraph documentParagraph = new Paragraph(fileData + "\n", customFont);
            documentParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(documentParagraph);
        } else if (extension.equals(DataConstants.DOCX_EXTENSION)) {
            XWPFDocument doc = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String fileData = extractor.getText();

            Paragraph documentParagraph = new Paragraph(fileData + "\n", customFont);
            documentParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(documentParagraph);
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                Paragraph para = new Paragraph(line + "\n", customFont);
                para.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(para);
            }
            reader.close();
        }
    }
}
