package com.pdfconverter.jpg2pdf.pdf.converter.ui.theme;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityThemeBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnThemeItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;

public class ThemeActivity extends BaseBindingActivity<ActivityThemeBinding, ThemeViewModel> implements ThemeNavigator, OnThemeItemClickListener {
    private ActivityThemeBinding mActivityThemeBinding;
    private ThemeViewModel mThemeViewModel;
    private int mSelectedTheme;
    private ThemeAdapter mThemeAdapter;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_theme;
    }

    @Override
    public ThemeViewModel getViewModel() {
        mThemeViewModel = ViewModelProviders.of(this).get(ThemeViewModel.class);
        return mThemeViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemeViewModel.setNavigator(this);
        mActivityThemeBinding = getViewDataBinding();

        setActionBar(getString(R.string.theme_title), true);

        initView();
    }

    @Override
    protected void initView() {
        mSelectedTheme = mThemeViewModel.getSelectedTheme();
        mThemeAdapter = new ThemeAdapter(this);
        mThemeAdapter.setCurrentItem(mSelectedTheme);

        mActivityThemeBinding.dataListArea.setLayoutManager(new LinearLayoutManager(this));
        mActivityThemeBinding.dataListArea.setAdapter(mThemeAdapter);

        mActivityThemeBinding.applyButton.setOnClickListener(view -> {
            mThemeViewModel.setSelectedTheme(mSelectedTheme);
            restartApp(false);
            setResult(RESULT_NEED_FINISH);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void setClick() {

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickItem(int position) {
        mThemeAdapter.setCurrentItem(position);
        mSelectedTheme = position;
    }

    @Override
    public void onFragmentDetached(String tag) {

    }
}
