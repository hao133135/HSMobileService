package com.qindor.hsmobileservice.Adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qindor.hsmobileservice.R;

import java.util.List;

public class RoomDialogAdpater extends BaseAdapter {
    private List<String> list;
    private int resource;   //item的布局
    private Context context;
    private LayoutInflater inflator;
    private int selectedItem = -1;

    public RoomDialogAdpater(Context context, List<String> sslist, int resource) {
        this.context = context;
        this.list = sslist;
        this.resource = resource;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RoomDialogAdpater.ViewHolder viewHolder;
        if(convertView==null){
            inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(resource, null);
            viewHolder = new RoomDialogAdpater.ViewHolder();
            viewHolder.serviceName = convertView.findViewById(R.id.room_name);
            //为了减少开销，则只在第一页时调用findViewById
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (RoomDialogAdpater.ViewHolder)convertView.getTag();
        }
        String s  = list.get(position);
        viewHolder.serviceName.setText(s);
        return convertView;
    }

    class ViewHolder{
        private TextView serviceName,title;
    }
}
