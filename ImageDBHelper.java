package com.pic.stage.picexplorer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ImageDBHelper extends SQLiteOpenHelper {
    public final static String IMAGE_INFO = "image_info";
    public final static String IMAGE_RATING = "image_rating";
    public final static String IMAGE_CLASSIFCATION = "image_classification";
    public final static String CATEGORY = "category";


    public ImageDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, 1);
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
                "date_added INTEGER)";
        db.execSQL(create_image_info);

        String create_image_rating = "CREATE TABLE image_rating(_id INTEGER PRIMARY KEY,rating INTEGER)";
        db.execSQL(create_image_rating);

        String create_image_classification = "CREATE TABLE image_classification(_id INTEGER PRIMARY KEY,category VARCHAR(255))";
        db.execSQL(create_image_classification);

        String create_category = "CREATE TABLE category(category_id INTEGER PRIMARY KEY AUTOINCREMENT,category_name VARCHAR(255))";
        db.execSQL(create_category);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
