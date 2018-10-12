package com.qindor.hsmobileservice.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.WristbandAdpater;
import com.qindor.hsmobileservice.Adpater.WristbandDialogAdpater;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.Model.ProjectAndPlistModel;
import com.qindor.hsmobileservice.Model.ProjectModel;
import com.qindor.hsmobileservice.Model.RegionModel;
import com.qindor.hsmobileservice.Model.RoomModel;
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
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WristbandActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private Map<String, Object> map;
    private String resultData,re,code;
    private RoomsModel roomModel;
    private WristbandAdpater wristbandAdpater;
    private WristbandDialogAdpater wristbandDialogAdpater;
    private ListView listView;
    private LayoutInflater inflater;
    private ProjectAndPlistModel projectAndPlistModel;
    private Spinner serviceSpinner,tSpinner;
    private Button regionBtn,outBtn,sbtn,sobtn,pSpinner;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> adapter;
    private List<String> sslist = new ArrayList<>(),tslist=new ArrayList<>();
    private String msg,sout,userid,sKey;
    private TextView open,sell,t1,p1,t2,p2,wristband,title,dtitle;
    private List<TechnicianModel> technicianModels;
    private List<RoomModel> roomModels;
    private boolean isDate=false;
    private InformationModel informationModel;
    private LoadingDialog dialog1;
    private ListView dlistView;
    private SimpleAdapter saImageItems = null;
    private RoomModel sXMMC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_wristband);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(WristbandActivity.this)
                .setMessage("加载中...")
                .setCancelable(false).create();
        listView = findViewById(R.id.room_wristband_list_view);
        open = findViewById(R.id.room_wristband_open);
        sell = findViewById(R.id.room_wristband_sell);
        wristband = findViewById(R.id.room_wristband_wr);
        title = findViewById(R.id.hotspring_title);
        sXMMC = new RoomModel();
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        isDate = sharedPreferences.getBoolean("isDate",false);
        String temp1 = sharedPreferences.getString("informationModel", "");
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        ByteArrayInputStream bais1 =  new ByteArrayInputStream(Base64.decode(temp1.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais1);
            informationModel = (InformationModel) ois.readObject();
        } catch (Exception e) {
            msg=e.toString();
            handler.post(toast);
        }
        String temp = sharedPreferences.getString("rooms", "");
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            roomModel = (RoomsModel) ois.readObject();
            title.setText("腕带号："+roomModel.getModels().get(0).getsWDBH());
        } catch (Exception e) {
            msg=e.toString();
            handler.post(toast);
        }
        //wristband.setText("腕带号："+roomModel.getModels().get(0).getsWDBH());
        roomModels = new ArrayList<>();
        httpUtils = new HttpUtils();
        technicianModels = new ArrayList<>();
        configuration = new Configuration();
        projectAndPlistModel= new ProjectAndPlistModel();
        handler = new Handler();
        map = new HashMap<String, Object>();
        open.setOnClickListener(this);
        sell.setOnClickListener(this);
     /*   LoadingDialog.Builder builder1=new LoadingDialog.Builder(WristbandActivity.this)
                .setMessage("加载中...")
                .setCancelable(false);
        final LoadingDialog dialog1=builder1.create();
        dialog1.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog1.dismiss();
            }
        },1000);*/
        SharedPreferences sp=getSharedPreferences("config",0);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("pro", "");
        editor.putString("type", "");
        editor.putString("tec", "");
        editor.commit();
        handler.post(setData);
        if(isDate){
            getPData();
        }else {
            String temp2 = sharedPreferences.getString("projectAndPlistModel", "");
            ByteArrayInputStream bais2 =  new ByteArrayInputStream(Base64.decode(temp2.getBytes(), Base64.DEFAULT));
            try {
                ObjectInputStream ois = new ObjectInputStream(bais2);
                projectAndPlistModel = (ProjectAndPlistModel) ois.readObject();
            } catch (Exception e) {
                msg=e.toString();
                handler.post(toast);
            }
        }

        //getTData();
        setServiceData();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.room_wristband_open:
                getServiceData("doklt");
                break;
            case R.id.room_wristband_sell:
                //showServiceDialog();
                Intent i = new Intent(WristbandActivity.this, SellServiceActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    private void getTData() {
        //{"code":"getjsl","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        map.clear();
        map.put("code","getjsl");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        resultData = null;
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                returnedTData(resultData);
            }
        }).start();*/
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
        //resultData = "{\"code\":\"getjsl \",\"ret\":\"0\",\"msg\":[{\"sGH\":\"A001\",\"sXM\":\"张三\",\"sBM\":\"桑拿部\",\"sGZ\":\"桑拿技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"},{\"sGH\":\"B001\",\"sXM\":\"李四\",\"sBM\":\"足浴部\",\"sGZ\":\"足浴技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"}]}";
    }
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
        resultData = null;
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                //resultData = "{\"code\":\"getjxm\",\"ret\":\"0\",\"msg\":[{\"sXMLX\":\"按摩类\",\"sXMMC\":\"推背\",\"iSZZC\":\"30\",\"fSZDJ\":\"80\",\"iJZZC\":\"15\",\"fJZDJ\":\"30\"},{\"sXMLX\":\"按摩类\",\"sXMMC\":\"按摩\",\"iSZZC\":\"30\",\"fSZDJ\":\"100\",\"iJZZC\":\"15\",\"fJZDJ\":\"50\"},{\"sXMLX\":\"洗浴类\",\"sXMMC\":\"足疗\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"}]}";
                returnedPData(resultData);
            }
        }).start();*/
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
    }
    private void getData() {
        //{"code":"gettxx","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sTBH":"301"}}
        map.clear();
        map.put("code","gettxx");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sTBH",roomModel.getModels().get(0).getsTBH());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        resultData = null;
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                //resultData = "{\"code\":\"gettxx\",\"ret\":\"0\",\"msg\":[{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"20.00\",\"fSL\":\"1.00\",\"fXMJE\":\"20.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"张三\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:41:41\",\"sDateYMDHMSXZ\":\"2018-08-11 16:41:41\",\"iZSC\":\"60\",\"iSY\":\"60\"},{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"25.00\",\"fSL\":\"1.00\",\"fXMJE\":\"25.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"李四\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:42:42\",\"sDateYMDHMSXZ\":\"2018-08-11 16:42:42\",\"iZSC\":\"60\",\"iSY\":\"60\"}]}";
                returnedData(resultData);
            }
        }).start();*/
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
                returnedData(resultData);
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
                tslist.clear();
                tslist.add("自动分配");
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    TechnicianModel technicianModel = new TechnicianModel(jsonObject1.getString("sGH"),jsonObject1.getString("sXM"),jsonObject1.getString("sBM"),jsonObject1.getString("sGZ"),jsonObject1.getString("sJB"),jsonObject1.getString("sZT"),jsonObject1.getString("sXB"));
                    technicianModels.add(technicianModel);
                    tslist.add(jsonObject1.getString("sXM"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    class TSpinnerTask extends AsyncTask<Object, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Object... params) {
            return tslist;
        }
        @Override
        protected void onPostExecute(List<String> result) {
            // TODO Auto-generated method stub
           // tspinnerClick();
        }
    }
  /*  private void tspinnerClick() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tslist);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        tSpinner.setAdapter(adapter);
        int k= adapter.getCount();
        for(int i=0;i<k;i++){
            if("自动分配".equals(adapter.getItem(i).toString())){
                pSpinner.setSelection(i,true);
                break;
            }
        }
    }*/
    class PSpinnerTask extends AsyncTask<Object, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Object... params) {
            return projectAndPlistModel.getPslist();
        }
        @Override
        protected void onPostExecute(List<String> result) {
            // TODO Auto-generated method stub
            //pspinnerClick();
        }
    }
    /*private void pspinnerClick() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projectAndPlistModel.getPslist());
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        pSpinner.setAdapter(adapter);
        int k= adapter.getCount();
        for(int i=0;i<k;i++){
            if("推背".equals(adapter.getItem(i).toString())){
                pSpinner.setSelection(i,true);
                break;
            }
        }
    }*/
    private void showServiceDialog() {
        AlertDialog.Builder setDeBugDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.wristband_sell_service, null);
       // pSpinner = dialogView.findViewById(R.id.room_wristband_service_p);
       // tSpinner = dialogView.findViewById(R.id.room_wristband_service_t);
        t1 = dialogView.findViewById(R.id.room_wristband_service_iSZZC);
        p1 = dialogView.findViewById(R.id.room_wristband_service_fSZDJ);
        t2 = dialogView.findViewById(R.id.room_wristband_service_iJZZC);
        p2 = dialogView.findViewById(R.id.room_wristband_service_fJZDJ);
        sbtn = dialogView.findViewById(R.id.room_wristband_service_btn);
        sobtn = dialogView.findViewById(R.id.room_wristband_service_out_btn);
        new PSpinnerTask().execute();
        new TSpinnerTask().execute();
        setDeBugDialog = new AlertDialog.Builder(this);
        setDeBugDialog.setView(dialogView);
        alertDialog = setDeBugDialog.create();
        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        phander.sendEmptyMessageDelayed(0,1000);
        sbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(sellService);
                alertDialog.dismiss();
            }
        });
        sobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

   Runnable sellService = new Runnable() {
       @Override
       public void run() {
           //{"code":"dodxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sXM":"推背","sJS":"A001","sTH":"301",}}
           map.clear();
           map.put("code","getwdh");
           JSONObject data = new JSONObject();
           try {
               data.put("sMAC",baseModel.getMac());
               data.put("sIP",baseModel.getIp());
               data.put("sWD",roomModel.getModels().get(0).getsWDBH());
              // data.put("sXM",pSpinner.getSelectedItem().toString());
               if(!"自动分配".equals(tSpinner.getSelectedItem().toString())){
                   for (TechnicianModel t: technicianModels)
                   {
                       if (t.getsXM().equals(tSpinner.getSelectedItem().toString()))
                       {
                           data.put("sJS",t.getsGH());
                           break;
                       }
                   }
               }else {
                   data.put("sJS","");
               }
               data.put("sTH",roomModel.getModels().get(0).getsTBH());
           } catch (JSONException e) {
               e.printStackTrace();
           }
           map.put("msg",data);
           resultData = null;
           //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
           //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
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
                   returnedSData(resultData);
               }
           });
           //resultData = "{\"code\":\"dodxm\",\"ret\":\"0\",\"msg\":\"点服务成功\"}";
           //returnedSData(resultData);
       }
   };

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
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private Handler phander = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (ProjectModel p : projectAndPlistModel.getProjectModels()) {
               /* if (p.getsXMMC().equals(pSpinner.getSelectedItem().toString())) {
                    t1.setText(p.getiSZZC());
                    p1.setText(p.getfSZDJ());
                    t2.setText(p.getiJZZC());
                    p2.setText(p.getfJZDJ());
                }*/
            }
            /*pSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    for (ProjectModel p: projectAndPlistModel.getProjectModels() )
                    {
                        if(p.getsXMMC().equals(pSpinner.getSelectedItem().toString()))
                        {
                            t1.setText(p.getiSZZC());
                            p1.setText(p.getfSZDJ());
                            t2.setText(p.getiJZZC());
                            p2.setText(p.getfJZDJ());
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/
        }
    };

    private void setServiceData() {
        sslist.clear();
        sslist.add("上钟");
        sslist.add("加钟");
        sslist.add("减钟");
        sslist.add("下钟");
        sslist.add("退单");
    }
    private void returnedData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                roomModels.clear();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    if(jsonObject1.getString("sWDBH").equals(roomModel.getModels().get(0).getsWDBH())) {
                        RoomModel roomModel = new RoomModel(jsonObject1.getString("sDWID"), jsonObject1.getString("sWDBH"), jsonObject1.getString("sXMMC"), jsonObject1.getString("fXMDJ"), jsonObject1.getString("fSL"), jsonObject1.getString("fXMJE"), jsonObject1.getString("sJSGH"), jsonObject1.getString("sJSXM"), jsonObject1.getString("sZLX"), jsonObject1.getString("sDateYMDHMSSZ"), jsonObject1.getString("sDateYMDHMSXZ"), jsonObject1.getString("iZSC"), jsonObject1.getString("iSY"));
                        roomModels.add(roomModel);
                    }
                }
                handler.post(setSData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    Runnable setData = new Runnable() {
        @Override
        public void run() {
            loadView(roomModel.getModels());
        }
    };
    Runnable setSData = new Runnable() {
        @Override
        public void run() {
            loadView(roomModels);
        }
    };
    public void loadView(final List<RoomModel> roomModels){
        wristbandAdpater = new WristbandAdpater(WristbandActivity.this,roomModels,R.layout.room_wristband_items);
        listView.setAdapter(wristbandAdpater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (roomModels.get(position).getsZLX().equals("首钟")||roomModels.get(position).getsZLX().equals("加钟"))
                {
                    showSetDeBugDialog(roomModels.get(position).getsXMMC());
                    sXMMC = roomModels.get(position);
                }
            }
        });
    }
    private void showSetDeBugDialog(String sXMMC) {
        AlertDialog.Builder setDeBugDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.wristband_dialog, null);
        //serviceSpinner = dialogView.findViewById(R.id.room_wristband_service);
        outBtn = dialogView.findViewById(R.id.wristband_dialog_out_btn);
        dlistView = dialogView.findViewById(R.id.wristband_dialog_list);
        dtitle = dialogView.findViewById(R.id.wristband_dialog_title);
        dtitle.setText(sXMMC);
        selectService=null;
        setDeBugDialog = new AlertDialog.Builder(this);
        setDeBugDialog.setView(dialogView);
        alertDialog = setDeBugDialog.create();
        alertDialog.show();
        handler.post(setDListData);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private String selectService;
    Runnable setDListData = new Runnable() {
        @Override
        public void run() {
            setServiceData();
            wristbandDialogAdpater = new WristbandDialogAdpater(WristbandActivity.this,sslist,R.layout.service_select_icon);
            dlistView.setAdapter(wristbandDialogAdpater);
            dlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    re = sslist.get(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(WristbandActivity.this);
                    builder.setMessage("确定选择"+re+"服务吗?");
                    builder.setTitle("提示");
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setPositiveButton("确认",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    myHandler.sendEmptyMessageDelayed(0, 1000);
                                }
                            });

                    builder.setNegativeButton("取消",
                            new android.content.DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    builder.create().show();
                }
            });
          /*    int length = sslist.size();
            //生成动态数组，并且转入数据
            ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < length; i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("service", sslist.get(i));
                lstImageItem.add(map);
            }
            lstImageItem = getSingle(lstImageItem);
            //生成适配器的ImageItem 与动态数组的元素相对应
            saImageItems = new SimpleAdapter(WristbandActivity.this,
                    lstImageItem,//数据来源
                    R.layout.service_icon_items,//item的XML实现
                    //动态数组与ImageItem对应的子项
                    new String[]{"service"},
                    //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                    new int[]{R.id.service_btn});
            //添加并且显示
            gridView.setAdapter(saImageItems);
            final ArrayList<HashMap<String, Object>> finalLstImageItem = lstImageItem;
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectService = finalLstImageItem.get(position).get("service").toString();
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
                }
            });*/
        }
    };

    //这里处理传过来的数据
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            RegionModel regionModel1 = new RegionModel();
            switch (re)
            {
                case "上钟":
                    getServiceData("dojsz");
                    break;
                case "加钟":
                    getServiceData("dojaz");
                    break;
                case "减钟":
                    getServiceData("dojdz");
                    break;
                case "下钟":
                    getServiceData("dojxz");
                    break;
                case "退单":
                    getServiceData("dojtd");
                    break;

            }
        }
    };
    private void getServiceData(String service) {
        //{"code":"doklt","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sTH":"301"}}}
        map.clear();
        map.put("code",service);
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sDWID",sXMMC.getsDWID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        resultData = null;
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                //resultData = "{\"code\":\"dojaz\",\"ret\":\"0\",\"msg\":\"加钟成功\"}";
                returnedSData(resultData);
            }
        }).start();*/
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
                alertDialog.dismiss();
                getData();
                msg = jsonObject.getString("msg");
                handler.post(toast);
                if (msg.equals("退单成功"))
                {
                    startActivity(configuration.getIntent(WristbandActivity.this,InformationActivity.class));
                    finish();
                }
            }else {
                alertDialog.dismiss();
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    };
    public void back(){
        startActivity(configuration.getIntent(WristbandActivity.this,InformationActivity.class));
        finish();
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
}
