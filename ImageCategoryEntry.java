package com.pic.stage.picexplorer;

import java.util.ArrayList;

public class ImageCategoryEntry {
    private String name;
    private ArrayList<ImgInfo> imgInfos = new ArrayList<>();

    public ImageCategoryEntry(String s, ArrayList<ImgInfo> infos) {
        name = s;
        imgInfos = infos;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ImgInfo> getImgInfos() {
        return imgInfos;
    }

    public void addImgInfo(ImgInfo imgInfo) {
        imgInfos.add(imgInfo);
    }
}
