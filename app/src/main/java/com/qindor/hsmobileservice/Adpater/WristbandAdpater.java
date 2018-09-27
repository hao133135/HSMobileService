package com.qindor.hsmobileservice.Adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qindor.hsmobileservice.Model.RoomModel;
import com.qindor.hsmobileservice.R;

import java.util.List;

public class WristbandAdpater<T>  extends BaseAdapter {
    private List<RoomModel> roomModels;
    private int resource;   //item的布局
    private Context context;
    private LayoutInflater inflator;
    private int selectedItem = -1;
    /**
     *
     * @param context mainActivity
     * @param roomModels   显示的数据
     * @param resource  一个Item的布局
     */
    public WristbandAdpater(Context context, List<RoomModel> roomModels, int resource){
        this.context = context;
        this.roomModels = roomModels;
        this.resource = resource;
    }
    /*
     * 获得数据总数
     * */
    @Override
    public int getCount() {
        return roomModels.size();
    }
    /*
     * 根据索引为position的数据
     * */
    @Override
    public Object getItem(int position) {
        return roomModels.get(position);
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
        WristbandAdpater.ViewHolder viewHolder;
        if(convertView==null){
            inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(resource, null);
            viewHolder = new WristbandAdpater.ViewHolder();
            viewHolder.sXMMC = convertView.findViewById(R.id.room_wristband_sXMMC);
            viewHolder.fXMDJ = convertView.findViewById(R.id.room_wristband_fXMDJ);
            viewHolder.sZLX = convertView.findViewById(R.id.room_wristband_sZLX);
            viewHolder.fSL = convertView.findViewById(R.id.room_wristband_fSL);
            viewHolder.sJSXM = convertView.findViewById(R.id.room_wristband_sJSXM);
            viewHolder.fXMJE = convertView.findViewById(R.id.room_wristband_fXMJE);
            viewHolder.sDateYMDHMSSZ = convertView.findViewById(R.id.room_wristband_sDateYMDHMSSZ);
            //为了减少开销，则只在第一页时调用findViewById
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (WristbandAdpater.ViewHolder)convertView.getTag();
        }
        RoomModel roomModel  = roomModels.get(position);
        viewHolder.sXMMC.setText(roomModel.getsXMMC());
        viewHolder.fXMDJ.setText(roomModel.getfXMDJ());
        viewHolder.sZLX.setText(roomModel.getsZLX());
        viewHolder.fSL.setText(roomModel.getfSL());
        viewHolder.sJSXM.setText(roomModel.getsJSXM());
        viewHolder.fXMJE.setText(roomModel.getfXMJE());
        int i = roomModel.getsDateYMDHMSSZ().indexOf(" ");
        viewHolder.sDateYMDHMSSZ.setText(roomModel.getsDateYMDHMSSZ().substring(i,roomModel.getsDateYMDHMSSZ().length()));
        return convertView;
    }
    public void setSelectedItem(int selectedItem)
    {
        this.selectedItem = selectedItem;
    }
    class ViewHolder{
        private TextView sXMMC,fXMDJ,sZLX,fSL,sJSXM,fXMJE,sDateYMDHMSSZ;
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
