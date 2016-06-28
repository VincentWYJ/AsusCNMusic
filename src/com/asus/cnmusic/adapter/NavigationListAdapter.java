package com.asus.cnmusic.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import com.asus.cnmusic.info.NavigationListBean;
import com.asus.cnmusic.R;

/**
 * Created by Jun on 2015/3/25.
 *
 */
public class NavigationListAdapter extends SimpleBaseAdapter<NavigationListBean> {

    private int selectedPosition = -1;

    public NavigationListAdapter(Context context, int layoutId, List<NavigationListBean> data) {
        super(context, layoutId, data);
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    @Override
    public void getItemView(ViewHolder holder, NavigationListBean drawerListBean) {
        holder.setText(R.id.navigation_title,drawerListBean.getTitle())
                .setImageResource(R.id.navigation_image,drawerListBean.getTitleImageId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = ViewHolder.get(mContext, convertView, parent,
                layoutId, position);

        getItemView(holder,mData.get(position));

        if(selectedPosition == position || (selectedPosition == -1 && position == 0)) {
            holder.getView(R.id.navigation_title).setSelected(true);
            holder.getView(R.id.navigation_image).setSelected(true);
        }else {
            holder.getView(R.id.navigation_title).setSelected(false);
            holder.getView(R.id.navigation_image).setSelected(false);
            holder.getView(R.id.navigation_list).setBackgroundColor(Color.TRANSPARENT);
        }

        return holder.getConvertView();
    }


}
