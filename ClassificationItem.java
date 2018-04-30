package com.pic.stage.picexplorer;

public class ClassificationItem {
    private int imgId;
    private int catId;

    public ClassificationItem(int iid, int cid) {
        imgId = iid;
        catId = cid;
    }

    public int getImgId() {
        return imgId;
    }

    public int getCatId() {
        return catId;
    }
}
