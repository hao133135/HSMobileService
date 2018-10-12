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

public class ServiceProjectActivity  extends AppCompatActivity {
    private TextView title;
    private GridView gridview;
    private SimpleAdapter saImageItems = null;
    private ProjectAndPlistModel projectAndPlistModel;
    private String msg;
    private Handler handler;
    private TextView out;
    private String type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_icon);
        title = findViewById(R.id.service_title);
        gridview = findViewById(R.id.serviceGridView);
        out = findViewById(R.id.service_out_btn);
        handler = new Handler();
        projectAndPlistModel = new ProjectAndPlistModel();
        title.setText("服务项目");
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
        Bundle bundle =this.getIntent().getExtras();
        type = bundle.getString("type");
        loadView(projectAndPlistModel.getProjectModels());
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ServiceProjectActivity.this,SellServiceActivity.class);
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
        int length = projectModels.size();
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (type.equals(projectModels.get(i).getsXMLX()))
            {
                map.put("sXMMC", projectModels.get(i).getsXMMC());
                lstImageItem.add(map);
            }
        }

        //生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new SimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.service_icon_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sXMMC"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.service_btn});
        //添加并且显示
        gridview.setAdapter(saImageItems);
        //添加消息处理
        final ArrayList<HashMap<String, Object>> item = lstImageItem;
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
               /* String s = item.get(position).get("sXMMC").toString().substring(5,item.get(position).get("sXMMC").toString().length());
                String s1 = s.substring(0,s.length()-1);*/
                editor.putString("pro",item.get(position).get("sXMMC").toString());
                editor.putString("tec","");
                editor.commit();
                Intent i = new Intent(ServiceProjectActivity.this,SellServiceActivity.class);
                startActivity(i);
                finish();
            }
        });
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
        Intent i = new Intent(ServiceProjectActivity.this,SellServiceActivity.class);
        startActivity(i);
        finish();
    }
}
