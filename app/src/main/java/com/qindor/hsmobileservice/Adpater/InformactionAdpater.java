package com.qindor.hsmobileservice.Adpater;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.R;

import java.util.List;

public class InformactionAdpater<T> extends BaseAdapter {
    private List<InformationModel> informationModels;
    private int resource;   //item的布局
    private Context context;
    private LayoutInflater inflator;
    private int selectedItem = -1;
    /**
     *
     * @param context mainActivity
     * @param informationModels   显示的数据
     * @param resource  一个Item的布局
     */
    public InformactionAdpater(Context context, List<InformationModel> informationModels, int resource){
        this.context = context;
        this.informationModels = informationModels;
        this.resource = resource;
    }
    /*
     * 获得数据总数
     * */
    @Override
    public int getCount() {
        return informationModels.size();
    }
    /*
     * 根据索引为position的数据
     * */
    @Override
    public Object getItem(int position) {
        return informationModels.get(position);
    }
    /*
     * 根据索引值获得Item的Id
     * */
    @Override
    public long getItemId(int position) {
        return position;
    }
    /*
     *通过索引值position将数据映射到视图
     *convertView具有缓存功能，在第一页时为null，在第二第三....页时不为null
     * */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InformactionAdpater.ViewHolder viewHolder;
        if(convertView==null){
            inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(resource, null);
            viewHolder = new InformactionAdpater.ViewHolder();
            viewHolder.information = convertView.findViewById(R.id.room_list_information);
            viewHolder.service = convertView.findViewById(R.id.room_list_service);
            viewHolder.sate = convertView.findViewById(R.id.room_list_sate);
            viewHolder.count = convertView.findViewById(R.id.room_list_count);
            viewHolder.layout = convertView.findViewById(R.id.room_list_layout);
            //为了减少开销，则只在第一页时调用findViewById
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (InformactionAdpater.ViewHolder)convertView.getTag();
        }
        InformationModel informationModel  = informationModels.get(position);
        viewHolder.information.setText(informationModel.getsTBH());
        viewHolder.service.setText(informationModel.getsTMC());
        viewHolder.sate.setText(informationModel.getsTZT());
        viewHolder.count.setText(informationModel.getsRTS()+"/"+informationModel.getsNTS());
        if (!informationModel.getsRTS().equals("0"))
        {
            viewHolder.layout.setBackgroundResource(R.color.ash1);
            viewHolder.information.setTextColor(Color.WHITE);
            viewHolder.service.setTextColor(Color.WHITE);
            viewHolder.sate.setTextColor(Color.WHITE);
            viewHolder.count.setTextColor(Color.WHITE);
        }else
        {
            viewHolder.layout.setBackgroundResource(R.color.white);
            viewHolder.information.setTextColor(Color.BLACK);
            viewHolder.service.setTextColor(Color.BLACK);
            viewHolder.sate.setTextColor(Color.BLACK);
            viewHolder.count.setTextColor(Color.BLACK);
        }
        return convertView;
    }
    public void setSelectedItem(int selectedItem)
    {
        this.selectedItem = selectedItem;
    }
    class ViewHolder{
        private TextView information,service,sate,count;
        private View layout;
    }
    /**
     * 局部刷新
     * @param view
     * @param itemIndex
     */
    public void updateView(View view, int itemIndex) {
        if (view == null) {
            return;
        }else if(itemIndex == selectedItem){
            return;
        }
    }
}
