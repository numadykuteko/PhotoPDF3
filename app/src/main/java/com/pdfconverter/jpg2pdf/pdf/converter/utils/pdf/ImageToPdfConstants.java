package com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf;

import android.graphics.Color;

public class ImageToPdfConstants {
    public static final String pdfExtension = ".pdf";
    public static final String DEFAULT_PAGE_SIZE = "A4";
    public static final int DEFAULT_PAGE_COLOR = Color.WHITE;
    public static final String IMAGE_SCALE_TYPE_ASPECT_RATIO = "Aspect ratio";
    public static final String IMAGE_SCALE_TYPE_STRETCH = "Stretch image";

    public static final String PG_NUM_STYLE_PAGE_X_OF_N = "pg_num_style_page_x_of_n";
    public static final String PG_NUM_STYLE_X_OF_N = "pg_num_style_x_of_n";
    public static final String PG_NUM_STYLE_X = "pg_num_style_x";
    public static final String AUTHORITY_APP = "com.pdfconverter.jpg2pdf.pdf.converter.provider";

    public static final String[] PAGE_SIZE_TYPE = {"A4", "A0", "A1", "A2", "A3", "B0", "B1", "B2", "LETTER", "LEGAL", "TABLOID"};
    public static final String[] PAGE_NUMBER_TYPE = {"None", "Page X of N", "X of N", "X"};
    public static final String[] PAGE_NUMBER_TYPE_VALUE = {"", "pg_num_style_page_x_of_n", "pg_num_style_x_of_n", "pg_num_style_x"};

    public static final String[] SCALE_TYPE = {IMAGE_SCALE_TYPE_ASPECT_RATIO, IMAGE_SCALE_TYPE_STRETCH};

    public static final int DEFAULT_QUALITY = 67;

}
