package com.pdfconverter.jpg2pdf.pdf.converter.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.AppConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.constants.DataConstants;
import com.pdfconverter.jpg2pdf.pdf.converter.data.DataManager;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.ActivityMainBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseBindingActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.browser.BrowserFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.home.HomeFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.more.MoreFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.theme.ThemeActivity;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.DialogFactory;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.FirebaseUtils;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.ToastUtils;

import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseBindingActivity<ActivityMainBinding, MainViewModel> implements MainNavigator {

    public static final String HOME = "HOME";
    public static final String TOOL = "TOOL";
    public static final String BROWSER = "BROWSER";

    public static final List<String> mListScreenId = Arrays.asList(HOME, TOOL, BROWSER);

    private int mCurrentScreen;

    private ActionBarDrawerToggle mDrawerToggle;

    private MainViewModel mMainViewModel;
    private ActivityMainBinding mActivityMainBinding;

    private final int REQUEST_EXTERNAL_PERMISSION_FOR_MAIN = 1;
    private SweetAlertDialog mRequestPermissionDialog;

    private int mSelectedItem = 0;
    private boolean mIsFromFirstOpen;
    private BrowserFragment mBrowserFragment;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public MainViewModel getViewModel() {
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        return mMainViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainViewModel.setNavigator(this);
        mActivityMainBinding = getViewDataBinding();

        mIsFromFirstOpen = getIntent().getBooleanExtra(EXTRA_FROM_FIRST_OPEN, false);

        if (mIsFromFirstOpen) {
            gotoActivityWithFlag(AppConstants.FLAG_IMAGE_TO_PDF_FROM_HOME);
        }

        initView();
    }

    @Override
    public void onBackPressed() {
        if (mActivityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (mCurrentScreen != 0) {
            if (mCurrentScreen != 2) {
                mSelectedItem = 0;
                MenuItem currentItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_home);
                if (currentItem != null) {
                    currentItem.setCheckable(true);
                    currentItem.setChecked(true);
                }

                goToScreen(HOME, false);
            } else {
                if (mBrowserFragment != null && !mBrowserFragment.isCurrentFolderRoot()) {
                    mBrowserFragment.onBackPress();
                } else {
                    mSelectedItem = 0;
                    MenuItem currentItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_home);
                    if (currentItem != null) {
                        currentItem.setCheckable(true);
                        currentItem.setChecked(true);
                    }
                    goToScreen(HOME, false);
                }
            }

        } else {
            DataManager dataManager = DataManager.getInstance(this);
            List<Integer> requestRateTimeList = Arrays.asList(0, 1, 2, 3, 5, 7, 9);
            if (requestRateTimeList.contains(dataManager.getBackTime())) {
                dataManager.increaseBackTime();
                if (!dataManager.checkRatingUsDone()) {
                    showRatingUsPopup(super::onBackPressed, super::onBackPressed);
                } else {
                    super.onBackPressed();
                }
            } else {
                dataManager.increaseBackTime();
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == BaseBindingActivity.RESULT_NEED_FINISH) {
                finish();
                setResult(RESULT_NEED_FINISH);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void initView() {
        setTabView();
        if (!mIsFromFirstOpen) {
            checkPermissionOnMain();
        }

        mActivityMainBinding.searchToolbar.setOnClickListener(view -> gotoActivityWithFlag(AppConstants.FLAG_SEARCH_PDF));
    }

    private void checkPermissionOnMain() {
        if (notHaveStoragePermission()) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestReadStoragePermissionsSafely(REQUEST_EXTERNAL_PERMISSION_FOR_MAIN);
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_MAIN:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.thank_you_for_support));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.reject_read_file));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsRequestFullPermission) {
            mIsRequestFullPermission = false;

            if (!notHaveStoragePermission()) {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                mRequestPermissionDialog.setContentText(getString(R.string.thank_you_for_support));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
            } else {
                mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                mRequestPermissionDialog.setContentText(getString(R.string.reject_read_file));
                mRequestPermissionDialog.showCancelButton(false);
                mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
            }
        }
    }

    @Override
    protected void setClick() {
    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setClickSortItem(View.OnClickListener listener) {
        mActivityMainBinding.sortToolbar.setOnClickListener(listener);
    }

    public void gotoFeedBackApplication() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{DataConstants.EMAIL_DEV});
        startActivity(intent);
    }

    public void gotoMoreApp() {
        String url = "https://play.google.com/store/apps/developer?id=Andromeda+App";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void shareApplicationLink() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = "\nLet me recommend you this PDF converter application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose one"));
        } catch (Exception e) {
            ToastUtils.showMessageShort(this, getString(R.string.can_not_connect_to_network_text));
        }
    }


    public void gotoPolicyApp() {
        String url = "https://firebasestorage.googleapis.com/v0/b/photos-to-pdf-44d63.appspot.com/o/Privacy_Policy.html?alt=media&token=9e316366-1b40-4c49-828a-dfdcb6d9d669";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void gotoThemeActivity() {
        Intent intent = new Intent(MainActivity.this, ThemeActivity.class);
        startActivityForResult(intent, 1);
    }

    private void setTabView() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mActivityMainBinding.drawerLayout, mActivityMainBinding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mActivityMainBinding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mSelectedItem = 0;
        MenuItem homeItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_home);
        MenuItem toolItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_tool);
        MenuItem browserItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_browser);

        if (homeItem != null) {
            homeItem.setCheckable(true);
            homeItem.setChecked(true);
        }

        if (toolItem != null) {
            toolItem.setCheckable(true);
        }

        if (browserItem != null) {
            browserItem.setCheckable(true);
        }

        goToScreen(HOME, false);

        mActivityMainBinding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                if (mSelectedItem != 0) {
                    mSelectedItem = 0;
                    MenuItem currentItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_home);
                    if (currentItem != null) {
                        currentItem.setCheckable(true);
                        currentItem.setChecked(true);
                    }

                    goToScreen(HOME, false);
                }
            } else if (id == R.id.navigation_tool) {
                if (mSelectedItem != 1) {
                    mSelectedItem = 1;
                    MenuItem currentItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_tool);
                    if (currentItem != null) {
                        currentItem.setCheckable(true);
                        currentItem.setChecked(true);
                    }

                    goToScreen(TOOL, false);
                }
            } else if (id == R.id.navigation_browser) {
                if (mSelectedItem != 2) {
                    mSelectedItem = 2;
                    MenuItem currentItem = mActivityMainBinding.navView.getMenu().findItem(R.id.navigation_browser);
                    if (currentItem != null) {
                        currentItem.setCheckable(true);
                        currentItem.setChecked(true);
                    }

                    goToScreen(BROWSER, false);
                }
            } else if (id == R.id.navigation_drawer_privacy) {
                gotoPolicyApp();
            } else if (id == R.id.navigation_feedback) {
                gotoFeedBackApplication();
            } else if (id == R.id.navigation_rate) {
                showRatingUsPopup(() -> {
                }, () -> {
                });
            } else if (id == R.id.navigation_drawer_more_app) {
                gotoMoreApp();
            } else if (id == R.id.navigation_drawer_pro_version) {
                FirebaseUtils.sendEventFunctionUsed(this, "Upgrade pro version", "From home");
                ToastUtils.showFunctionNotSupportToast(this);
            } else if (id == R.id.navigation_drawer_share) {
                shareApplicationLink();
            }
            mActivityMainBinding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    public void loadFragment(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            mActivityMainBinding.searchToolbar.setVisibility(View.VISIBLE);
            mActivityMainBinding.sortToolbar.setVisibility(View.VISIBLE);
        } else {
            mActivityMainBinding.searchToolbar.setVisibility(View.GONE);
            mActivityMainBinding.sortToolbar.setVisibility(View.GONE);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    public void setVisibleSortToolbar(int visibleSortToolbar) {
        mActivityMainBinding.sortToolbar.setVisibility(visibleSortToolbar);
    }

    public void goToScreen(String screenId, boolean isDelay) {
        int indexScreen = mListScreenId.indexOf(screenId);
        if (indexScreen == -1) return;

        if (indexScreen < mListScreenId.size()) {
            mCurrentScreen = indexScreen;

            if (indexScreen == 0) {
                loadFragment(HomeFragment.newInstance());
                mActivityMainBinding.titleToolbar.setText(getString(R.string.app_name));
            } else if (indexScreen == 1) {
                mActivityMainBinding.titleToolbar.setText(getString(R.string.menu_drawer_tool));
                loadFragment(MoreFragment.newInstance());
            } else if (indexScreen == 2) {
                mActivityMainBinding.titleToolbar.setText(getString(R.string.menu_drawer_browser));
                mBrowserFragment = BrowserFragment.newInstance();
                loadFragment(mBrowserFragment);
            }
        }
    }
}
