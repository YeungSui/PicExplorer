package com.pic.stage.picexplorer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String[] permList = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.WRITE_INTERNAL_STORAGE"
    };

    private RadioGroup mRadioGroup;
    private Fragment[] mFragments;
    private ImageDBHelper imageDBHelper;
    private FrameLayout mLayout;
    private ArrayList<ImgInfo> thumbnailsInfo = new ArrayList<ImgInfo>();
    private ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();
    private LocalBroadcastManager localBroadcastManager;
    private DBUpdateBroadcastReciever dbUpdateBroadcastReciever;
    private IntentFilter notifyThumbnailDisplayIntent = new IntentFilter("NOTIFY_GV_THUMBNAILS_DISPLAY");

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(dbUpdateBroadcastReciever);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        initView();
        initFragments();
        setListener();
        if (!checkPermission()) {
            finish();
            setResult(RESULT_OK);
        }
        imageDBHelper = new ImageDBHelper(this, "image.db", null, 1);
        dbUpdateBroadcastReciever = new DBUpdateBroadcastReciever();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(dbUpdateBroadcastReciever, notifyThumbnailDisplayIntent);
        DBUpdateThread runable = new DBUpdateThread();
        Thread thread = new Thread(runable, "dbUpdateThread");
        thread.start();
    }

    private boolean checkPermission() {
        for (String perm : permList) {
            int permState = getApplicationContext().checkCallingOrSelfPermission(perm);
            Log.i("permission state ", "" + permState);
            return permState == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void initView() {
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_radio_navigation);
        mLayout = (FrameLayout) findViewById(R.id.fl_radio_show);
    }

    private void initFragments() {
        mFragments = new Fragment[4];
        // initialize all fragments;
        mFragments[0] = new HomepageFragment();
        mFragments[1] = new SubscriptionFragment();
        mFragments[2] = new DiscoveryFragment();
        mFragments[3] = new ProfileFragment();
        //get a manager
        FragmentManager fragManager = getSupportFragmentManager();
        // get a transaction
        FragmentTransaction transaction = fragManager.beginTransaction();
        //set HomepageFragment as default
        //replace blank framelayout with HomepageFragment
        transaction.replace(R.id.fl_radio_show, mFragments[0]);
        //commit a transaction
        transaction.commit();
        //set checked state as default in homepage
        mRadioGroup.check(R.id.rb_radio_homepage);
    }

    private void setListener() {
        mRadioGroup.setOnCheckedChangeListener(this);
        // 设置image view监听器
        // 若image view是visible，单击会显示底部选项,反之隐藏
        findViewById(R.id.iv_image_display).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.iv_image_display).getVisibility() == View.VISIBLE) {
                    if (findViewById(R.id.display_options).getVisibility() == View.GONE) {
                        findViewById(R.id.display_options).setVisibility(View.VISIBLE);
                        Log.i("event", "show options");
                    } else {
                        findViewById(R.id.display_options).setVisibility(View.GONE);
                    }
                }
            }
        });
        // 设置删除按钮监听器
        // 删除图片文件同时删除媒体库记录（注意先设置imageview内容）
        // 删除gridview子项（被删除的缩略图）
        // 调用返回键返回到主页
        findViewById(R.id.btn_delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView iv = (ImageView) findViewById(R.id.iv_image_display);
                ImgInfo imgInfo = (ImgInfo) iv.getTag();
                iv.setImageBitmap(null);
                onBackPressed();
                File file = new File(imgInfo.getPath());
                file.delete();
                ContentResolver con = getContentResolver();
                String where = MediaStore.Images.Media.DATA + " ='" + imgInfo.getPath() + "'";
                con.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                HomepageFragment fragment = (HomepageFragment) mFragments[0];
                Log.i("event", (fragment == null ? "" : "not ") + "null");
                fragment.getPhotoThumbnailFragment().onDeleteImage(imgInfo);
                Log.i("fragment no.", "" + fragment.getId());
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup rg, @IdRes int checkedId) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        switch (checkedId) {
            case R.id.rb_radio_homepage:
                transaction.replace(R.id.fl_radio_show, mFragments[0]);
                break;
            case R.id.rb_radio_subscription:
                transaction.replace(R.id.fl_radio_show, mFragments[1]);
            case R.id.rb_radio_discovery:
                transaction.replace(R.id.fl_radio_show, mFragments[2]);
            case R.id.rb_radio_profile:
                transaction.replace(R.id.fl_radio_show, mFragments[3]);
        }
        transaction.commit();
    }

    // 返回键监听
    // 隐藏imageview，底部选项，及两者的父容器
    // 恢复底部RadioGroup
    @Override
    public void onBackPressed() {
        ImageView iv = findViewById(R.id.iv_image_display);
        HomepageFragment homepage = (HomepageFragment) mFragments[0];
        GridView gv = findViewById(R.id.gv_thumbnails_display);
        ThumbnailsAdapter adapter = (ThumbnailsAdapter) gv.getAdapter();
        if (iv.getVisibility() == View.VISIBLE) {
            iv.setImageBitmap(null);
            iv.setVisibility(View.GONE);
            findViewById(R.id.display_options).setVisibility(View.GONE);
            findViewById(R.id.image_display_layout).setVisibility(View.GONE);
            findViewById(R.id.rg_radio_navigation).setVisibility(View.VISIBLE);
        }
        // 缩略图被选中状态时的操作
        // 隐藏右上角标记
        // 清除选中缩略图的记录
        // 通知适配器更新视图
        // 隐藏底部菜单
        // 显示底部选项卡（主页的）
        else if (adapter.getSelectHint()) {
            adapter.setSelectHint(false);
            adapter.clearSelectedThumbnails();
            adapter.notifyDataSetChanged();
            // 隐藏底部菜单
            findViewById(R.id.rg_thumbnail_menu).setVisibility(View.GONE);
            // 显示底部选项卡
            findViewById(R.id.rg_radio_navigation).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    // 方便其他fragment使用数据库
    public ImageDBHelper getImageDBHelper() {
        return imageDBHelper;
    }

    public ArrayList<ImgInfo> getThumbnailsInfo() {
        return thumbnailsInfo;
    }

    public ArrayList<Bitmap> getThumbnails() {
        return thumbnails;
    }

    class DBUpdateThread implements Runnable {
        @Override
        public void run() {
            final String[] IMAGE_INFO = {
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.LONGITUDE,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED
            };
            ContentResolver cont = getContentResolver();
            Cursor cur = MediaStore.Images.Media.query(cont, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_INFO);
            thumbnailsInfo = new ArrayList<ImgInfo>();
            thumbnails = new ArrayList<Bitmap>();
            while (cur.moveToNext()) {
                String name = cur.getString(0);
                String path = cur.getString(1);
                String id = cur.getString(2);
                String dir_id = cur.getString(3);
                String dir = cur.getString(4);
                String longitude = cur.getString(5);
                String latitude = cur.getString(6);
                String takenTime = cur.getString(7);
                String addedTime = cur.getString(8);
                thumbnailsInfo.add(new ImgInfo(name, path, id, dir_id, dir, longitude, latitude, takenTime, addedTime));
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            for (ImgInfo imgInfo : thumbnailsInfo) {
                thumbnails.add(MediaStore.Images.Thumbnails.getThumbnail(cont, Long.parseLong(imgInfo.getId()), MediaStore.Images.Thumbnails.MICRO_KIND, options));
            }
            Log.i("Debug", "before broadcast");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent("NOTIFY_GV_THUMBNAILS_DISPLAY");
            LocalBroadcastManager lm = LocalBroadcastManager.getInstance(MainActivity.this);
            lm.sendBroadcast(intent);
            // 刷新数据库
            for (ImgInfo imgInfo : thumbnailsInfo) {
                SQLiteDatabase db = imageDBHelper.getWritableDatabase();
                ContentValues values1 = new ContentValues();
                values1.put(IMAGE_INFO[0], imgInfo.getName());
                values1.put(IMAGE_INFO[1], imgInfo.getPath());
                values1.put(IMAGE_INFO[2], Long.parseLong(imgInfo.getId()));
                values1.put(IMAGE_INFO[3], Long.parseLong(imgInfo.getDirId()));
                values1.put(IMAGE_INFO[4], imgInfo.getDir());
                if (imgInfo.getLongitude() != null) {
                    values1.put(IMAGE_INFO[5], Double.parseDouble(imgInfo.getLongitude()));
                    values1.put(IMAGE_INFO[6], Double.parseDouble(imgInfo.getLatitude()));
                }
                if (imgInfo.getTakenTime() != null) {
                    values1.put(IMAGE_INFO[7], Long.parseLong(imgInfo.getTakenTime()));
                }
                values1.put(IMAGE_INFO[8], Long.parseLong(imgInfo.getAddedTime()));
                db.replace("image_info", null, values1);
            }
        }

    }

    // 广播接收器
    class DBUpdateBroadcastReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context contextx, Intent intent) {
            ((ThumbnailsAdapter) ((GridView) (findViewById(R.id.gv_thumbnails_display))).getAdapter()).notifyDataSetChanged();
            Log.i("Debug", "receive success");
        }
    }
}
