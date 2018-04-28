package com.pic.stage.picexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

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
    private ArrayList<ImgInfo> thumbnailsInfo = new ArrayList<>();
    private Cursor cur;
    private ArrayList<Bitmap> thumbnails = new ArrayList<>();
    private HashMap<Integer, Boolean> selectedThumbnails = new HashMap<>();

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
        LinearLayout bottom_menu = getActivity().findViewById(R.id.layout_thumbnail_menu);
        setBottomMenuListener(bottom_menu);
        GridView gv = view.findViewById(R.id.gv_thumbnails_display);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                onThumbnailClick((GridView) parent, pos);
            }
        });
        // 长按监听
        // 显示右上角标记
        // 显示底部菜单
        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ThumbnailsAdapter adapter = (ThumbnailsAdapter) ((GridView) parent).getAdapter();
                if (!adapter.getSelectHint()) {
                    showSelectHint(parent, view, position);
                    showBottomMenu((GridView) parent, adapter, position);
                }
                return true;
            }
        });
    }

    public void initView(View view) {
        thumbnail_display = view.findViewById(R.id.gv_thumbnails_display);
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
            if (imgInfo.getId().equals(thumbnailsInfo.get(i).getId())) {
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
        Log.i("Debug", "show bottom menu");
        RadioGroup navigation = getActivity().findViewById(R.id.rg_radio_navigation);
        navigation.setVisibility(View.GONE);
        LinearLayout menu = getActivity().findViewById(R.id.layout_thumbnail_menu);
        menu.setVisibility(View.VISIBLE);
    }

    // 设置缩略图网格底部菜单监听器
    private void setBottomMenuListener(LinearLayout bottomMenu) {
        Button btn = bottomMenu.findViewById(R.id.btn_thumbnail_delete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImages();
            }
        });
        btn = bottomMenu.findViewById(R.id.btn_thumbnail_rate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateImage();
            }
        });
        btn = bottomMenu.findViewById(R.id.btn_thumbnail_classify);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classifyImage();
            }
        });
        btn = bottomMenu.findViewById(R.id.btn_thumbnail_upload);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        Log.i("Debug", "bottom menu listener set");
    }

    private void deleteImages() {
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
        file.delete();
        // 再删除mediastore的记录
        ContentResolver con = getActivity().getContentResolver();
        String where = MediaStore.Images.Media.DATA + " ='" + imgInfo.getPath() + "'";
        con.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
        // 删除数据库记录
        ((MainActivity) getActivity()).getImageDBHelper().getWritableDatabase().delete("image_info", MediaStore.Images.Media._ID + "=?", new String[]{imgInfo.getId()});
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("评分");
        String ratings[] = {"1", "2", "3", "4", "5"};
        DialogMenuListener dialogMenuListener = new DialogMenuListener();
        builder.setSingleChoiceItems(ratings, dialogMenuListener.DEFAULT_CHECKED, dialogMenuListener);
        String positive = "确认";
        builder.setPositiveButton(positive, dialogMenuListener);
        String negative = "取消";
        builder.setNegativeButton(negative, dialogMenuListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean doRating(int rating) {
        Log.i("Get Rating", "Rating is " + rating);
        Iterator it = selectedThumbnails.entrySet().iterator();
        while (it.hasNext()) {
            int pos = (int) ((HashMap.Entry) it.next()).getKey();
            ContentValues values = new ContentValues();
            values.put("rating", rating);
            try {
                ImageDBHelper imageDBHelper = ((MainActivity) getActivity()).getImageDBHelper();
                SQLiteDatabase db = imageDBHelper.getWritableDatabase();
                db.update(imageDBHelper.IMAGE_INFO, values, "_id=?", new String[]{thumbnailsInfo.get(pos).getId()});
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    class DialogMenuListener implements DialogInterface.OnClickListener {
        public final static int DEFAULT_CHECKED = 2;
        private int selectedId = DEFAULT_CHECKED;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which > 0) {
                selectedId = which;
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                boolean success = doRating(selectedId + 1);
                Toast.makeText(getActivity(), (success ? "评分已记录" : "评分记录出错了-_-!"), Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
                dialog.dismiss();
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            }
        }
    }
}
