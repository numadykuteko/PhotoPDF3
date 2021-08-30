package com.pdfconverter.jpg2pdf.pdf.converter.constants;

import com.pdfconverter.jpg2pdf.pdf.converter.R;

public class DataConstants {

    public static final String DATABASE_NAME = "photo_pdf_application";
    public static final String PREF_NAME = "photo_pdf_application";

    public static final String EMAIL_DEV = "hello@proxglobal.com";

    public static final int CONNECT_TIMEOUT_NETWORK = 10;
    public static final String FAN_INTERSTITIAL_ID = "12212122";

    public static final String PREF_NAME_OPEN_BEFORE = "PREF_NAME_OPEN_BEFORE";
    public static final String PREF_NAME_SHOW_GUIDE_CONVERT = "PREF_NAME_SHOW_GUIDE_CONVERT";
    public static final String PREF_NAME_RATING_US = "PREF_NAME_RATING_US";
    public static final String PREF_NAME_SHOW_GUIDE = "PREF_NAME_SHOW_GUIDE";
    public static final String PREF_NAME_THEME = "PREF_NAME_THEME";
    public static final String PREF_NAME_BACK_TIME = "PREF_NAME_BACK_TIME";
    public static final String PREF_NAME_OPTION_EXCEL = "PREF_NAME_OPTION_EXCEL";
    public static final String PREF_NAME_OPTION_IMAGE = "PREF_NAME_OPTION_IMAGE";
    public static final String PREF_NAME_OPTION_TEXT = "PREF_NAME_OPTION_TEXT";
    public static final String PREF_NAME_OPTION_VIEW_PDF = "PREF_NAME_OPTION_VIEW_PDF";
    public static final String PREF_NAME_COUNTRY_CODE = "PREF_NAME_COUNTRY_CODE";

    public static final String IMAGE_DESCRIPTION = "Image provided by " + DATABASE_NAME;
    public static final String PDF_DESCRIPTION = "PDF provided by " + DATABASE_NAME;
    public static final String WORD_DESCRIPTION = "WORD provided by " + DATABASE_NAME;
    public static final String EXCEL_DESCRIPTION = "EXCEL provided by " + DATABASE_NAME;

    public static final int MAX_SIZE_IMAGE_LOADER_WIDTH = 200;
    public static final int MAX_SIZE_IMAGE_LOADER_HEIGHT = 300;

    public static final int HORIZONTAL_IMAGE_LOADER_WIDTH = 300;
    public static final int HORIZONTAL_IMAGE_LOADER_HEIGHT = 200;

    public static final String FILE_TYPE_PDF = "pdf";
    public static final String FILE_TYPE_WORD = "doc";
    public static final String FILE_TYPE_TEXT = "text";
    public static final String FILE_TYPE_TXT = "txt";
    public static final String FILE_TYPE_EXCEL = "xlsx";
    public static final String FILE_TYPE_DIRECTORY = "__directory";

    public static final String PDF_EXTENSION = ".pdf";
    public static final String TEXT_EXTENSION = ".txt";
    public static final String EXCEL_EXTENSION = ".xls";
    public static final String EXCEL_WORKBOOK_EXTENSION = ".xlsx";
    public static final String DOC_EXTENSION = ".doc";
    public static final String DOCX_EXTENSION = ".docx";

    public static final String IMAGE_DIRECTORY = "/ImageFiles/";

    public static final String TITLE_TYPE_PDF = "PDF";
    public static final String TITLE_TYPE_WORD = "WORD";
    public static final String TITLE_TYPE_EXCEL = "EXCEL";

    public static final int INDEX_TYPE_PDF = 0;
    public static final int INDEX_TYPE_WORD = 1;
    public static final int INDEX_TYPE_EXCEL = 2;

    public static final String EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD";

    public static final boolean USE_DEEP_SEARCH = true;

    public static final int THEME_ORANGE = 0;
    public static final int THEME_BLUE = 2;
    public static final int THEME_JADE = 3;
    public static final int THEME_VIOLET = 4;

    public static final int[] THEME_ITEM_LIST = {R.drawable.bg_item_orange_theme, -1, R.drawable.bg_item_blue_theme, R.drawable.bg_item_jade_theme, R.drawable.bg_item_violet_theme};
    public static final String[] THEME_NAME_LIST = {"Red", "", "Blue", "Jade", "Violet"};

    public static final String TEMP_FILE_NAME = "Temp_file_pdf_converter_";

    public static final String EXCEL_TO_PDF_PREFIX_NAME = "Excel to pdf_";
    public static final String MERGE_FILE_PREFIX_NAME = "Merge pdf_";
    public static final String IMAGE_FILE_PREFIX_NAME = "Image to pdf_";
    public static final String PDF_TO_TEXT_PREFIX_NAME = "Extract to txt_";
    public static final String TEXT_TO_PDF_PREFIX_NAME = "Text to pdf_";
    public static final String NEW_PDF_PREFIX_NAME = "New pdf_";

    public static final int VIEW_MODE_DAY = 0;
    public static final int VIEW_MODE_NIGHT = 1;
    public static final int VIEW_ORIENTATION_VERTICAL = 0;
    public static final int VIEW_ORIENTATION_HORIZONTAL = 1;
}
