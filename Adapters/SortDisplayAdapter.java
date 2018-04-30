package com.pic.stage.picexplorer.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pic.stage.picexplorer.ImageCategoryEntry;
import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.MainActivity;
import com.pic.stage.picexplorer.R;

import java.util.ArrayList;

public class SortDisplayAdapter extends BaseAdapter {
    private ArrayList<ImageCategoryEntry> categories;
    private Context mContext;
    private LayoutInflater inflater;
    private ArrayList<Integer> selectedIds;

    public SortDisplayAdapter(Context context, ArrayList<ImageCategoryEntry> cats, ArrayList<Integer> mSelectedIds) {
        inflater = LayoutInflater.from(context);
        mContext = context;
        categories = cats;
        selectedIds = mSelectedIds;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (categories.size() == 0) {
            return convertView;
        }
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sort_display_item, null);
            holder = new ViewHolder();
            holder.tv = convertView.findViewById(R.id.tv_sort_display_title);
            holder.gv = convertView.findViewById(R.id.gv_sort_display_thumbnails);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ArrayList<Bitmap> thumbnails = getThumbnails(categories.get(position).getImgInfos());
        holder.gv.setAdapter(new ThumbnailsAdapter(mContext, categories.get(position).getImgInfos(), thumbnails, selectedIds));
        holder.gv.setOnItemClickListener(new OnThumbnailClickListener());
        holder.tv.setText(categories.get(position).getName());
        //holder.tv.setTextColor(ColorStateList.valueOf(0));
        Log.i("分类名称", categories.get(position).getName());
        return convertView;
    }

    private ArrayList<Bitmap> getThumbnails(ArrayList<ImgInfo> infos) {
        ArrayList<Bitmap> result = new ArrayList<>();
        ContentResolver cont = mContext.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        for (ImgInfo info : infos) {
            result.add(MediaStore.Images.Thumbnails.getThumbnail(cont, Long.parseLong(info.getId()), MediaStore.Images.Thumbnails.MICRO_KIND, options));
        }
        return result;
    }

    private class ViewHolder {
        public TextView tv;
        public GridView gv;
    }

    private class OnThumbnailClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ImageView tiv = view.findViewById(R.id.iv_thumbnail);
            ImgInfo imgInfo = (ImgInfo) tiv.getTag();
            int imgId = Integer.parseInt(imgInfo.getId());
            Log.i("时间碎片", "id为" + imgId);
            ImageView iiv = ((MainActivity) mContext).findViewById(R.id.iv_image_display);
            RelativeLayout rl = ((MainActivity) mContext).findViewById(R.id.image_display_layout);
            rl.setVisibility(View.VISIBLE);
            Button btn = ((MainActivity) mContext).findViewById(R.id.btn_delete_image);
            RadioGroup rg = ((MainActivity) mContext).findViewById(R.id.rg_radio_navigation);
            rg.setVisibility(View.GONE);
            ContentResolver cr = mContext.getContentResolver();
            Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, "" + MediaStore.Images.Media._ID + "=?", new String[]{"" + imgId}, null);
            String path = "";
            while (cur.moveToNext()) {
                path = cur.getString(0);
            }
            iiv.setImageBitmap(BitmapFactory.decodeFile(path));
            int x = -1, y = -1;
            for (int i = 0; i < categories.size(); i++) {
                for (int j = 0; j < categories.get(i).getImgInfos().size(); j++) {
                    if (categories.get(i).getImgInfos().get(j).getPath().equals(path)) {
                        x = i;
                        y = j;
                        break;
                    }
                }
                if (x >= 0) break;
            }
            iiv.setTag(categories.get(x).getImgInfos().get(y));
            iiv.setVisibility(View.VISIBLE);
        }
    }
}
