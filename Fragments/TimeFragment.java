package com.pic.stage.picexplorer.Fragments;

import android.util.Log;

import com.pic.stage.picexplorer.ImageCategoryEntry;
import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TimeFragment extends SortDisplayFragment {
    @Override
    public ArrayList<ImageCategoryEntry> getData() {
        ArrayList<ImgInfo> imgInfos = ((MainActivity) getActivity()).getThumbnailsInfo();
        ArrayList<ImageCategoryEntry> result = new ArrayList<>();
        HashMap<String, Integer> record = new HashMap<>();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        for (ImgInfo info : imgInfos) {
            String s = f.format(new Date(1000 * Long.parseLong(info.getAddedTime())));
            Log.i("日期转化", s);
            // 已存在该日期的记录，直接添加该图片信息
            if (record.get(s) != null) {
                result.get(record.get(s)).addImgInfo(info);
            } else {
                // 新的日期，加入到临时记录中，返回列表创建新的日期分类集合
                record.put(s, result.size());
                ArrayList<ImgInfo> tempList = new ArrayList<>();
                tempList.add(info);
                result.add(new ImageCategoryEntry(s, tempList));
            }
        }
        return result;
    }
}
