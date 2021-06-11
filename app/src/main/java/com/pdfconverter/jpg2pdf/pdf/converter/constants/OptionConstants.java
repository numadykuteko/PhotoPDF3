package com.pdfconverter.jpg2pdf.pdf.converter.constants;

import com.aspose.cells.PaperSizeType;
import com.itextpdf.text.Font;

public class OptionConstants {
    public static final int POSITION_CENTER = 0;
    public static final int POSITION_TOP_LEFT = 1;
    public static final int POSITION_TOP_RIGHT = 2;
    public static final int POSITION_BOTTOM_LEFT = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;
    public static final int POSITION_TOP_CENTER = 5;
    public static final int POSITION_BOTTOM_CENTER = 6;

    public static final String[] LIST_FONT_FAMILY = {
            "assets/fonts/Arial.ttf",
            "assets/fonts/Book Antiqua.ttf",
            "assets/fonts/Calibri.ttf",
            "assets/fonts/Century.ttf",
            "assets/fonts/Courier New.ttf",
            "assets/fonts/Crimson.ttf",
            "assets/fonts/Lucida Bright.ttf",
            "assets/fonts/Roboto-Medium.ttf",
            "assets/fonts/Segoe UI Historic.ttf",
            "assets/fonts/Times New Roman.ttf"
    };

    public static final String[] LIST_FONT_FAMILY_NAME = {
            "Arial",
            "Book Antiqua",
            "Calibri",
            "Century",
            "Courier New",
            "Crimson",
            "Lucida Bright",
            "Roboto",
            "Segoe UI Historic",
            "Times New Roman"
    };

    public static final int[] LIST_FONT_STYLE = {
            Font.NORMAL,
            Font.BOLD,
            Font.ITALIC,
            Font.UNDERLINE,
            Font.BOLDITALIC,
            Font.STRIKETHRU
    };

    public static final String[] LIST_FONT_STYLE_NAME = {
            "Normal",
            "Bold",
            "Italic",
            "Underline",
            "Bold italic",
            "Strikethru"
    };

    public static final int[] LIST_POSITION = {
            POSITION_CENTER,
            POSITION_TOP_LEFT,
            POSITION_TOP_RIGHT,
            POSITION_TOP_CENTER,
            POSITION_BOTTOM_LEFT,
            POSITION_BOTTOM_RIGHT,
            POSITION_BOTTOM_CENTER,
    };

    public static final String[] LIST_POSITION_NAME = {
            "Center",
            "Top left",
            "Top right",
            "Top center",
            "Bottom left",
            "Bottom right",
            "Bottom center",
    };

    public static final String[] LIST_PAGE_SIZE = {"A4", "A0", "A1", "A2", "A3", "B0", "B1", "B2", "LETTER", "LEGAL", "TABLOID"};

    public static final String[] LIST_PAGE_SIZE_EXCEL = {"A4", "A3", "A2", "B3", "B4", "B5", "LETTER", "LEGAL", "TABLOID"};
    public static final int[] LIST_PAGE_SIZE_EXCEL_VALUE = {PaperSizeType.PAPER_A_4, PaperSizeType.PAPER_A_3, PaperSizeType.PAPER_A_2, PaperSizeType.PAPER_B_3, PaperSizeType.PAPER_B_4, PaperSizeType.PAPER_B_5, PaperSizeType.PAPER_LETTER, PaperSizeType.PAPER_LEGAL, PaperSizeType.PAPER_TABLOID};

    public static final String[] LIST_PAGE_ORIENTATION = {"Portrait", "Landscape"};

    public static final String DEFAULT_PAGE_SIZE = "A4";
    public static final String DEFAULT_PAGE_ORIENTATION = "Portrait";
    public static final int DEFAULT_PAGE_SIZE_EXCEL = PaperSizeType.PAPER_A_4;
    public static final int DEFAULT_FONT_SIZE = 14;
    public static final int DEFAULT_ANGLE = 0;
    public static final String DEFAULT_FONT_FAMILY = LIST_FONT_FAMILY[0];
    public static final int DEFAULT_FONT_STYLE = Font.NORMAL;
    public static final int DEFAULT_THEME = DataConstants.THEME_ORANGE;

}
