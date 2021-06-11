package com.pdfconverter.jpg2pdf.pdf.converter.ui.search;

import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;

import java.util.List;

public interface SearchFileListener {
    public void loadDone(List<FileData> result);
}
