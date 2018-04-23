package com.shawnlee.iflytekasr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Shawn.Lee on 2018/4/23.
 */

class MyListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<String> list;

    public MyListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setList(List<String> list){
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {

        return position;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.item_voicelist, null);
        TextView tv = convertView.findViewById(R.id.tv_armName);
        tv.setText(list.get(position));
        return convertView;
    }
}
