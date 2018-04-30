package com.pic.stage.picexplorer.Fragments;

import android.util.Log;

import com.pic.stage.picexplorer.ClassificationItem;
import com.pic.stage.picexplorer.ImageCategoryEntry;
import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryFragment extends SortDisplayFragment {
    @Override
    public ArrayList<ImageCategoryEntry> getData() {
        HashMap<Integer, ImgInfo> imgInfos = ((MainActivity) getActivity()).getThumbnailsInfoMap();
        ArrayList<ImageCategoryEntry> result = new ArrayList<>();
        HashMap<String, Integer> record = new HashMap<>();
        HashMap<Integer, String> category = ((MainActivity) getActivity()).getCategory();
        ArrayList<ClassificationItem> classification = ((MainActivity) getActivity()).getClassification();
        Log.i("分类碎片", "classifcation size:" + classification.size() + " infos size: " + imgInfos.size());
        for (ClassificationItem item : classification) {
            if (record.get("" + item.getCatId()) != null) {
                int pos = record.get("" + item.getCatId());
                result.get(pos).getImgInfos().add(imgInfos.get(item.getImgId()));
            } else {
                record.put("" + item.getCatId(), result.size());
                ArrayList<ImgInfo> tempInfoList = new ArrayList<>();
                tempInfoList.add(imgInfos.get(item.getImgId()));
                result.add(new ImageCategoryEntry(category.get(item.getCatId()), tempInfoList));
            }
        }
        return result;
    }
}
