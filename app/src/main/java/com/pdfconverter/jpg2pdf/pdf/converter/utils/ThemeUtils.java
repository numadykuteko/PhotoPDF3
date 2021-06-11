package com.pdfconverter.jpg2pdf.pdf.converter.utils;

import android.content.Context;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;

public class ThemeUtils {
    public static int getBaseThemeColor(Context context) {
        DataManager dataManager = DataManager.getInstance(context);
        int themeIndex = dataManager.getTheme();

        switch (themeIndex) {
            case DataConstants.THEME_ORANGE:
                return ColorUtils.getColorFromResource(context, R.color.orange_theme_color);
            case DataConstants.THEME_BLUE:
                return ColorUtils.getColorFromResource(context, R.color.blue_theme_color);
            case DataConstants.THEME_JADE:
                return ColorUtils.getColorFromResource(context, R.color.jade_theme_color);
            case DataConstants.THEME_VIOLET:
                return ColorUtils.getColorFromResource(context, R.color.violet_theme_color);
        }

        return ColorUtils.getColorFromResource(context, R.color.orange_theme_color);
    }
}
