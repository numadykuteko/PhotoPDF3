package com.pdfconverter.jpg2pdf.pdf.converter.utils.editpdf;

import java.util.ArrayList;

public interface FindDuplicateListener {
    void onFindSuccess(ArrayList<String> listDuplicate);
    void onFindStart();
}
