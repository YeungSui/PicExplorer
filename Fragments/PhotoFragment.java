package com.pic.stage.picexplorer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.pic.stage.picexplorer.R;

import java.util.ArrayList;

public class PhotoFragment extends Fragment {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private Spinner spinner_sort_menu;
    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        spinner_sort_menu = view.findViewById(R.id.sp_sort_method);
    }

    private void initData() {
        fragments.add(new PhotoThumbnailFragment());
        fragments.add(new TimeFragment());
        fragments.add(new CategoryFragment());
        fragments.add(new RatingFragment());
        fragmentManager = getChildFragmentManager();
        spinner_sort_menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fl_thumbnail_show, fragments.get(position));
                transaction.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fragmentManager.beginTransaction().replace(R.id.fl_thumbnail_show, fragments.get(0)).commit();
            }
        });
        spinner_sort_menu.setSelection(0);
        fragmentManager.beginTransaction().replace(R.id.fl_thumbnail_show, fragments.get(0)).commit();
    }
}
