package com.pic.stage.picexplorer.Fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pic.stage.picexplorer.Adapters.HomepageAdapter;
import com.pic.stage.picexplorer.R;

import java.util.ArrayList;
import java.util.List;

public class HomepageFragment extends Fragment {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private List<Fragment> mFragments;
    private List<String> mTitles;
    private HomepageAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);
        initView(view);
        initData(view);
        setData();
        setListeners();
        return view;
    }

    private void setData() {
        mViewPager.setAdapter(mAdapter);
        //设置Viewpager和Tablayout进行联动
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initData(View view) {
        //初始化导航标题,如果是title在json数据中,在初始化的时候可以使用异步任务加载的形式添加
        mTitles = new ArrayList<>();
        mTitles.add("照片");
        mTitles.add("图片");
        //初始化Fragment
        mFragments = new ArrayList<>();
        for (int i = 0; i < mTitles.size(); i++) {
            if (i == 0) {
                mFragments.add(new PhotoFragment());
            } else if (i == 1) {
                mFragments.add(new PictureFragment());
            }
        }
        //getSupportFragmentManager()是Activity嵌套fragment时使用
        //getChildFragmentManager()是Fragment嵌套Fragment时使用
        mAdapter = new HomepageAdapter(getChildFragmentManager(), mFragments, mTitles);
        mAdapter.notifyDataSetChanged();
    }

    private void initView(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.vp_homepage_show);
        mTabLayout = (TabLayout) view.findViewById(R.id.homepage_navigation);
    }

    private void setListeners() {

        // 设置主页的监听器，要判断imageview的状态是否为visible
        // 若是点击主页任何部分将会
        // 恢复底部按钮
        // 隐藏imageview
        getActivity().findViewById(R.id.main_page_outline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView iv = getActivity().findViewById(R.id.iv_image_display);
                if (iv.getVisibility() == View.VISIBLE) {

                }
            }
        });
    }

    // 向父activity提供子fragment对象
    // 子fragment有部分服务需要父activity协助
    // 父activity的删除按钮删除zifragment中的缩略图
    public PhotoThumbnailFragment getPhotoThumbnailFragment() {
        return (PhotoThumbnailFragment) mFragments.get(0);
    }
}
