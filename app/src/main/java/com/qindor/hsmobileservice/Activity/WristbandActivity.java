package com.qindor.hsmobileservice.Activity;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.WristbandAdpater;
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

public class WristbandActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private Map<String, Object> map;
    private String resultData,re,code;
    private RoomsModel roomModel;
    private WristbandAdpater wristbandAdpater;
    private ListView listView;
    private LayoutInflater inflater;
    private ProjectAndPlistModel projectAndPlistModel;
    private Spinner serviceSpinner,tSpinner;
    private Button regionBtn,outBtn,sbtn,sobtn,pSpinner;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> adapter;
    private List<String> sslist = new ArrayList<>(),tslist=new ArrayList<>();
    private String msg,sout,userid,sKey;
    private TextView open,sell,t1,p1,t2,p2,wristband,title;
    private List<TechnicianModel> technicianModels;
    private List<RoomModel> roomModels;
    private boolean isDate=false;
    private ImageButton backBtn;
    private InformationModel informationModel;
    private LoadingDialog dialog1;
    private LoadingDialog.Builder builder1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_wristband);
        init();
    }

    private void init() {
        builder1=new LoadingDialog.Builder(WristbandActivity.this)
                .setMessage("加载中...")
                .setCancelable(false);
        dialog1=builder1.create();
        listView = findViewById(R.id.room_wristband_list_view);
        open = findViewById(R.id.room_wristband_open);
        sell = findViewById(R.id.room_wristband_sell);
        wristband = findViewById(R.id.room_wristband_wr);
        backBtn = findViewById(R.id.sell_page_back_btn);
        title = findViewById(R.id.hotspring_title);
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
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
        Bundle bundle =this.getIntent().getExtras();
        roomModel = (RoomsModel) bundle.get("rooms");
        title.setText("腕带号："+roomModel.getModels().get(0).getsWDBH());
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
        handler.post(setData);
        if(isDate){
            getPData();
        }else {
            String temp = sharedPreferences.getString("projectAndPlistModel", "");
            ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
            try {
                ObjectInputStream ois = new ObjectInputStream(bais);
                projectAndPlistModel = (ProjectAndPlistModel) ois.readObject();
            } catch (Exception e) {
                msg=e.toString();
                handler.post(toast);
            }
        }

        getTData();
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
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("rooms",roomModel);
                i.putExtras(bundle1);
                startActivity(i);
                finish();
                break;
            case R.id.sell_page_back_btn:
                back();
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
        dialog1.show();
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"getjsl \",\"ret\":\"0\",\"msg\":[{\"sGH\":\"A001\",\"sXM\":\"张三\",\"sBM\":\"桑拿部\",\"sGZ\":\"桑拿技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"},{\"sGH\":\"B001\",\"sXM\":\"李四\",\"sBM\":\"足浴部\",\"sGZ\":\"足浴技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"}]}";
        dialog1.dismiss();
        returnedTData(resultData);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
                resultData = "{\"code\":\"getjsl \",\"ret\":\"0\",\"msg\":[{\"sGH\":\"A001\",\"sXM\":\"张三\",\"sBM\":\"桑拿部\",\"sGZ\":\"桑拿技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"},{\"sGH\":\"B001\",\"sXM\":\"李四\",\"sBM\":\"足浴部\",\"sGZ\":\"足浴技师\",\"sJB\":\"高级\",\"sZT\":\"空闲\",\"sXB\":\"女\"}]}";
                returnedTData(resultData);
            }
        }).start();*/
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
        dialog1.show();
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"getjxm\",\"ret\":\"0\",\"msg\":[{\"sXMLX\":\"按摩类\",\"sXMMC\":\"推背\",\"iSZZC\":\"30\",\"fSZDJ\":\"80\",\"iJZZC\":\"15\",\"fJZDJ\":\"30\"},{\"sXMLX\":\"按摩类\",\"sXMMC\":\"按摩\",\"iSZZC\":\"30\",\"fSZDJ\":\"100\",\"iJZZC\":\"15\",\"fJZDJ\":\"50\"},{\"sXMLX\":\"洗浴类\",\"sXMMC\":\"足疗\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"}]}";
        dialog1.dismiss();
        returnedPData(resultData);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
                resultData = "{\"code\":\"getjxm\",\"ret\":\"0\",\"msg\":[{\"sXMLX\":\"按摩类\",\"sXMMC\":\"推背\",\"iSZZC\":\"30\",\"fSZDJ\":\"80\",\"iJZZC\":\"15\",\"fJZDJ\":\"30\"},{\"sXMLX\":\"按摩类\",\"sXMMC\":\"按摩\",\"iSZZC\":\"30\",\"fSZDJ\":\"100\",\"iJZZC\":\"15\",\"fJZDJ\":\"50\"},{\"sXMLX\":\"洗浴类\",\"sXMMC\":\"足疗\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"}]}";
                returnedPData(resultData);
            }
        }).start();*/
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
        dialog1.show();
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"gettxx\",\"ret\":\"0\",\"msg\":[{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"20.00\",\"fSL\":\"1.00\",\"fXMJE\":\"20.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"张三\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:41:41\",\"sDateYMDHMSXZ\":\"2018-08-11 16:41:41\",\"iZSC\":\"60\",\"iSY\":\"60\"},{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"25.00\",\"fSL\":\"1.00\",\"fXMJE\":\"25.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"李四\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:42:42\",\"sDateYMDHMSXZ\":\"2018-08-11 16:42:42\",\"iZSC\":\"60\",\"iSY\":\"60\"}]}";
        dialog1.dismiss();
        returnedData(resultData);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
                //resultData = "{\"code\":\"gettxx\",\"ret\":\"0\",\"msg\":[{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"20.00\",\"fSL\":\"1.00\",\"fXMJE\":\"20.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"张三\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:41:41\",\"sDateYMDHMSXZ\":\"2018-08-11 16:41:41\",\"iZSC\":\"60\",\"iSY\":\"60\"},{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"25.00\",\"fSL\":\"1.00\",\"fXMJE\":\"25.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"李四\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:42:42\",\"sDateYMDHMSXZ\":\"2018-08-11 16:42:42\",\"iZSC\":\"60\",\"iSY\":\"60\"}]}";
                returnedData(resultData);
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
           dialog1.show();
           //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
           //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
           resultData = "{\"code\":\"dodxm\",\"ret\":\"0\",\"msg\":\"点服务成功\"}";
           dialog1.dismiss();
           returnedSData(resultData);
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
                    RoomModel roomModel = new RoomModel(jsonObject1.getString("sDWID"),jsonObject1.getString("sWDBH"),jsonObject1.getString("sXMMC"),jsonObject1.getString("fXMDJ"),jsonObject1.getString("fSL"),jsonObject1.getString("fXMJE"),jsonObject1.getString("sJSGH"),jsonObject1.getString("sJSXM"),jsonObject1.getString("sZLX"),jsonObject1.getString("sDateYMDHMSSZ"),jsonObject1.getString("sDateYMDHMSXZ"),jsonObject1.getString("iZSC"),jsonObject1.getString("iSY"));
                    roomModels.add(roomModel);
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
                showSetDeBugDialog(roomModels.get(position).getsXMMC());
            }
        });
    }
    private void showSetDeBugDialog(String sXMMC) {
        AlertDialog.Builder setDeBugDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.wristband_dialog, null);
        serviceSpinner = dialogView.findViewById(R.id.room_wristband_service);
        regionBtn = dialogView.findViewById(R.id.wristband_dialog_btn);
        outBtn = dialogView.findViewById(R.id.wristband_dialog_out_btn);
        new SpinnerTask().execute();
        setDeBugDialog = new AlertDialog.Builder(this);
        setDeBugDialog.setView(dialogView);
        alertDialog = setDeBugDialog.create();
        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        regionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                re = serviceSpinner.getSelectedItem().toString();
                myHandler.sendEmptyMessageDelayed(0, 1000);
                alertDialog.dismiss();
            }
        });
        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }
    class SpinnerTask extends AsyncTask<Object, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Object... params) {
            return sslist;
        }
        @Override
        protected void onPostExecute(List<String> result) {
            // TODO Auto-generated method stub
            spinnerClick();
        }
    }
    private void spinnerClick() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sslist);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        serviceSpinner.setAdapter(adapter);
    }
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
            data.put("sDWID",roomModel.getModels().get(0).getsDWID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        resultData = null;
        dialog1.show();
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"dojaz\",\"ret\":\"0\",\"msg\":\"加钟成功\"}";
        dialog1.dismiss();
        returnedSData(resultData);
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
                resultData = "{\"code\":\"doklt\",\"ret\":\"0\",\"msg\":\"加钟成功\"}";
                returnedSData(resultData);
            }
        }).start();*/
    }
    private void returnedSData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                getData();
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
