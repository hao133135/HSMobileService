package com.qindor.hsmobileservice.Adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.qindor.hsmobileservice.R;

import java.util.List;

public class SellServiceAdpater  extends BaseAdapter {
    private int resource;   //item的布局
    private Context context;
    private List<String> strings;
    private LayoutInflater inflator;
    private int selectedItem = -1;

    public SellServiceAdpater(int resource, Context context, List<String> strings) {
        this.resource = resource;
        this.context = context;
        this.strings = strings;
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return strings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SellServiceAdpater.viewHolder viewHolder;
        if(convertView==null){
            inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(resource, null);
            viewHolder = new SellServiceAdpater.viewHolder();
            viewHolder.button = convertView.findViewById(R.id.service_btn);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (SellServiceAdpater.viewHolder) convertView.getTag();
        }
        String s = strings.get(position);
        viewHolder.button.setText(s);
        return convertView;
    }
    class viewHolder{
       private Button button;
    }
}
