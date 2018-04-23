package com.shawnlee.iflytekasr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Shawn.Lee on 2018/4/23.
 * 语音列表适配器
 */

class MyListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<String> list;

    public MyListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        list = new ArrayList<>();
    }

    public ArrayList getList(){
        return list;
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
