package com.pic.stage.picexplorer.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.pic.stage.picexplorer.Adapters.ThumbnailsAdapter;
import com.pic.stage.picexplorer.ImageDBHelper;
import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.MainActivity;
import com.pic.stage.picexplorer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

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
    private ArrayList<Bitmap> thumbnails = new ArrayList<>();
    private ArrayList<Integer> selectedThumbnails = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thumbnail, container, false);
        initView(view);
        initData();
        setAdapter();
        return view;
    }

    private void initView(View view) {
        thumbnail_display = view.findViewById(R.id.gv_thumbnails_display);
    }

    private void initData() {
        thumbnailsInfo = ((MainActivity) getActivity()).getThumbnailsInfo();
        thumbnails = ((MainActivity) getActivity()).getThumbnails();
    }

    private void setAdapter() {
        ThumbnailsAdapter thumbnailsAdpt = new ThumbnailsAdapter(getActivity(), thumbnailsInfo, thumbnails, selectedThumbnails);
        thumbnail_display.setAdapter(thumbnailsAdpt);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 设置底部菜单监听
        LinearLayout bottom_menu = getActivity().findViewById(R.id.layout_thumbnail_menu);
        setBottomMenuListener(bottom_menu);
        // 设置缩略图单击事件监听
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
                    showSelectHint(parent, position);
                    showBottomMenu();
                }
                return true;
            }
        });
    }

    // 设置缩略图网格底部菜单监听器
    private void setBottomMenuListener(LinearLayout bottomMenu) {
        // 删除按钮监听
        Button btn = bottomMenu.findViewById(R.id.btn_thumbnail_delete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImages();
            }
        });
        // 评分按钮监听
        btn = bottomMenu.findViewById(R.id.btn_thumbnail_rate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateImage();
            }
        });
        // 分类按钮监听
        btn = bottomMenu.findViewById(R.id.btn_thumbnail_classify);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classifyImage();
            }
        });
        // 上传按钮监听
        btn = bottomMenu.findViewById(R.id.btn_thumbnail_upload);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        Log.i("Debug", "bottom menu listener set");
    }

    // 批量删除图片
    private void deleteImages() {
        Log.i("Menu Click", "delete image");
        GridView gv = getActivity().findViewById(R.id.gv_thumbnails_display);
        ThumbnailsAdapter adapter = (ThumbnailsAdapter) gv.getAdapter();
        //遍历hashmap，对选中的图片进行删除操作
        Collections.sort(selectedThumbnails);
        Iterator iter = selectedThumbnails.iterator();
        Iterator info_iter = thumbnailsInfo.iterator();
        Iterator thumbnails_iter = thumbnails.iterator();
        int before = -1;
        while (iter.hasNext()) {
            int dis = 0;
            if (before < 0) {
                before = (int) iter.next();
                dis = before;
            } else {
                // 待删除项目与上一个删除的项目的距离-1，因为删除时要进行一次next()
                int temp = (int) iter.next();
                dis = temp - before - 1;
                before = temp;
            }
            Log.i("before", "" + before);
            Log.i("Distance", "" + dis);
            // 将迭代器迭代到待删除项目前一位
            while (dis > 0) {
                info_iter.next();
                thumbnails_iter.next();
                dis--;
            }
            // 删除
            deleteOneThumbnail(info_iter, thumbnails_iter);
        }
        // 清空选中列表
        selectedThumbnails.clear();
        // 通知适配器更新
        ((ThumbnailsAdapter) ((GridView) getActivity().findViewById(R.id.gv_thumbnails_display)).getAdapter()).notifyDataSetChanged();
        getActivity().onBackPressed();
    }

    // 批量删除图片时单独删除一张的操作
    private void deleteOneThumbnail(Iterator info_iter, Iterator thumbnails_iter) {
        // 先删除文件
        ImgInfo imgInfo = (ImgInfo) info_iter.next(); // 出现next
        File file = new File(imgInfo.getPath());
        file.delete();
        // 再删除mediastore的记录
        ContentResolver con = getActivity().getContentResolver();
        String where = MediaStore.Images.Media.DATA + " ='" + imgInfo.getPath() + "'";
        con.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
        // 删除数据库记录
        ((MainActivity) getActivity()).getImageDBHelper().getWritableDatabase().delete("image_info", MediaStore.Images.Media._ID + "=?", new String[]{imgInfo.getId()});
        // 最后删除网格的缩略图的数据，包括选中列表、缩略图信息和缩略图
        info_iter.remove();
        thumbnails_iter.next(); // 出现next
        thumbnails_iter.remove();
    }

    // 评分
    // 写入数据库
    private void rateImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("评分");
        String ratings[] = {"1", "2", "3", "4", "5"};
        RatingDialogListener dialogMenuListener = new RatingDialogListener();
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
        Iterator it = selectedThumbnails.iterator();
        ImageDBHelper imageDBHelper = ((MainActivity) getActivity()).getImageDBHelper();
        SQLiteDatabase db = imageDBHelper.getWritableDatabase();
        while (it.hasNext()) {
            int pos = (int) it.next();
            ContentValues values = new ContentValues();
            values.put("rating", rating);
            try {
                db.update(imageDBHelper.IMAGE_INFO, values, "_id=?", new String[]{thumbnailsInfo.get(pos).getId()});
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            thumbnailsInfo.get(pos).setRating(rating);
            getActivity().onBackPressed();
        }
        return true;
    }

    // 自定分类
    // 先从数据库获取所有分类
    // 弹出选项，用户选择已有的分类，提交后写入图片分类表
    // 新建分类，提交后写入分类表和图片分类表
    private void classifyImage() {
        Log.i("Menu Click", "classify image");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        HashMap<Integer, String> category = new HashMap<>();
        ArrayList<Integer> category_ids = new ArrayList<>();
        getCategory(category, category_ids);
        // 获取分类名
        String[] nameList = new String[category_ids.size()];
        for (int i = 0; i < category_ids.size(); i++) {
            nameList[i] = category.get(category_ids.get(i));
        }
        // 设置多选框，用户可选择多个已有类别进行分类，或者新建类别，新建类别只能新建一个进行分类
        ClassifyMultiChoiceListener classifyMultiChoiceListener = new ClassifyMultiChoiceListener(category_ids);
        ArrayList<Integer> selectedIds = classifyMultiChoiceListener.getSelectedIds();
        ClassifyButtonListener classifyButtonListener = new ClassifyButtonListener(selectedIds, category_ids);
        builder.setTitle("选择分类").setMultiChoiceItems(nameList, null, classifyMultiChoiceListener);
        builder.setPositiveButton("确认", classifyButtonListener);
        builder.setNeutralButton("新建", classifyButtonListener);
        builder.setNegativeButton("取消", classifyButtonListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 获取分类（分类id和分类名）
    private void getCategory(HashMap<Integer, String> cat, ArrayList<Integer> category_ids) {
        SQLiteDatabase db = ((MainActivity) getActivity()).getImageDBHelper().getWritableDatabase();
        String columns[] = {"category_id", "category_name"};
        Cursor cur = db.query(ImageDBHelper.CATEGORY, columns, null, null, null, null, null);
        while (cur.moveToNext()) {
            cat.put(cur.getInt(0), cur.getString(1));
            category_ids.add(cur.getInt(0));
        }
    }

    // 创建 新建分类 的对话框
    private void newCategoryDialog() {
        EditText et = new EditText(getActivity());
        // 对话框点击事件监听，et参数提供文本输入框的输入
        AddCategoryListener addCategoryListener = new AddCategoryListener(et);
        new AlertDialog.Builder(getActivity()).setTitle("新建分类")
                .setView(et)
                .setPositiveButton("确定", addCategoryListener)
                .setNegativeButton("取消", null)
                .show();
    }

    // 记录图片分类
    // 遍历选中图片，遍历选中分类，将图片id和分类id写入数据库
    private boolean doClassifying(ArrayList<Integer> selectedIds, ArrayList<Integer> categoryIds) {
        Log.i("doClassifying", "正在记录分类");
        SQLiteDatabase db = ((MainActivity) getActivity()).getImageDBHelper().getWritableDatabase();
        for (int pos : selectedThumbnails) {
            Log.i("doClassifying", "正在记录第" + pos + "张图片");
            for (int menuPos : selectedIds) {
                Log.i("doClassifying", "正在记录第" + menuPos + "个类别");
                ImgInfo imgInfo = thumbnailsInfo.get(pos);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media._ID, Integer.parseInt(imgInfo.getId()));
                values.put("category_id", categoryIds.get(menuPos));
                try {
                    db.insertOrThrow(ImageDBHelper.IMAGE_CLASSIFCATION, null, values);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    // 添加分类
    // 先向ImageDBHelper.Category添加分类
    // 获取新增的分类号
    // 遍历选中图片，向ImageDBHelper.IMAGE_CLASSIFICATION添加图片分类信息
    private void addCategory(String input) {
        SQLiteDatabase db = ((MainActivity) getActivity()).getImageDBHelper().getWritableDatabase();
        // 先向ImageDBHelper.Category添加分类
        ContentValues values = new ContentValues();
        values.put("category_name", input);
        Long insertRow = db.insertOrThrow(ImageDBHelper.CATEGORY, null, values);
        // 获取新增分类的编号
        Cursor cur = db.query(ImageDBHelper.CATEGORY, new String[]{"category_id"}, "category_name=?", new String[]{input}, null, null, null);
        int newCatId = -1;
        while (cur.moveToNext()) {
            newCatId = cur.getInt(0);
        }
        for (int pos : selectedThumbnails) {
            values = new ContentValues();
            values.put(MediaStore.Images.Media._ID, Integer.parseInt(thumbnailsInfo.get(pos).getId()));
            values.put("category_id", newCatId);
            try {
                db.insertOrThrow(ImageDBHelper.IMAGE_CLASSIFCATION, null, values);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("function addCategory", "选中图片添加新增分类出错");
                Log.e("Variable", "" + newCatId);
                Log.e("Variable", thumbnailsInfo.get(pos).getId());
            }
            Log.i("addCategory", "第" + pos + "张图片分类已记录");
        }
    }

    // 上传操作
    private void uploadImage() {
        Log.i("Menu Click", "upload image");

    }

    // 缩略图单击监听
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
            if (selectedThumbnails.contains(pos)) {
                int tmp = selectedThumbnails.indexOf(pos);
                selectedThumbnails.remove(tmp);
            } else {
                selectedThumbnails.add(pos);
            }
            adapter.notifyDataSetChanged();
            Iterator it = selectedThumbnails.iterator();
            String msg = "";
            while (it.hasNext()) {
                int a = (int) it.next();
                msg += a + "|";
            }
            Log.i("Selected Thumbnails", msg);
        }
    }

    // 长按监听细节
    // 长按显示网格子项的帧布局，显示右上角的 未 选择图案
    // actionbar显示全选选项，显示context菜单（删除，上传，评分，分类）
    private void showSelectHint(AdapterView<?> parent, int pos) {
        GridView gridView = (GridView) parent;
        ThumbnailsAdapter adapter = (ThumbnailsAdapter) gridView.getAdapter();
        adapter.setSelectHint(true);
        selectedThumbnails.add(pos);
        adapter.notifyDataSetChanged();
    }

    // 长按以后显示底部菜单
    // 隐藏activity的底部选项，显示本fragment的底部布局
    private void showBottomMenu() {
        Log.i("Debug", "show bottom menu");
        RadioGroup navigation = getActivity().findViewById(R.id.rg_radio_navigation);
        navigation.setVisibility(View.GONE);
        LinearLayout menu = getActivity().findViewById(R.id.layout_thumbnail_menu);
        menu.setVisibility(View.VISIBLE);
    }

    // 删除缩略图的具体细节
    // 该服务供父activity调用
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

    // 评分对话框监听
    class RatingDialogListener implements DialogInterface.OnClickListener {
        public final static int DEFAULT_CHECKED = 2;
        private int selectedId = DEFAULT_CHECKED;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which >= 0) {
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

    // 分类对话框多选按钮监听
    class ClassifyMultiChoiceListener implements DialogInterface.OnMultiChoiceClickListener {
        private ArrayList<Integer> selectedIds = new ArrayList<>();
        private ArrayList<Integer> categoryIds;

        public ClassifyMultiChoiceListener(ArrayList<Integer> ids) {
            categoryIds = ids;
        }

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            if (which >= 0) {
                if (isChecked) {
                    Log.i("选中了", "" + which);
                    selectedIds.add(which);
                } else {
                    Log.i("取消了", "" + which);
                    selectedIds.remove(selectedIds.indexOf(which));
                }
            }
        }

        public ArrayList<Integer> getSelectedIds() {
            return selectedIds;
        }
    }

    // 分类对话框底部确认、取消等按钮的监听
    class ClassifyButtonListener implements DialogInterface.OnClickListener {
        private ArrayList<Integer> selectedIds;
        private ArrayList<Integer> categoryIds;

        public ClassifyButtonListener(ArrayList<Integer> mids, ArrayList<Integer> mcatIds) {
            selectedIds = mids;
            categoryIds = mcatIds;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.dismiss();
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                if (selectedIds == null || selectedIds.size() == 0) {
                    dialog.dismiss();
                    return;
                }
                boolean success = doClassifying(selectedIds, categoryIds);
                Toast.makeText(getActivity(), success ? "分类成功" : "有重复分类-_-!", Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                newCategoryDialog();
            }
        }
    }

    // 添加分类 对话框点击监听
    class AddCategoryListener implements DialogInterface.OnClickListener {
        private EditText et;

        public AddCategoryListener(EditText met) {
            et = met;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                String input = et.getText().toString();
                if (input.equals("")) {
                    Toast.makeText(getActivity(), "内容不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    addCategory(input);
                    getActivity().onBackPressed();
                    Toast.makeText(getActivity().getApplicationContext(), "添加分类成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                getActivity().onBackPressed();
            }
        }
    }

}
