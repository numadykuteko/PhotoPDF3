package com.pdfconverter.jpg2pdf.pdf.converter.ui.convert;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.ads.control.Admod;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.pdfconverter.jpg2pdf.pdf.converter.BuildConfig;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityConvertBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.exceltopdf.ExcelToPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.texttopdf.TextToPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;

import java.util.Arrays;
import java.util.List;

public class ConvertActivity extends BaseBindingActivity<ActivityConvertBinding, ConvertViewModel> implements ConvertNavigator {
    private List<String> mTypeTitle;

    private int mFirstTimePosition;
    private FragmentPagerItemAdapter mFragmentAdapter;
    private ActivityConvertBinding mActivityConvertBinding;
    private ConvertViewModel mConvertViewModel;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_convert;
    }

    @Override
    public ConvertViewModel getViewModel() {
        mConvertViewModel = ViewModelProviders.of(this).get(ConvertViewModel.class);
        return mConvertViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mConvertViewModel.setNavigator(this);
        mActivityConvertBinding = getViewDataBinding();

        mFirstTimePosition = 0;
        mTypeTitle = Arrays.asList(
                getString(R.string.word_file),
                getString(R.string.excel_file),
                getString(R.string.txt_file));

        initView();
    }

    @Override
    protected void initView() {
        setActionBar(getString(R.string.convert_pdf), true);
        setForClick();
        Admod.getInstance().loadBanner(this, BuildConfig.banner_id);

        if (mFragmentAdapter == null) {
            FragmentPagerItems.Creator pagesCreator = FragmentPagerItems.with(this);

            for (String filterTitle : mTypeTitle) {
                Bundle bundle = new Bundle();
                pagesCreator.add(filterTitle, ConvertFragment.class, bundle);
            }

            FragmentPagerItems pages = pagesCreator.create();
            mFragmentAdapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);

            mActivityConvertBinding.viewpagerTypeFilter.setAdapter(mFragmentAdapter);
            mActivityConvertBinding.viewpagerTypeFilter.setOffscreenPageLimit(3);
            mActivityConvertBinding.viewpagerTypeFilter.setCurrentItem(mFirstTimePosition);

            mActivityConvertBinding.tabTypeFilter.setViewPager(mActivityConvertBinding.viewpagerTypeFilter);

            mActivityConvertBinding.tabTypeFilter.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        mActivityConvertBinding.tabTypeFilter.setSelectedIndicatorColors(ColorUtils.getColorFromResource(getApplicationContext(), R.color.word_file_color));
                    } else if (position == 1) {
                        mActivityConvertBinding.tabTypeFilter.setSelectedIndicatorColors(ColorUtils.getColorFromResource(getApplicationContext(), R.color.excel_file_color));
                    } else {
                        mActivityConvertBinding.tabTypeFilter.setSelectedIndicatorColors(ColorUtils.getColorFromResource(getApplicationContext(), R.color.txt_file_color));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    private void setForClick() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void gotoConvertFile(FileData fileData) {
        Intent intent;
        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_WORD) || fileData.getFileType().equals(DataConstants.FILE_TYPE_TXT)) {
            intent = new Intent(ConvertActivity.this, TextToPdfActivity.class);
        } else {
            intent = new Intent(ConvertActivity.this, ExcelToPdfActivity.class);
        }
        intent.putExtra(EXTRA_FILE_PATH, fileData.getFilePath());
        startActivity(intent);
    }

}
