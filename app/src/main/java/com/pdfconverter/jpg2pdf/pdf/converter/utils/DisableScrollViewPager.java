package com.pdfconverter.jpg2pdf.pdf.converter.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class DisableScrollViewPager extends ViewPager {
    private Boolean disable = false;
    public DisableScrollViewPager(Context context) {
        super(context);
    }
    public DisableScrollViewPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !disable && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !disable && super.onTouchEvent(event);
    }

    public void disableScroll(Boolean disable){
        this.disable = disable;
    }
}
