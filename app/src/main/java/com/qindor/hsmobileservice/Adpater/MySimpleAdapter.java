package com.qindor.hsmobileservice.Adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.R;

import java.util.List;

public class MySimpleAdapter  extends BaseAdapter {
    private List<InformationModel> informationModels;
    private int resource;   //item的布局
    private Context context;
    private LayoutInflater inflator;
    private int selectedItem = -1;

    public MySimpleAdapter(Context context, List<InformationModel> informationModels, int resource) {
        this.context = context;
        this.informationModels = informationModels;
        this.resource = resource;
    }
    @Override
    public int getCount() {return informationModels.size();}
    @Override
    public Object getItem(int position) {
        return informationModels.get(position);}
    @Override
    public long getItemId(int position) {
        return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Viewholder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.room_icon_items, null);
            viewHolder = new Viewholder();
            viewHolder.information = convertView.findViewById(R.id.room_icon_sTBH);
            viewHolder.service = convertView.findViewById(R.id.room_icon_sXMMC);
            viewHolder.sate = convertView.findViewById(R.id.room_icon_state);
            viewHolder.count = convertView.findViewById(R.id.room_icon_fSL);
            viewHolder.layout = convertView.findViewById(R.id.shoukuan);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (Viewholder) convertView.getTag();
        }
        InformationModel informationModel  = informationModels.get(position);
        viewHolder.information.setText(informationModel.getsTBH());
        viewHolder.service.setText(informationModel.getsTMC());
        viewHolder.sate.setText(informationModel.getsTZT());
        viewHolder.count.setText(informationModel.getsRTS()+"/"+informationModel.getsNTS());
        if (!informationModel.getsRTS().equals("0"))
        {
            viewHolder.layout.setBackgroundResource(R.drawable.shape_edit_back_black);
        }else
        {
            viewHolder.layout.setBackgroundResource(R.drawable.shape_edit_back_green);
        }
        return convertView;
    }
    class Viewholder {
        private TextView information,service,sate,count;
        private View layout;
    }
}



