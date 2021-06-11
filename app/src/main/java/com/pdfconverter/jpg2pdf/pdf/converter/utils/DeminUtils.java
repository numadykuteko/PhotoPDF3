package com.pdfconverter.jpg2pdf.pdf.converter.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;


public class DeminUtils {

    public static int dpToPx(int dp, Context context) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
