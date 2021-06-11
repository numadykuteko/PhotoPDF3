package com.pdfconverter.jpg2pdf.pdf.converter.ui.home;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.NewPDFOptions;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.FragmentHomeBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.bookmark.BookmarkFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.component.SettingNewPdfDialog;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.formatpdf.FormatPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.imagetopdf.ImageToPdfActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.lib.LibFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.main.MainActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.recent.RecentFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ColorUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.TabViewAdapter;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.file.FileUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.pdf.ImageToPdfConstants;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends BaseFragment<FragmentHomeBinding, HomeViewModel> implements HomeNavigator {
    private HomeViewModel mHomeViewModel;
    private FragmentHomeBinding mFragmentDiscoversBinding;
    private TabViewAdapter adapter;
    private Boolean mIsFabOpen = false;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_HOME = 1;
    private SweetAlertDialog mRequestPermissionDialog;
    private NewPDFOptions mNewPDFOptions;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    @Override
    public HomeViewModel getViewModel() {
        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        return mHomeViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHomeViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentDiscoversBinding = getViewDataBinding();

        if (mActivity != null) {
            mActivity.preloadMyPdfAdsIfInit();
            mActivity.preloadHomeAdsIfInit();
        }

        initCommonView();
        initView();
        setForClick();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivity != null) {
            mActivity.setNoActionBar();
        }
    }


    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        Bundle args = new Bundle();
        homeFragment.setArguments(args);
        homeFragment.setRetainInstance(true);

        return homeFragment;
    }

    private void initCommonView() {

    }

    private void initView() {
        setForFabOpenPdf();

        adapter = new TabViewAdapter(getChildFragmentManager());
        adapter.addFragment(LibFragment.newInstance(), "All files");
        adapter.addFragment(BookmarkFragment.newInstance(), "Bookmarks");
        adapter.addFragment(RecentFragment.newInstance(), "Converted");
        mFragmentDiscoversBinding.viewPager.setOffscreenPageLimit(3);
        mFragmentDiscoversBinding.viewPager.setAdapter(adapter);
        mFragmentDiscoversBinding.tabLayout.setupWithViewPager(mFragmentDiscoversBinding.viewPager);

        mFragmentDiscoversBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (mActivity instanceof MainActivity) {
                        ((MainActivity) mActivity).setVisibleSortToolbar(View.VISIBLE);
                    }
                } else {
                    if (mActivity instanceof MainActivity) {
                        ((MainActivity) mActivity).setVisibleSortToolbar(View.GONE);
                    }
                }

                try {
                    Fragment frag = adapter.getItem(position);
                    if (frag instanceof BookmarkFragment && ((BookmarkFragment) frag).isCreatedView()) {
                        ((BookmarkFragment) frag).reloadData(false);
                    } else if (frag instanceof RecentFragment && ((RecentFragment) frag).isCreatedView()) {
                        ((RecentFragment) frag).reloadData(false);
                    }
                } catch (Exception e) {
                    // donothing
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setForFabOpenPdf() {
        Animation mFabClockAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.fab_rotate_lock);
        Animation mFabAnticlockAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.fab_rotate_antilock);

        mFragmentDiscoversBinding.createPdfFabLayout.fabViewOption.setOnClickListener(view -> {
            if (mIsFabOpen) {
                mIsFabOpen = false;
                updateFabStatus();

                mFragmentDiscoversBinding.createPdfFabLayout.fabViewOption.startAnimation(mFabAnticlockAnimation);
            } else {

                if (mActivity.notHaveStoragePermission()) {
                    mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(mActivity, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_HOME);
                        sweetAlertDialog.dismiss();
                    });
                    mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                        sweetAlertDialog.setContentText(getString(R.string.reject_read_file));
                        sweetAlertDialog.setConfirmClickListener(Dialog::dismiss);
                        sweetAlertDialog.showCancelButton(false);
                        sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
                    });
                    mRequestPermissionDialog.show();

                    return;
                }

                mIsFabOpen = true;
                updateFabStatus();

                mFragmentDiscoversBinding.createPdfFabLayout.fabViewOption.startAnimation(mFabClockAnimation);
                mFragmentDiscoversBinding.createPdfFabLayout.fabFromImage.setOnClickListener(v -> mActivity.showHomeAdsBeforeAction(() -> {
                    Intent imagePdfIntent = new Intent(mActivity, ImageToPdfActivity.class);
                    startActivity(imagePdfIntent);

                    FirebaseUtils.sendEventFunctionUsed(mActivity, "Image To Pdf", "From home");
                }));

                mFragmentDiscoversBinding.createPdfFabLayout.fabNewPdf.setOnClickListener(v -> {
                    if (mNewPDFOptions == null) {
                        mNewPDFOptions = new NewPDFOptions(0, FileUtils.getDefaultFileName(DataConstants.NEW_PDF_PREFIX_NAME), ImageToPdfConstants.DEFAULT_PAGE_SIZE, 1);
                    } else {
                        mNewPDFOptions.setFileName(FileUtils.getDefaultFileName(DataConstants.NEW_PDF_PREFIX_NAME));
                    }

                    SettingNewPdfDialog settingNewPdfDialog = new SettingNewPdfDialog(mNewPDFOptions, mOptions -> {
                        if (mOptions == null || mOptions.getFileName() == null || mOptions.getFileName().length() == 0 || mOptions.getNumberPage() < 1 || mOptions.getNumberPage() > 999) {
                            return;
                        }

                        mActivity.showHomeAdsBeforeAction(() -> {
                            mNewPDFOptions = mOptions;

                            Intent imagePdfIntent = new Intent(mActivity, FormatPdfActivity.class);

                            Gson gson = new Gson();
                            String json = gson.toJson(mNewPDFOptions);
                            imagePdfIntent.putExtra(FormatPdfActivity.INTENT_PDF_OPTION, json);

                            startActivity(imagePdfIntent);

                            FirebaseUtils.sendEventFunctionUsed(mActivity, "Create new pdf", "From home");
                        });
                    });
                    settingNewPdfDialog.show(getChildFragmentManager(), settingNewPdfDialog.getTag());
                });
            }
        });
    }

    private void updateFabStatus() {
        mFragmentDiscoversBinding.createPdfFabLayout.fabFromImage.setClickable(mIsFabOpen);
        mFragmentDiscoversBinding.createPdfFabLayout.fabNewPdf.setClickable(mIsFabOpen);
        mFragmentDiscoversBinding.createPdfFabLayout.textviewFromImage.setClickable(mIsFabOpen);
        mFragmentDiscoversBinding.createPdfFabLayout.textviewNewPdf.setClickable(mIsFabOpen);

        Animation mFabCloseAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.fab_close);
        Animation mFabOpenAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.fab_open);
        Animation mTextOpenAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.text_open);
        Animation mTextCloseAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.text_close);
        Animation fabAnimation, textAnimation;

        if (mIsFabOpen) {
            fabAnimation = mFabOpenAnimation;
            textAnimation = mTextOpenAnimation;
            mFragmentDiscoversBinding.createPdfFabLayout.contentFab.setBackgroundColor(ColorUtils.getColorFromResource(mActivity, R.color.black_semi_transparent));
            mFragmentDiscoversBinding.createPdfFabLayout.contentFab.setVisibility(View.VISIBLE);
            mFragmentDiscoversBinding.createPdfFabLayout.contentFab.setOnClickListener(v -> {
                mIsFabOpen = false;
                updateFabStatus();
                Animation mFabAnticlockAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.fab_rotate_antilock);
                mFragmentDiscoversBinding.createPdfFabLayout.fabViewOption.startAnimation(mFabAnticlockAnimation);
            });
        } else {
            fabAnimation = mFabCloseAnimation;
            textAnimation = mTextCloseAnimation;
            mFragmentDiscoversBinding.createPdfFabLayout.contentFab.setBackgroundColor(ColorUtils.getColorFromResource(mActivity, R.color.transparent));
            mFragmentDiscoversBinding.createPdfFabLayout.contentFab.setVisibility(View.GONE);
            mFragmentDiscoversBinding.createPdfFabLayout.contentFab.setOnClickListener(v -> {

            });
        }

        mFragmentDiscoversBinding.createPdfFabLayout.textviewFromImage.startAnimation(textAnimation);
        mFragmentDiscoversBinding.createPdfFabLayout.textviewNewPdf.startAnimation(textAnimation);
        mFragmentDiscoversBinding.createPdfFabLayout.fabFromImage.startAnimation(fabAnimation);
        mFragmentDiscoversBinding.createPdfFabLayout.fabNewPdf.startAnimation(fabAnimation);
    }

    private void setForClick() {

    }
}
