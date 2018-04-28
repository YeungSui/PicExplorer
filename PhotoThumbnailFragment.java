package com.pic.stage.picexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
This is a fragment used to display thumbnails on the device.
 */
public class PhotoThumbnailFragment extends Fragment {
    private static final String[] IMAGE_INFO = {
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
    private GridView thumbnail_display;
    private ArrayList<ImgInfo> thumbnailsInfo = new ArrayList<ImgInfo>();
    private Cursor cur;
    private ArrayList<Bitmap> thumbnails = new ArrayList<Bitmap>();
    private HashMap<Integer, Boolean> selectedThumbnails = new HashMap<Integer, Boolean>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thumbnail, container, false);
        initView(view);
        initData();
        //getThumbnails();
        setAdapter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gv = view.findViewById(R.id.gv_thumbnails_display);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                onThumbnailClick((GridView) parent, pos);
            }
        });
        // 长按监听
        // 细节在showSelectHint
        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ThumbnailsAdapter adapter = (ThumbnailsAdapter) ((GridView) parent).getAdapter();
                if (!adapter.getSelectHint()) {
                    showSelectHint(parent, view, position);
                    showBottomMenu((GridView) parent, adapter, position);
                }
                return false;
            }
        });
    }

    public void initView(View view) {
        thumbnail_display = (GridView) view.findViewById(R.id.gv_thumbnails_display);
    }

    private void initData() {
        thumbnailsInfo = ((MainActivity) getActivity()).getThumbnailsInfo();
        thumbnails = ((MainActivity) getActivity()).getThumbnails();
    }

    public void setAdapter() {
        ThumbnailsAdapter thumbnailsAdpt = new ThumbnailsAdapter(getActivity(), thumbnailsInfo, thumbnails, selectedThumbnails);
        thumbnail_display.setAdapter(thumbnailsAdpt);
    }

    /*
     * Get all thumbnails on the device
     */

    private void getThumbnails() {
        ContentResolver cont = getActivity().getContentResolver();
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
    }

    // 长按监听细节
    // 长按显示网格子项的帧布局，显示右上角的 未 选择图案
    // actionbar显示全选选项，显示context菜单（删除，上传，评分，分类）
    private void showSelectHint(AdapterView<?> parent, View view, int pos) {
        GridView gridView = (GridView) parent;
        ThumbnailsAdapter adapter = (ThumbnailsAdapter) gridView.getAdapter();
        adapter.setSelectHint(true);
        selectedThumbnails.put(pos, false);
        adapter.notifyDataSetChanged();
    }

    // 删除缩略图的具体细节
    // 删除缩略图列表和缩略图信息列表
    // 通知适配器刷新
    public void onDeleteImage(ImgInfo imgInfo) {
        int pos = -1;
        //找出要删除的图片在网格的位置
        for (int i = 0; i < thumbnailsInfo.size(); i++) {
            if (imgInfo.getId() == thumbnailsInfo.get(i).getId()) {
                pos = i;
                break;
            }
        }
        // 如果找到该图片的位置就删除链表中的缩略图和缩略图信息
        // 通知适配器更新
        if (pos > -1) {
            thumbnails.remove(pos);
            thumbnailsInfo.remove(pos);
            ThumbnailsAdapter adapter = (ThumbnailsAdapter) thumbnail_display.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    private void onThumbnailClick(GridView gridView, int pos) {
        // 设置PhotoThumbnailFragment中的gridview的子项的监听器
        // 点击子fragment的网格子项将会：
        // 隐藏底部按钮（恢复主页时需要注意）
        // 显示imageview的父容器（相对布局，恢复时要注意）
        // 显示图片原图（覆盖网格的缩略图，这在恢复中不必考虑）
        ThumbnailsAdapter adapter = (ThumbnailsAdapter) gridView.getAdapter();
        // 网格缩略图非选中状态
        if (!adapter.getSelectHint()) {
            ImgInfo imgInfo = thumbnailsInfo.get(pos);
            Activity activity = getActivity();
            ImageView iv = activity.findViewById(R.id.iv_image_display);
            activity.findViewById(R.id.rg_radio_navigation).setVisibility(View.GONE);
            activity.findViewById(R.id.image_display_layout).setVisibility(View.VISIBLE);
            Log.i("Operation", "fragment removed");
            Bitmap bitmap = BitmapFactory.decodeFile(imgInfo.getPath());
            iv.setImageBitmap(bitmap);
            iv.setVisibility(View.VISIBLE);
            iv.setTag(imgInfo);
        }
        // 网格缩略图选中状态
        // 判断缩略图是否被选中
        // 选中则单击标记未选中状态，反之标记为选中状态
        else {
            if (selectedThumbnails.get(pos) != null) {
                selectedThumbnails.remove(pos);
            } else {
                selectedThumbnails.put(pos, false);
            }
            adapter.notifyDataSetChanged();
            Iterator it = selectedThumbnails.entrySet().iterator();
            String msg = "";
            while (it.hasNext()) {
                int a = (int) ((Map.Entry) it.next()).getKey();
                msg += a + "|";
            }
            Log.i("Selected Thumbnails", msg);
        }
    }

    // 长按以后显示底部菜单
    // 隐藏activity的底部选项，显示本fragment的底部布局
    // 为菜单按钮设置监听
    private void showBottomMenu(GridView gridView, ThumbnailsAdapter adapter, int pos) {
        RadioGroup rg = getActivity().findViewById(R.id.rg_radio_navigation);
        rg.setVisibility(View.GONE);
        RadioGroup radioGroup = getActivity().findViewById(R.id.rg_thumbnail_menu);
        radioGroup.setVisibility(View.VISIBLE);
        setBottomMenuListener(radioGroup);
    }

    // 设置缩略图网格底部菜单监听器
    private void setBottomMenuListener(RadioGroup rg) {
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_thumbnail_delete:
                        deleteImage();
                        break;
                    case R.id.btn_thumbnail_upload:
                        uploadImage();
                        break;
                    case R.id.btn_thumbnail_classify:
                        classifyImage();
                        break;
                    case R.id.btn_thumbnail_rate:
                        rateImage();
                        break;
                }
            }
        });
    }

    private void deleteImage() {
        Log.i("Menu Click", "delete image");
        GridView gv = getActivity().findViewById(R.id.gv_thumbnails_display);
        ThumbnailsAdapter adapter = (ThumbnailsAdapter) gv.getAdapter();
        //遍历hashmap，对选中的图片进行删除操作
        Iterator iter = selectedThumbnails.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            int pos = (int) entry.getKey();
            deleteOneThumbnail(pos);
        }
        // 清空选中列表
        selectedThumbnails.clear();
        // 通知适配器更新
        ((ThumbnailsAdapter) ((GridView) getActivity().findViewById(R.id.gv_thumbnails_display)).getAdapter()).notifyDataSetChanged();
        getActivity().onBackPressed();
    }

    private void deleteOneThumbnail(int pos) {
        // 先删除文件
        ImgInfo imgInfo = thumbnailsInfo.get(pos);
        File file = new File(imgInfo.getPath());
        if (file != null) {
            file.delete();
        }
        // 再删除mediastore的记录
        ContentResolver con = getActivity().getContentResolver();
        String where = MediaStore.Images.Media.DATA + " ='" + imgInfo.getPath() + "'";
        con.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
        // 最后删除网格的缩略图的数据，包括选中列表、缩略图信息和缩略图
        thumbnailsInfo.remove(pos);
        thumbnails.remove(pos);
    }

    private void uploadImage() {
        Log.i("Menu Click", "upload image");
    }

    private void classifyImage() {
        Log.i("Menu Click", "classify image");
    }

    // 评分
    // 写入数据库
    private void rateImage() {
        Log.i("Menu Click", "rate image");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("评分");

        Iterator it = selectedThumbnails.entrySet().iterator();
        while (it.hasNext()) {
            int pos = (int) ((HashMap.Entry) it.next()).getKey();
            ContentValues con = new ContentValues();
            con.put(MediaStore.Images.Media._ID, thumbnailsInfo.get(pos).getId());

            ImageDBHelper imageDBHelper = ((MainActivity) getActivity()).getImageDBHelper();
            SQLiteDatabase db = imageDBHelper.getWritableDatabase();
            //db.replace(imageDBHelper.IMAGE_RATING, null, )
        }
    }
}
