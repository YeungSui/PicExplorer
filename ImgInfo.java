package com.pic.stage.picexplorer;

public class ImgInfo {
    private String path;
    private String id;
    private String dirId;
    private String dir;
    private String longitude;
    private String latitude;
    private String takenTime;
    private String addedTime;
    private String name;
    private int rating = -1;

    public ImgInfo(String mname, String mpath, String mid, String mdir_id, String mdir, String mlongitude, String mlatitude, String mtakenTime, String maddedTime) {
        path = mpath;
        id = mid;
        dirId = mdir_id;
        dir = mdir;
        longitude = mlongitude;
        latitude = mlatitude;
        takenTime = mtakenTime;
        addedTime = maddedTime;
        name = mname;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public String getDirId() {
        return dirId;
    }

    public String getDir() {
        return dir;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getTakenTime() {
        return takenTime;
    }

    public String getAddedTime() {
        return addedTime;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }
}
