package com.pic.stage.picexplorer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.pic.stage.picexplorer.Adapters.SortDisplayAdapter;
import com.pic.stage.picexplorer.ImageCategoryEntry;
import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.R;

import java.util.ArrayList;

public abstract class SortDisplayFragment extends Fragment {
    protected ArrayList<ImageCategoryEntry> categories = new ArrayList<>();
    protected GridView mGridView;
    protected SortDisplayAdapter mAdapter;
    protected ArrayList<Integer> selectedIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort_display, container, false);
        initView(view);
        initData();
        setAdapter();
        setListener(view);
        return view;
    }

    private void initView(View view) {
        mGridView = view.findViewById(R.id.gv_sort_display);
    }

    private void initData() {
        categories = getData();
    }

    private void setAdapter() {
        mAdapter = new SortDisplayAdapter(getActivity(), categories, selectedIds);
        mGridView.setAdapter(mAdapter);
    }

    private void setListener(View view) {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onThumbnailClick(view, position);
            }
        });
    }

    private void onThumbnailClick(View view, int pos) {
        Log.i("点击", view.getClass().getName());
    }

    public void onDeleteImage(ImgInfo info) {
        int x = -1, y = -1;
        for (int i = 0; i < categories.size(); i++) {
            for (int j = 0; j < categories.get(i).getImgInfos().size(); j++) {
                if (categories.get(i).getImgInfos().get(j).getId().equals(info.getId())) {
                    x = i;
                    y = j;
                }
            }
        }
        categories.get(x).getImgInfos().remove(y);
        if (categories.get(x).getImgInfos().size() == 0) {
            categories.remove(x);
        }
        mAdapter.notifyDataSetChanged();
    }

    public abstract ArrayList<ImageCategoryEntry> getData();
}
