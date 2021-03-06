package com.pic.stage.picexplorer.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pic.stage.picexplorer.ImgInfo;
import com.pic.stage.picexplorer.R;

import java.util.ArrayList;

public class ThumbnailsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<ImgInfo> thumbnailsInfo;
    private ArrayList<Bitmap> thumbnails;
    private ArrayList<Integer> selectedThumbnails;
    private boolean selectHint = false;

    public ThumbnailsAdapter(Context context, ArrayList<ImgInfo> mData, ArrayList<Bitmap> mThumbnails, ArrayList<Integer> mSelectedThumbnails) {
        this.inflater = LayoutInflater.from(context);
        thumbnailsInfo = mData;
        thumbnails = mThumbnails;
        selectedThumbnails = mSelectedThumbnails;
    }

    @Override
    public int getCount() {
        if (thumbnailsInfo == null)
            return 0;
        return thumbnailsInfo.size() > thumbnails.size() ? thumbnails.size() : thumbnailsInfo.size();
    }

    @Override
    public Object getItem(int position) {
        if (thumbnailsInfo == null)
            return null;
        return thumbnailsInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (thumbnailsInfo.size() == 0) {
            return convertView;
        }
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.thumbnail_item, null);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.iv_thumbnail);
            holder.text = convertView.findViewById(R.id.tv_thumbnail_name);
            holder.selectHint = convertView.findViewById(R.id.iv_select_thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.selectHint.setVisibility((selectHint ? View.VISIBLE : View.GONE));
        // 网格在选择状态下而且当前缩略图被选中，将右上角标记置为选中
        if (selectHint && isThumbnailSelected(position)) {
            holder.selectHint.setImageResource(R.drawable.ic_selected_hint);
        }
        // 其他情况下，右上角设为未选中
        else {
            holder.selectHint.setImageResource(R.drawable.ic_unselected_hint);
        }
        holder.image.setImageBitmap(thumbnails.get(position));
        holder.image.setTag(thumbnailsInfo.get(position));
        holder.text.setText("");
        return convertView;
    }

    public boolean getSelectHint() {
        return selectHint;
    }

    public void setSelectHint(boolean s) {
        selectHint = s;
    }

    private boolean isThumbnailSelected(int pos) {
        if (selectedThumbnails.contains(pos)) {
            return true;
        }
        return false;
    }

    public void clearSelectedThumbnails() {
        selectedThumbnails.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView image;
        TextView text;
        ImageView selectHint;
    }
}
