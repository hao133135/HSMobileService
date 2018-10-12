package com.qindor.hsmobileservice.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.ProjectAndPlistModel;
import com.qindor.hsmobileservice.Model.ProjectModel;
import com.qindor.hsmobileservice.Model.RoomsModel;
import com.qindor.hsmobileservice.Model.TechnicianModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Configuration;
import com.qindor.hsmobileservice.Utils.HttpUtils;
import com.qindor.hsmobileservice.Utils.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SellServiceActivity  extends AppCompatActivity implements View.OnClickListener{
    private Button b1,b2;
    private Configuration configuration;
    private Handler handler;
    private String msg,tec,tecNum;
    private Map map;
    private BaseModel baseModel;
    private RoomsModel roomModel;
    private String resultData,pro1,pro2,userid,sKey;
    private List<TechnicianModel> technicianModels;
    private List<String> tslist;
    private ArrayAdapter<String> adapter;
    private TextView t1,p1,t2,p2,title,t4,t5,t6;
    private boolean isDate = false;
    private ProjectAndPlistModel projectAndPlistModel;
    private HttpUtils httpUtils;
    private LoadingDialog dialog1;
    private View v1,v2,v3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wristband_sell_service);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(SellServiceActivity.this)
                .setMessage("加载中...")
                .setCancelable(false).create();
        httpUtils = new HttpUtils();
        b1 = findViewById(R.id.room_wristband_service_btn);
        b2 = findViewById(R.id.room_wristband_service_out_btn);
        t1 = findViewById(R.id.room_wristband_service_iSZZC);
        p1 = findViewById(R.id.room_wristband_service_fSZDJ);
        t2 = findViewById(R.id.room_wristband_service_iJZZC);
        p2 = findViewById(R.id.room_wristband_service_fJZDJ);
        t4 = findViewById(R.id.service_type_name);
        t5 = findViewById(R.id.service_project_name);
        t6 = findViewById(R.id.service_technician_name);
        v1 = findViewById(R.id.service_type);
        v2 = findViewById(R.id.service_project);
        v3 = findViewById(R.id.service_technician);
        //spinner = findViewById(R.id.service_t_sp);
        title = findViewById(R.id.hotspring_title);
        title.setText("购买服务");
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        isDate = sharedPreferences.getBoolean("isDate",false);
        tec = sharedPreferences.getString("tec", "");
        tecNum = sharedPreferences.getString("tecNum", "");
        String temp = sharedPreferences.getString("rooms", "");
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            roomModel = (RoomsModel) ois.readObject();
        } catch (Exception e) {
            msg=e.toString();
            handler.post(toast);
        }
        setData(sharedPreferences);
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        configuration = new Configuration();
        technicianModels = new ArrayList<>();
        handler = new Handler();
        tslist = new ArrayList<>();
        map = new HashMap();
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        v1.setOnClickListener(this);
        v2.setOnClickListener(this);
        v3.setOnClickListener(this);
        //getTData();
        if(!sharedPreferences.getString("projectAndPlistModel", "").equals("")) {
            if (isDate) {
                getPData();
            } else {
                String temp2 = sharedPreferences.getString("projectAndPlistModel", "");
                ByteArrayInputStream bais2 = new ByteArrayInputStream(Base64.decode(temp2.getBytes(), Base64.DEFAULT));
                try {
                    ObjectInputStream ois = new ObjectInputStream(bais2);
                    projectAndPlistModel = (ProjectAndPlistModel) ois.readObject();
                } catch (Exception e) {
                    msg = e.toString();
                    handler.post(toast);
                }
                //loadView(projectAndPlistModel.getProjectModels());
            }
        }else {
            getPData();
        }

    }

    private void setData(SharedPreferences sharedPreferences) {
        if (!sharedPreferences.getString("type","").equals(""))
        {
            t4.setText("服务类型："+sharedPreferences.getString("type",""));
        }else {
            t4.setText("服务类型");
        }
        if (!sharedPreferences.getString("pro","").equals(""))
        {
            t5.setText("服务项目："+sharedPreferences.getString("pro",""));
            phander.sendEmptyMessageDelayed(0,1000);
        }else {
            t5.setText("服务项目");
        }
        if (!sharedPreferences.getString("tec","").equals(""))
        {
            t6.setText("技师姓名："+sharedPreferences.getString("tec",""));
        }else {
            t6.setText("技师选择/自动分配");
        }
    }

    /* private void loadView(List<ProjectModel> projectModels) {
         int length = projectModels.size();
         //生成动态数组，并且转入数据
         ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
         for (int i = 0; i < length; i++) {
             HashMap<String, Object> map = new HashMap<String, Object>();
             map.put("sXMLX", projectModels.get(i).getsXMLX());
             lstImageItem.add(map);
         }
         lstImageItem = getSingle(lstImageItem);
         //生成适配器的ImageItem 与动态数组的元素相对应
         saImageItems = new SimpleAdapter(this,
                 lstImageItem,//数据来源
                 R.layout.service_icon_items,//item的XML实现
                 //动态数组与ImageItem对应的子项
                 new String[]{"sXMLX"},
                 //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                 new int[]{R.id.service_btn});
         //添加并且显示
         gridView1.setAdapter(saImageItems);
         final ArrayList<HashMap<String, Object>> finalLstImageItem = lstImageItem;
         gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 pro1 = finalLstImageItem.get(position).get("sXMLX").toString();
                 for (int i=0;i< parent.getCount();i++)
                 {
                     View v=parent.getChildAt(i);
                     if (position == i) {
                         TextView mChoosedTv = (TextView) v.findViewById(R.id.service_btn);
                         mChoosedTv.setBackgroundResource(R.drawable.shape_edit_back_green);
                     } else {
                         TextView mNormalTv = (TextView) v.findViewById(R.id.service_btn);
                         mNormalTv.setBackgroundResource(R.drawable.shape_ac_login_btn_back_fill);
                     }
                 }
                 handler.post(loadPro);
             }
         });
     }*/
    public static ArrayList getSingle(ArrayList list){
        ArrayList newList = new ArrayList();     //创建新集合
        Iterator it = list.iterator();        //根据传入的集合(旧集合)获取迭代器
        while(it.hasNext()){          //遍历老集合
            Object obj = it.next();       //记录每一个元素
            if(!newList.contains(obj)){      //如果新集合中不包含旧集合中的元素
                newList.add(obj);       //将元素添加
            }
        }
        return newList;
    }
   /* Runnable loadPro = new Runnable() {
        @Override
        public void run() {
            setPData(projectAndPlistModel.getProjectModels());
        }
    };*/

  /*  private void setPData(List<ProjectModel> projectModels) {
        int length = projectModels.size();
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (pro1.equals(projectModels.get(i).getsXMLX()))
            {
                map.put("sXMMC", projectModels.get(i).getsXMMC());
                lstImageItem.add(map);
            }
        }
        lstImageItem = getSingle(lstImageItem);
        //生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new SimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.service_icon_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sXMMC"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.service_btn});
        //添加并且显示
        gridView2.setAdapter(saImageItems);
        final ArrayList<HashMap<String, Object>> finalLstImageItem = lstImageItem;
        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pro2 = finalLstImageItem.get(position).get("sXMMC").toString();
                for (int i=0;i< parent.getCount();i++)
                {
                    View v=parent.getChildAt(i);
                    if (position == i) {
                        TextView mChoosedTv = (TextView) v.findViewById(R.id.service_btn);
                        mChoosedTv.setBackgroundResource(R.drawable.shape_edit_back_green);
                    } else {
                        TextView mNormalTv = (TextView) v.findViewById(R.id.service_btn);
                        mNormalTv.setBackgroundResource(R.drawable.shape_ac_login_btn_back_fill);
                    }
                }
                handler.post(loadTer);
            }
        });
    }*/
   /* Runnable loadTer = new Runnable() {

        @Override
        public void run() {
            getTData();
            for (ProjectModel p : projectAndPlistModel.getProjectModels()) {
                if (p.getsXMMC().equals(pro2)) {
                    t1.setText(p.getiSZZC());
                    p1.setText(p.getfSZDJ());
                    t2.setText(p.getiJZZC());
                    p2.setText(p.getfJZDJ());
                }
            }
        }
    };*/
    private void getPData() {
        //{"code":"getjxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        map.clear();
        map.put("code","getjxm");
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
                returnedPData(resultData);
            }
        });
        //resultData = "{\"code\":\"getjxm\",\"ret\":\"0\",\"msg\":[{\"sXMLX\":\"按摩类\",\"sXMMC\":\"推背\",\"iSZZC\":\"30\",\"fSZDJ\":\"80\",\"iJZZC\":\"15\",\"fJZDJ\":\"30\"},{\"sXMLX\":\"按摩类\",\"sXMMC\":\"按摩\",\"iSZZC\":\"30\",\"fSZDJ\":\"100\",\"iJZZC\":\"15\",\"fJZDJ\":\"50\"},{\"sXMLX\":\"洗浴类\",\"sXMMC\":\"足疗\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类1\",\"sXMMC\":\"足疗1\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类2\",\"sXMMC\":\"足疗2\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类3\",\"sXMMC\":\"足疗3\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类4\",\"sXMMC\":\"足疗4\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类5\",\"sXMMC\":\"足疗5\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"}]}";

        //returnedPData(resultData);
    }

    private void returnedPData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                projectAndPlistModel = new ProjectAndPlistModel();
                List<ProjectModel> pl = new ArrayList<>();
                List<String> ps = new ArrayList<>();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    ProjectModel projectModel = new ProjectModel(jsonObject1.getString("sXMLX"),jsonObject1.getString("sXMMC"),jsonObject1.getString("iSZZC"),jsonObject1.getString("fSZDJ"),jsonObject1.getString("iJZZC"),jsonObject1.getString("fJZDJ"));
                    pl.add(projectModel);
                    ps.add(jsonObject1.getString("sXMMC"));
                }
                projectAndPlistModel.setProjectModels(pl);
                projectAndPlistModel.setPslist(ps);
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(projectAndPlistModel);//把对象写到流里
                    String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                    editor.putString("projectAndPlistModel", temp);
                    editor.putBoolean("isDate",false);
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
             //   handler.post(setPTData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /*Runnable setPTData = new Runnable() {
        @Override
        public void run() {
            loadView(projectAndPlistModel.getProjectModels());
        }
    };*/
    private void getTData() {
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
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        //resultData = "{\"code\":\"getjsl \",\"ret\":\"0\",\"msg\":[{\"sGH\":\"A001\",\"sXM\":\"张三\",\"sBM\":\"桑拿部\",\"sGZ\":\"桑拿技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"},{\"sGH\":\"B001\",\"sXM\":\"李四\",\"sBM\":\"足浴部\",\"sGZ\":\"足浴技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"}]}";
        //returnedTData(resultData);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
                resultData = "{\"code\":\"getjsl \",\"ret\":\"0\",\"msg\":[{\"sGH\":\"A001\",\"sXM\":\"张三\",\"sBM\":\"桑拿部\",\"sGZ\":\"桑拿技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"},{\"sGH\":\"B001\",\"sXM\":\"李四\",\"sBM\":\"足浴部\",\"sGZ\":\"足浴技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"}]}";
                returnedTData(resultData);
            }
        }).start();*/
    }

    private void returnedTData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                technicianModels.clear();
                tslist.clear();
                //tslist.add("自动分配");
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    TechnicianModel technicianModel = new TechnicianModel(jsonObject1.getString("sGH"),jsonObject1.getString("sXM"),jsonObject1.getString("sBM"),jsonObject1.getString("sGZ"),jsonObject1.getString("sJB"),jsonObject1.getString("sZT"),jsonObject1.getString("sXB"));
                    technicianModels.add(technicianModel);
                    if (pro2.substring(0,1).equals(jsonObject1.getString("sBM").substring(0,1)))
                    {
                        if (jsonObject1.getString("sZT").equals("空闲")) {
                            tslist.add(jsonObject1.getString("sXM"));
                        }
                    }
                }
                //new TSpinnerTask().execute();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
  /*  class TSpinnerTask extends AsyncTask<Object, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Object... params) {
            return tslist;
        }
        @Override
        protected void onPostExecute(List<String> result) {
            // TODO Auto-generated method stub
            tspinnerClick();
        }
    }*/


    /*private void tspinnerClick() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tslist);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        spinner.setAdapter(adapter);
        int k= adapter.getCount();
        for(int i=0;i<k;i++){
            if("自动分配".equals(adapter.getItem(i).toString())){
                spinner.setSelection(i,true);
                break;
            }
        }
    }*/

    private Handler phander = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (ProjectModel p : projectAndPlistModel.getProjectModels()) {
                if (p.getsXMMC().equals(t5.getText().toString().substring(5,t5.getText().toString().length()))) {
                    t1.setText(p.getiSZZC());
                    p1.setText(p.getfSZDJ());
                    t2.setText(p.getiJZZC());
                    p2.setText(p.getfJZDJ());
                }
            }
        }
    };
    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        Bundle bundle1 = new Bundle();
        switch (v.getId())
        {
            case R.id.room_wristband_service_btn:
                if(!t5.getText().toString().equals("服务项目")||!t5.getText().toString().equals("")) {
                    sell();
                }else {
                    msg = "请选择服务项目";
                    handler.post(toast);
                }
                break;
            case R.id.room_wristband_service_out_btn:
                back();
                break;
            case R.id.service_type:
                i = new Intent(SellServiceActivity.this, ServiceTypeActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.service_project:
                if (t4.getText().toString().equals("服务类型")){
                    msg = "请选择服务类型";
                    handler.post(toast);
                }else {
                    i = new Intent(SellServiceActivity.this, ServiceProjectActivity.class);
                    bundle1.putSerializable("type", t4.getText().toString().substring(5,t4.getText().toString().length()));
                    i.putExtras(bundle1);
                    startActivity(i);
                    finish();
                }
                break;
            case R.id.service_technician:
                if (t5.getText().toString().equals("服务项目")){
                    msg = "请选择服务项目";
                    handler.post(toast);
                }else {
                    i = new Intent(SellServiceActivity.this, ServiceTechnicianActivity.class);
                    bundle1.putSerializable("pro", t5.getText().toString().substring(5,t5.getText().toString().length()));
                    i.putExtras(bundle1);
                    startActivity(i);
                    finish();
                }
                break;
        }
    }

    private void sell() {
        //{"code":"dodxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sXM":"推背","sJS":"A001","sTH":"301",}}
        map.clear();
        map.put("code","dodxm");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sWD",roomModel.getModels().get(0).getsWDBH());
             data.put("sXM",t5.getText().toString());
            if(!"技师选择/自动分配".equals(t6.getText().toString())){
                if(tecNum!=null)
                {
                    data.put("sJS",tecNum);
                }
            }else {
                data.put("sJS","");
            }
            data.put("sTH",roomModel.getModels().get(0).getsTBH());
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
                resultData="";
                resultData = response.body().string();
                returnedSData(resultData);
            }
        });
    }
    private void returnedSData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                msg = jsonObject.getString("msg");
                handler.post(toast);
                Intent i = new Intent(SellServiceActivity.this, RoomActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("rooms",roomModel);
                i.putExtras(bundle1);
                startActivity(i);
                finish();
            }
            else {
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            try {
                JSONObject jsonObject = new JSONObject(resultData);
                msg = jsonObject.getString("msg");
                handler.post(toast);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }
    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
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

    private void back() {
        Intent i = new Intent(SellServiceActivity.this, InformationActivity.class);
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("rooms",roomModel);
        i.putExtras(bundle1);
        startActivity(i);
        finish();
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
}
