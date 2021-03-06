package com.snqu.shopping.ui.main.frag.channel.plate;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;

import com.android.util.ext.ToastUtil;
import com.anroid.base.SimpleFrag;
import com.anroid.base.SimpleFragAct;
import com.anroid.base.ui.StatusBar;
import com.snqu.shopping.R;
import com.snqu.shopping.data.base.NetReqResult;
import com.snqu.shopping.data.base.ResponseDataArray;
import com.snqu.shopping.data.home.entity.CategoryEntity;
import com.snqu.shopping.data.home.entity.PlateCategoryEntity;
import com.snqu.shopping.ui.main.viewmodel.HomeViewModel;
import com.snqu.shopping.util.statistics.DataCache;
import com.snqu.shopping.util.statistics.SndoData;
import com.snqu.shopping.util.statistics.StatisticInfo;

import java.util.ArrayList;
import java.util.List;

import common.widget.viewpager.ViewPager;
import common.widget.viewpager.indicator.TabPageIndicator;
import common.widget.viewpager.indicator.TitleIndicator;

/**
 * @author 张全
 */
public class CommonPlateFrag extends SimpleFrag {
    private static final String PARAM = "PARAM";
    private static final String TYPE = "TYPE";
    private List<View> lines = new ArrayList<>();
    private List<CategoryEntity> categoryList = new ArrayList<>();
    private List<CommonPlateItemFrag> fragList = new ArrayList<>();

    private ViewPager viewPager;
    private PlateCategoryEntity plateCategoryEntity;
    private TabPageIndicator tabPageIndicator;
    private HomeViewModel mHomeViewModel;

    public static void start(Context ctx, PlateCategoryEntity plateCategoryEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM, plateCategoryEntity);
        SimpleFragAct.start(ctx, new SimpleFragAct.SimpleFragParam(plateCategoryEntity.name, CommonPlateFrag.class, bundle).showBg());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.channel_list_frag;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        StatusBar.setStatusBar(mContext, false, getTitleBar());

        plateCategoryEntity = (PlateCategoryEntity) getArguments().getSerializable(PARAM);

        initView();
        initData();
    }

    private void initData() {
        mHomeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        mHomeViewModel.mNetReqResultLiveData.observe(getLifecycleOwner(), new Observer<NetReqResult>() {
            @Override
            public void onChanged(@Nullable NetReqResult netReqResult) {
                switch (netReqResult.tag) {
                    case HomeViewModel.TAG_PLATE_LIST:
                        if (netReqResult.successful) {
                            ResponseDataArray<CategoryEntity> responseDataArray = (ResponseDataArray<CategoryEntity>) netReqResult.data;
                            List<CategoryEntity> categoryEntities = responseDataArray.getDataList();
                            if (categoryEntities.isEmpty()) {
                                return;
                            }
                            initPlateList(categoryEntities);
                        } else {
                            ToastUtil.show(netReqResult.message);
                        }
                        break;
                }
            }
        });
        mHomeViewModel.getPlateList(plateCategoryEntity.plate_id);

    }

    private void initView() {
        getTitleBar().setTitleTextColor(R.color.white);
        getTitleBar().setLeftBtnDrawable(R.drawable.back_white);

        tabPageIndicator = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setSmoothScroll(false);
//        viewPager.setPagingEnabled(false);
        viewPager.setOffscreenPageLimit(3);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        tabPageIndicator.setTitleInidcator(new TitleIndicator() {
            @Override
            public View addTab(int index, CharSequence title) {
                View view = inflater.inflate(R.layout.home_tab_title_layout, null);
                TextView textView = view.findViewById(R.id.tv_title);
                textView.setText(title);
                lines.add(view);
                if (index == 0) {
                    view.setBackgroundResource(R.drawable.home_category_title_bg);
                    view.setSelected(true);
                }
                return view;
            }
        });
    }

    private void initPlateList(List<CategoryEntity> categoryEntities) {
        categoryList = categoryEntities;
        CategoryEntity item = categoryList.get(0);
        StatisticInfo statisticInfo = new StatisticInfo();
        statisticInfo.viewPage(item.pid, item._id);

        //深度数据统计
        DataCache.plate_second_id = item._id;
        DataCache.plate_second_name = item.title;
        SndoData.event(SndoData.XLT_EVENT_PLATE,
                SndoData.XLT_ITEM_ID, item._id,
                SndoData.XLT_ITEM_TITLE, item.title,
                SndoData.XLT_ITEM_PID, item.pid
        );

        viewPager.setAdapter(new ChannelTabPagerAdapter(getChildFragmentManager()));
        tabPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float v, int i1) {

            }

            @Override
            public void onPageSelected(int pos) {
                for (View view : lines) {
                    view.setSelected(false);
                    view.setBackground(null);
                }
                lines.get(pos).setBackgroundResource(R.drawable.home_category_title_bg);
                lines.get(pos).setSelected(true);

                CategoryEntity item = categoryList.get(pos);
                StatisticInfo statisticInfo = new StatisticInfo();
                statisticInfo.viewPage(item.pid, item._id);

                //深度数据统计
                DataCache.plate_second_id = item._id;
                DataCache.plate_second_name = item.title;
                SndoData.event(SndoData.XLT_EVENT_PLATE,
                        SndoData.XLT_ITEM_ID, item._id,
                        SndoData.XLT_ITEM_TITLE, item.title,
                        SndoData.XLT_ITEM_PID, item.pid
                );
            }

            @Override
            public void onPageScrollStateChanged(int pos) {

            }
        });
        tabPageIndicator.setViewPager(viewPager);
    }

    private class ChannelTabPagerAdapter extends FragmentStatePagerAdapter {

        public ChannelTabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            CommonPlateItemFrag page = new CommonPlateItemFrag();
            page.setArguments(CommonPlateItemFrag.getParam(categoryList.get(position)));
            return page;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).title;
        }
    }
}