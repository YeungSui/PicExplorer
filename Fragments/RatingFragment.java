package com.pic.stage.picexplorer.Fragments;

import com.pic.stage.picexplorer.ImageCategoryEntry;
import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class RatingFragment extends SortDisplayFragment {
    @Override
    public ArrayList<ImageCategoryEntry> getData() {
        HashMap<String, Integer> record = new HashMap<>();
        HashMap<Integer, ImgInfo> infoMap = ((MainActivity) getActivity()).getThumbnailsInfoMap();
        Iterator it = infoMap.entrySet().iterator();
        ArrayList<ImageCategoryEntry> result = new ArrayList<>();
        while (it.hasNext()) {
            int id = (int) ((HashMap.Entry) it.next()).getKey();
            ImgInfo info = infoMap.get(id);
            if (record.get("" + info.getRating()) != null) {
                result.get(record.get("" + info.getRating())).getImgInfos().add(info);
            } else {
                record.put("" + info.getRating(), result.size());
                ArrayList<ImgInfo> tempInfoList = new ArrayList<ImgInfo>();
                tempInfoList.add(info);
                String title = "" + info.getRating() + "星";
                if (info.getRating() <= 0) {
                    title = "待评分";
                }
                result.add(new ImageCategoryEntry(title, tempInfoList));
            }
        }
        Collections.sort(result, new Comparator<ImageCategoryEntry>() {
            @Override
            public int compare(ImageCategoryEntry o1, ImageCategoryEntry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return result;
    }
}
