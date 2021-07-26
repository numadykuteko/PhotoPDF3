package com.pdfconverter.jpg2pdf.pdf.converter.ui.component;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.ads.control.AppPurchase;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.PurchaseAdapter;
import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;
import com.rd.draw.data.Orientation;
import com.rd.draw.data.RtlMode;

public class PurchaseDialog extends BaseCenterDialog {

    private Context mContext;
    private PurchaseListener mListener;

    private PurchaseAdapter mPurchaseAdapter;
    private PageIndicatorView mPageIndicatorView;
    private ViewPager mViewPager;

    private CardView mOption1;
    private TextView mOption1_Name;
    private TextView mOption1_Description;
    private View mOption1_Checkbox;
    private View mOption1_Selected;

    private CardView mOption2;
    private TextView mOption2_Name;
    private TextView mOption2_Description;
    private View mOption2_Checkbox;
    private View mOption2_Selected;

    private ImageView mCloseBtn;
    private CardView mContinue;

    private int mSelectedType = DEFAULT_VALUE;

    private static final int DEFAULT_VALUE = 0;

    @SuppressLint("SetTextI18n")
    public PurchaseDialog(@NonNull Context context, PurchaseListener listener) {
        super(context);
        mContext = context;
        mListener = listener;

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_purchase);

        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        mPurchaseAdapter = new PurchaseAdapter();
        mPageIndicatorView = findViewById(R.id.page_indicator_purchase);
        mViewPager = findViewById(R.id.view_pager_purchase);

        mPageIndicatorView.setAnimationType(AnimationType.WORM);
        mPageIndicatorView.setOrientation(Orientation.HORIZONTAL);
        mPageIndicatorView.setRtlMode(RtlMode.Off);
        mPageIndicatorView.setAutoVisibility(true);

        mViewPager.setAdapter(mPurchaseAdapter);

        mOption1 = findViewById(R.id.purchase_option_1);
        mOption1_Name = findViewById(R.id.purchase_option_1_name);
        mOption1_Description = findViewById(R.id.purchase_option_1_description);
        mOption1_Checkbox = findViewById(R.id.purchase_option_1_checkbox);
        mOption1_Selected = findViewById(R.id.purchase_option_1_selected);
        mOption1_Name.setText(getContext().getString(R.string.purchase_title_1, AppPurchase.getInstance().getPriceSub(BuildConfig.yearly_purchase_key)));

        mOption2 = findViewById(R.id.purchase_option_2);
        mOption2_Name = findViewById(R.id.purchase_option_2_name);
        mOption2_Description = findViewById(R.id.purchase_option_2_description);
        mOption2_Checkbox = findViewById(R.id.purchase_option_2_checkbox);
        mOption2_Selected = findViewById(R.id.purchase_option_2_selected);
        mOption2_Name.setText(getContext().getString(R.string.purchase_title_2, AppPurchase.getInstance().getPriceSub(BuildConfig.monthly_purchase_key)));

        setForSelected();

        mCloseBtn = findViewById(R.id.close_btn);
        mContinue = findViewById(R.id.purchase_continue);

        mOption1.setOnClickListener(v -> {
            mSelectedType = 0;
            setForSelected();
        });

        mOption2.setOnClickListener(v -> {
            mSelectedType = 1;
            setForSelected();
        });

        mCloseBtn.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCancel();
            }

            dismiss();
        });

        mContinue.setOnClickListener(v -> {
            if (mListener != null) {
                ToastUtils.showMessageShort(getContext(), getContext().getString(R.string.purchase_start));
                mListener.onSelectPurchase(mSelectedType);
            }

            dismiss();
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForSelected() {
        if (mSelectedType == 0) {
            mOption1_Name.setTextColor(ColorUtils.getColorFromResource(getContext(), R.color.redLight));
            mOption2_Name.setTextColor(ColorUtils.getColorFromResource(getContext(), R.color.black_totally));

            mOption1_Checkbox.setBackground(getContext().getDrawable(R.drawable.bg_purchase_selected));
            mOption2_Checkbox.setBackground(getContext().getDrawable(R.drawable.bg_purchase_not_selected));

            mOption1_Selected.setVisibility(View.VISIBLE);
            mOption2_Selected.setVisibility(View.GONE);
        } else {
            mOption1_Name.setTextColor(ColorUtils.getColorFromResource(getContext(), R.color.black_totally));
            mOption2_Name.setTextColor(ColorUtils.getColorFromResource(getContext(), R.color.redLight));

            mOption1_Checkbox.setBackground(getContext().getDrawable(R.drawable.bg_purchase_not_selected));
            mOption2_Checkbox.setBackground(getContext().getDrawable(R.drawable.bg_purchase_selected));

            mOption1_Selected.setVisibility(View.GONE);
            mOption2_Selected.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public interface PurchaseListener {
        void onSelectPurchase(int type);
        void onCancel();
    }
}
