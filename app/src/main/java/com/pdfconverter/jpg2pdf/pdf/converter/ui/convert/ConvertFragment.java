package com.pdfconverter.jpg2pdf.pdf.converter.ui.convert;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.pdfconverter.jpg2pdf.pdf.converter.R;
import com.pdfconverter.jpg2pdf.pdf.converter.data.model.FileData;
import com.pdfconverter.jpg2pdf.pdf.converter.databinding.FragmentConvertBinding;
import com.pdfconverter.jpg2pdf.pdf.converter.listener.OnFileItemClickListener;
import com.pdfconverter.jpg2pdf.pdf.converter.ui.base.BaseFragment;
import com.pdfconverter.jpg2pdf.pdf.converter.utils.adapter.FileListNoAdsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConvertFragment extends BaseFragment<FragmentConvertBinding, ConvertViewModel> implements ConvertNavigator, OnFileItemClickListener {
    private FragmentConvertBinding mFragmentConvertBinding;
    private ConvertViewModel mConvertViewModel;
    private int mPosition;

    private int mNoDataString = R.string.no_word_found, mNoDataImage = R.drawable.ic_no_file;
    private final int[] STRING_NO_DATA_LIST = {R.string.no_word_found, R.string.no_excel_found, R.string.no_txt_found};
    private final int[] IMAGE_NO_DATA_LIST = {R.drawable.ic_no_file, R.drawable.ic_no_file, R.drawable.ic_no_file};

    private boolean mIsLoading = false;
    private List<FileData> mDataList;
    private FileListNoAdsAdapter mDataListAdapter;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_convert;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    @Override
    public ConvertViewModel getViewModel() {
        mConvertViewModel = ViewModelProviders.of(this).get(ConvertViewModel.class);
        return mConvertViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConvertViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentConvertBinding = getViewDataBinding();
        mPosition = FragmentPagerItem.getPosition(getArguments());
        mConvertViewModel.setType(mPosition);
        mNoDataString = STRING_NO_DATA_LIST[mPosition];
        mNoDataImage = IMAGE_NO_DATA_LIST[mPosition];

        setForLiveData();
        setForRecyclerView();
        setForPullRefresh();

        startGetData(true);
    }

    private void setForLiveData() {
        mConvertViewModel.getListFileLiveData().observe(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(List<FileData> dataList) {
        mIsLoading = false;
        mFragmentConvertBinding.pullToRefresh.setRefreshing(false);

        if (dataList == mDataList) {
            return;
        }

        if (dataList.size() > 0) {
            mDataList = new ArrayList<>();
            mDataList.addAll(dataList);

            mDataListAdapter.setData(mDataList);

            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void setForRecyclerView() {
        mDataListAdapter = new FileListNoAdsAdapter(this);

        mFragmentConvertBinding.dataListArea.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFragmentConvertBinding.dataListArea.setAdapter(mDataListAdapter);
    }

    private void setForPullRefresh() {
        mFragmentConvertBinding.pullToRefresh.setOnRefreshListener(() -> {
            startGetData(false);
        });
    }

    private void startGetData(boolean forceReload) {
        if (mIsLoading) {
            mFragmentConvertBinding.pullToRefresh.setRefreshing(false);
            return;
        }

        if (forceReload) {
            showLoadingArea();
        }

        mIsLoading = true;
        mConvertViewModel.getFileList();
    }

    private void showLoadingArea() {
        mFragmentConvertBinding.loadingArea.setVisibility(View.VISIBLE);

        mFragmentConvertBinding.dataListArea.setVisibility(View.GONE);
        mFragmentConvertBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showNoDataArea() {
        mFragmentConvertBinding.noDataErrorTv.setText(mNoDataString);
        if (getActivity() != null) {
            mFragmentConvertBinding.noDataErrorImg.setImageDrawable(getActivity().getDrawable(mNoDataImage));
        }
        mFragmentConvertBinding.noDataErrorArea.setVisibility(View.VISIBLE);

        mFragmentConvertBinding.dataListArea.setVisibility(View.GONE);
        mFragmentConvertBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mFragmentConvertBinding.dataListArea.setVisibility(View.VISIBLE);

        mFragmentConvertBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentConvertBinding.loadingArea.setVisibility(View.GONE);
    }

    @Override
    public void onClickItem(int position) {
        if (position >= 0 && position < mDataList.size()) {
            FileData fileData = mDataList.get(position);

            if (mActivity != null && mActivity instanceof ConvertActivity) {
                ((ConvertActivity) mActivity).gotoConvertFile(fileData);
            }
        }
    }
}

