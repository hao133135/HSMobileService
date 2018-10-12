package com.qindor.hsmobileservice.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Model.ProjectAndPlistModel;
import com.qindor.hsmobileservice.Model.ProjectModel;
import com.qindor.hsmobileservice.R;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceTypeActivity  extends AppCompatActivity {
    private TextView title;
    private GridView gridview;
    private SimpleAdapter saImageItems = null;
    private ProjectAndPlistModel projectAndPlistModel;
    private String msg;
    private Handler handler;
    private TextView out;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_icon);
        title = findViewById(R.id.service_title);
        title.setText("服务类型");
        gridview = findViewById(R.id.serviceGridView);
        out = findViewById(R.id.service_out_btn);
        handler = new Handler();
        projectAndPlistModel = new ProjectAndPlistModel();
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        String temp = sharedPreferences.getString("projectAndPlistModel", "");
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            projectAndPlistModel = (ProjectAndPlistModel) ois.readObject();
        } catch (Exception e) {
            msg=e.toString();
            handler.post(toast);
        }
        loadView(projectAndPlistModel.getProjectModels());
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ServiceTypeActivity.this,SellServiceActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    };

    public void loadView(final List<ProjectModel> projectModels){
        List<String> list = new ArrayList<>();
        for (ProjectModel p:projectModels){
            list.add(p.getsXMLX());
        }
        List<String> list1 = removeDuplicate(list);
        int length = list1.size();
        //生成动态数组，并且转入数据
        final ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("sXMLX", list1.get(i));
            lstImageItem.add(map);
        }

        //生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new SimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.service_icon_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sXMLX"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.service_btn});
        //添加并且显示
        gridview.setAdapter(saImageItems);
        //添加消息处理
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
        /*        String s = projectModels.get(position).getsXMLX().substring(5,projectModels.get(position).getsXMLX().length());
                String s1 = s.substring(0,s.length()-1);*/
                editor.putString("type",projectModels.get(position).getsXMLX());
                editor.putString("pro","");
                editor.commit();
                Intent i = new Intent(ServiceTypeActivity.this,SellServiceActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    public   static   List  removeDuplicate(List list)  {
        for  ( int  i  =   0 ; i  <  list.size()  -   1 ; i ++ )  {
            for  ( int  j  =  list.size()  -   1 ; j  >  i; j -- )  {
                if  (list.get(j).equals(list.get(i)))  {
                    list.remove(j);
                }
            }
        }
        return list;
    }

    /**
     * 监听返回键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            back();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back(){
        Intent i = new Intent(ServiceTypeActivity.this,SellServiceActivity.class);
        startActivity(i);
        finish();
    }
}
