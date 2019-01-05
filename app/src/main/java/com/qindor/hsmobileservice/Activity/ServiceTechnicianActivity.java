package com.qindor.hsmobileservice.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.ProjectAndPlistModel;
import com.qindor.hsmobileservice.Model.TechnicianModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServiceTechnicianActivity  extends AppCompatActivity {
    private TextView title;
    private GridView gridview;
    private SimpleAdapter saImageItems = null;
    private ProjectAndPlistModel projectAndPlistModel;
    private String msg;
    private Handler handler;
    private TextView out;
    private String pro;
    private Map map ;
    private BaseModel baseModel;
    private HttpUtils httpUtils;
    private String userid,sKey,resultData;
    private List<TechnicianModel> technicianModels;
    private List<TechnicianModel> newTec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_icon);
        title = findViewById(R.id.service_title);
        title.setText("服务技师");
        gridview = findViewById(R.id.serviceGridView);
        out = findViewById(R.id.service_out_btn);
        handler = new Handler();
        map = new HashMap();
        httpUtils = new HttpUtils();
        technicianModels = new ArrayList<>();
        newTec = new ArrayList<>();
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
       /* Bundle bundle = this.getIntent().getExtras();
        pro = bundle.getString("pro");*/
        getData();
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ServiceTechnicianActivity.this,wristbandServiceActivity.class);
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

    public void loadView(final List<TechnicianModel> technicianModels){
        int length = technicianModels.size();
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("sXM", newTec.get(i).getsXM()+"("+newTec.get(i).getsXB()+")");
            map.put("type",newTec.get(i).getsGZ()+"/"+newTec.get(i).getsJB());
            lstImageItem.add(map);
        }

        //生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new SimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.service_t_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sXM","type"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.service_t_name,R.id.service_t_type});
        //添加并且显示
        gridview.setAdapter(saImageItems);
        //添加消息处理
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
                /*String s = newTec.get(position).getsXM().substring(3,newTec.get(position).getsXM().length());
                String s1 = s.substring(0,s.length()-1);*/
                editor.putString("tec",newTec.get(position).getsXM());
                editor.putString("tecNum",newTec.get(position).getsGH());
                editor.commit();
                Intent i = new Intent(ServiceTechnicianActivity.this,wristbandServiceActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void getData() {
        //{"code":"getjsl","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        map.put("code","getjsl");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        OkHttpClient mOkHttpClient = new OkHttpClient();//创建OkHttpClient对象。
        Request request = new Request.Builder()//创建Request 对象。
                .url(  "http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/spring")
                .post(httpUtils.baseOkHttp(baseModel,userid,sKey,map).build())//传递请求体
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                msg=e.toString();
                handler.post(toast);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                resultData = response.body().string();
                returnedTData(resultData);
            }
        });
    }

    private void returnedTData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                technicianModels.clear();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    TechnicianModel technicianModel = new TechnicianModel(jsonObject1.getString("sGH"),jsonObject1.getString("sXM"),jsonObject1.getString("sBM"),jsonObject1.getString("sGZ"),jsonObject1.getString("sJB"),jsonObject1.getString("sZT"),jsonObject1.getString("sXB"));
                    technicianModels.add(technicianModel);
                    newTec.add(technicianModel);
                }
                if(newTec.size()!=0) {
                    handler.post(setData);
                }else
                {
                    msg = "暂无空闲技师";
                    handler.post(toast);
                }
            }
            else
            {
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            msg = e.toString();
            handler.post(toast);
            //e.printStackTrace();
        }
    }
    Runnable setData = new Runnable() {
        @Override
        public void run() {
            loadView(technicianModels);
        }
    };

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
        Intent i = new Intent(ServiceTechnicianActivity.this,wristbandServiceActivity.class);
        startActivity(i);
        finish();
    }
}
