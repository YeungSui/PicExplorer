package com.pic.stage.picexplorer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ImageDBHelper extends SQLiteOpenHelper {
    public final static String IMAGE_INFO = "image_info";
    public final static String IMAGE_INFO_TEMP = "image_info_temp";
    public final static String IMAGE_CLASSIFCATION = "image_classification";
    public final static String CATEGORY = "category";


    public ImageDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_image_info = "CREATE TABLE image_info(" +
                "_id INTEGER PRIMARY KEY," +
                "_data VARCHAR(255), " +
                "_display_name VARCHAR(255), " +
                "bucket_id VARCHAR(255)," +
                "bucket_display_name VARCHAR(255), " +
                "longitude DOUBLE, " +
                "latitude DOUBLE, " +
                "datetaken INTEGER, " +
                "date_added INTEGER," +
                "rating INTEGER)";
        try {
            db.execSQL(create_image_info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String create_image_info_temp = "CREATE TABLE image_info_temp(" +
                "_id INTEGER PRIMARY KEY," +
                "_data VARCHAR(255), " +
                "_display_name VARCHAR(255), " +
                "bucket_id VARCHAR(255)," +
                "bucket_display_name VARCHAR(255), " +
                "longitude DOUBLE, " +
                "latitude DOUBLE, " +
                "datetaken INTEGER, " +
                "date_added INTEGER," +
                "rating INTEGER)";
        db.execSQL(create_image_info_temp);

        String create_category = "CREATE TABLE category(category_id INTEGER PRIMARY KEY AUTOINCREMENT,category_name VARCHAR(255))";
        db.execSQL(create_category);

        // 由于一张图片可能对应多个分类，只能将单独建一个表
        String create_image_classification = "CREATE TABLE image_classification(_id INTEGER NOT NULL REFERENCES image_info(_id) ON DELETE CASCADE,category_id INTEGER NOT NULL REFERENCES category(category_id) ON DELETE CASCADE)";
        try {
            db.execSQL(create_image_classification);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("ImageDBHelper Message", "creating a table with two foreign keys throws an exception");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
