package com.qindor.hsmobileservice.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.InformactionAdpater;
import com.qindor.hsmobileservice.Adpater.MySimpleAdapter;
import com.qindor.hsmobileservice.Adpater.RoomDialogAdpater;
import com.qindor.hsmobileservice.Adpater.RoomQueryAdpater;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.Model.RegionModel;
import com.qindor.hsmobileservice.Model.RoomModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Configuration;
import com.qindor.hsmobileservice.Utils.Constant;
import com.qindor.hsmobileservice.Utils.HttpUtils;
import com.qindor.hsmobileservice.Utils.LoadingDialog;
import com.qindor.hsmobileservice.Utils.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

public class RoomActivity extends AppCompatActivity implements View.OnClickListener {
    private List<RoomModel> roomModels = new ArrayList<>();
    private RegionModel regionModel;
    private View v1,v2;
    private TextView regionDialogBtn,queryBtn,swicthBtn,region,title;
    private GridView gridview;
    private ListView listView;
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private Map<String, Object> map;
    private List<InformationModel> informationModels ;
    private boolean sbool;
    private List<String> sslist = new ArrayList<>();
    private List<String> slist = new ArrayList<>();
    private ArrayAdapter<String> adapter,adapter1;
    private Spinner regionSpinner,qSpinner;
    private Button regionBtn,outBtn,qbtn,qoutbtn;
    private String resultData,userid,sKey;
    private Map rmap;
    private LayoutInflater inflater;
    private AlertDialog alertDialog;
    private String re,code,th,msg,regionName;
    private MySimpleAdapter saImageItems = null;
    private InformactionAdpater informactionAdpater = null;
    private int REQUEST_CODE = 0x01;
    private InformationModel imodel;
    private boolean isDate = false,isdialog = false;
    private int day=0;
    private String re1;
    private LoadingDialog dialog1;
    private RoomDialogAdpater roomDialogAdpater;
    private RoomQueryAdpater roomQueryAdpater;
    private ListView dlistView,qlistView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(RoomActivity.this)
                .setMessage("加载中...")
                .setCancelable(true).create();
        gridview = (GridView) findViewById(R.id.GridView);
        listView = findViewById(R.id.room_list_views);
        regionDialogBtn = findViewById(R.id.room_view_region);
        queryBtn = findViewById(R.id.room_view_query);
        swicthBtn = findViewById(R.id.room_view_switch_btn);
        region = findViewById(R.id.room_region);
        title = findViewById(R.id.hotspring_title);
        title.setText("台区域");
        v1 = findViewById(R.id.room_icon_view);
        v2 = findViewById(R.id.room_list_view);
        regionModel = new RegionModel();
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        regionName = sharedPreferences.getString("regionName","");
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        httpUtils = new HttpUtils();
        imodel = new InformationModel();
        configuration = new Configuration();
        handler = new Handler();
        map = new HashMap<String, Object>();
        informationModels = new ArrayList<>();
        sbool = false;
        swicthView();
        regionDialogBtn.setOnClickListener(this);
        queryBtn.setOnClickListener(this);
        swicthBtn.setOnClickListener(this);
         dialog1.show();
         getRegionData();
        Time t=new Time();
        t.setToNow();
        day = t.monthDay;
        if(day-sharedPreferences.getInt("day",0)!=0){
            isDate=true;
        }


    }

    private void getData(RegionModel regionModel) {
        //{"code":"gettls","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sQYH":"001002"}}
        map.clear();
        map.put("code","gettls");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sQYH",regionModel.getsQYH());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        resultData = null;
        OkHttpClient mOkHttpClient = new OkHttpClient();//创建OkHttpClient对象。
        Request request = new Request.Builder()//创建Request 对象。
                .url(  "http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/spring")
                .post(httpUtils.baseOkHttp(baseModel,userid,sKey,map).build())//传递请求体
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog1.dismiss();
                msg=e.toString();
                handler.post(toast);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                resultData = response.body().string();
                returnedValue(resultData);
            }
        });
    }

    private void returnedValue(String resultData) {
        //注：sTBH台编号；sTMC桑拿房；sTZT台状态；sRTS入台人数；sNTS台容纳人数
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                informationModels.clear();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    String s = jsonObject1.getString("sTZT");
                    if (s.equals("占用"))
                    {
                        s= "占";
                    }else
                    {
                        s ="空";
                    }
                    informationModels.add(new InformationModel(jsonObject1.getString("sTBH"),jsonObject1.getString("sTMC"),s,jsonObject1.getString("sRTS"),jsonObject1.getString("sNTS")));
                }
                handler.post(setData);
            }
        } catch (JSONException e) {
            dialog1.dismiss();
            msg = e.toString();
            handler.post(toast);
        }
    }
    Runnable setData = new Runnable() {
        @Override
        public void run() {
            loadView(informationModels);
        }
    };

    private void getRegionData() {
        //{"code":"gettqy","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        map.clear();
        map.put("code","gettqy");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        resultData = null;
        OkHttpClient mOkHttpClient = new OkHttpClient();//创建OkHttpClient对象。
        Request request = new Request.Builder()//创建Request 对象。
                .url(  "http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/spring")
                .post(httpUtils.baseOkHttp(baseModel,userid,sKey,map).build())//传递请求体
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog1.dismiss();
                msg=e.toString();
                handler.post(toast);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                resultData = response.body().string();
                returnedData(resultData);
            }
        });
        //resultData = "{\"code\":\"gettqy\",\"ret\":\"0\",\"msg\":[{\"sQYH\":\"001001\",\"sQYM\":\"按摩区\"},{\"sQYH\":\"001002\",\"sQYM\":\"足疗区\"}]}";
    }

    private void returnedData(String resultData) {
        List<String> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                rmap = new HashMap();
                sslist.clear();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    rmap.put(jsonObject1.getString("sQYH"),jsonObject1.getString("sQYM"));
                    sslist.add(jsonObject1.getString("sQYM"));
                }
                if(regionName!=""&&rmap!=null)
                {
                    re = regionName;
                    myHandler.sendEmptyMessageDelayed(0, 1000);
                }else {
                    dialog1.dismiss();
                }
            }
        } catch (JSONException e) {
            dialog1.dismiss();
            msg = e.toString();
            handler.post(toast);
        }
    }

    public void loadView(final List<InformationModel> informationModelss){
        dialog1.dismiss();
        int length = informationModelss.size();
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("sTBH", informationModelss.get(i).getsTBH());
            map.put("sXMMC", informationModelss.get(i).getsTMC());
            map.put("fSL", informationModelss.get(i).getsRTS()+"/"+informationModelss.get(i).getsNTS());
            map.put("state",informationModelss.get(i).getsTZT());
            lstImageItem.add(map);
        }

       /*//生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new MySimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.room_icon_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sTBH", "sXMMC","fSL","state"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.room_icon_sTBH, R.id.room_icon_sXMMC,R.id.room_icon_fSL,R.id.room_icon_state});
        //添加并且显示*/
       saImageItems = new MySimpleAdapter(this,informationModelss,R.layout.room_icon_items);
        gridview.setAdapter(saImageItems);
        //添加消息处理

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isOpen(informationModelss.get(position)))
                {
                    saveData(informationModelss.get(position));
                    startActivity(configuration.getIntent(RoomActivity.this,InformationActivity.class));
                    finish();
                }else {
                    th = informationModelss.get(position).getsTBH();
                    imodel =informationModelss.get(position);
                    openInformation();
                }
            }
        });
        informactionAdpater = new InformactionAdpater(this,informationModelss,R.layout.room_list_items);
        listView.setAdapter(informactionAdpater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isOpen(informationModelss.get(position)))
                {
                    saveData(informationModelss.get(position));
                    startActivity(configuration.getIntent(RoomActivity.this,InformationActivity.class));
                    finish();
                }else {
                    th = informationModelss.get(position).getsTBH();
                    imodel =informationModelss.get(position);
                    openInformation();
                }
            }
        });
    }

    private void openInformation() {
        new AlertDialog.Builder(RoomActivity.this)
                .setTitle("开台提示")
                .setMessage("\n\t\t\t\t是否开台！\t\n")
                .setNegativeButton("确认",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                scanMethod();
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton("取消",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }
    private void scanMethod() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(RoomActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(RoomActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if ( resultCode >0) {
            Bundle bundle = data.getExtras();
            code=bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            handler.post(getIData);
        }
    }


    Runnable getIData = new Runnable() {
        @Override
        public void run() {
            //{"code":"dotkt","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sTH":"301"}}
            map.clear();
            map.put("code","dotkt");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sWD",code);
                data.put("sTH",th);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.put("msg",data);
            resultData = null;
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
                    //returnedData(resultData);
                    try {
                        JSONObject jsonObject = new JSONObject(resultData);
                        if (jsonObject.getString("ret").equals("0"))
                        {
                            handler.post(toInfo);
                        }
                        msg = jsonObject.getString("msg");
                        handler.post(toast);
                    } catch (JSONException e) {
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            });
           /* new Thread(new Runnable() {
                @Override
                public void run() {
                    resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                    try {
                        JSONObject jsonObject = new JSONObject(resultData);
                        msg = jsonObject.getString("msg");
                        handler.post(toast);
                        handler.post(toInfo);
                    } catch (JSONException e) {
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            }).start();*/

            //resultData = httpUtils.baseHttp(InformationActivity.this,baseModel,"spring",map);
            //resultData = "{\"code\":\"dotkt \",\"ret\":\"0\",\"msg\":\"开台成功\"}";

        }
    };
    Runnable toInfo = new Runnable() {
        @Override
        public void run() {
            saveData(imodel);
            startActivity(configuration.getIntent(RoomActivity.this,InformationActivity.class));
            finish();
        }
    };
    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    };

    private boolean isOpen(InformationModel informationModel) {
        if(informationModel.getsRTS().equals("0")){
            return false;
        }else {
            return true;
        }
    }


    public void saveData(InformationModel informationModel){
        SharedPreferences sp=getSharedPreferences("config",0);
        SharedPreferences.Editor editor=sp.edit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(informationModel);//把对象写到流里
            String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            editor.putString("informationModel", temp);
            editor.putBoolean("isDate",isDate);
            editor.putString("regionName",re);
            if (isDate){
                editor.putBoolean("isDate",isDate);
                editor.putInt("day",day);
                isDate=false;
            }
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showSetDeBugDialog() {
        AlertDialog.Builder setDeBugDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.information_dialog, null);
        outBtn = dialogView.findViewById(R.id.information_dialog_out_btn);
        dlistView = dialogView.findViewById(R.id.room_dialog_list);
        setDeBugDialog = new AlertDialog.Builder(this);
        setDeBugDialog.setView(dialogView);
        alertDialog = setDeBugDialog.create();
        alertDialog.show();
        handler.post(setListData);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        outBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }
    Runnable setListData = new Runnable() {
        @Override
        public void run() {
            roomDialogAdpater = new RoomDialogAdpater(RoomActivity.this,sslist,R.layout.service_room_icon);
            dlistView.setAdapter(roomDialogAdpater);
            dlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    re = sslist.get(position);
                    alertDialog.dismiss();
                    myHandler.sendEmptyMessageDelayed(0, 1000);
                }
            });
        }
    };
    private void showSetQueryDialog() {
        AlertDialog.Builder setDeBugDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.room_query_dialog, null);
        qoutbtn = dialogView.findViewById(R.id.query_dialog_out_btn);
        //new QSpinnerTask().execute();
        qlistView =  dialogView.findViewById(R.id.query_dialog_list);
        setDeBugDialog = new AlertDialog.Builder(this);
        setDeBugDialog.setView(dialogView);
        alertDialog = setDeBugDialog.create();
        alertDialog.show();
        handler.post(setQueryListData);
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        qoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.show();
                getRegionData();
                alertDialog.dismiss();
            }
        });

    }
    Runnable setQueryListData = new Runnable() {
        @Override
        public void run() {
            slist.clear();
            for (InformationModel i : informationModels) {
                slist.add(i.getsTBH());
            }
            roomQueryAdpater = new RoomQueryAdpater(RoomActivity.this,slist,R.layout.service_query_icon);
            qlistView.setAdapter(roomQueryAdpater);
            qlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    re1 = slist.get(position);
                    alertDialog.dismiss();
                    myQHandler.sendEmptyMessageDelayed(0, 1000);
                }
            });
        }
    };
    //这里处理传过来的数据
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            RegionModel regionModel1 = new RegionModel();
            for(Object key : rmap.keySet()){
                String value = (String) rmap.get(key);
                if(value.equals(re))
                {
                    regionModel1.setsQYH(key.toString());
                }
            }
            if (regionModel1!=null)
            {
                //region.setText("台区域："+re);
                title.setText("台区域："+re);
                getData(regionModel1);
            }
        }
    };
    private Handler myQHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            List<InformationModel> is = new ArrayList<>();
            is.clear();
            if(re1!=null)
            {
                for(InformationModel i : informationModels)
                {
                    if (i.getsTBH().equals(re1))
                    {
                        is.add(i);
                    }
                }
                re1=null;
            }
            loadView(is);
        }
    };
    public void swicthView(){
        if (sbool)
        {
            swicthBtn.setText("视图");
            v2.setVisibility(View.VISIBLE);
            v1.setVisibility(View.GONE);
            sbool = false;
        }else {
            swicthBtn.setText("列表");
            v1.setVisibility(View.VISIBLE);
            v2.setVisibility(View.GONE);
            sbool = true;
        }
    }
    public void back(){
        startActivity(configuration.getIntent(RoomActivity.this,LoginActivity.class));
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

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.room_view_region:
                showSetDeBugDialog();
                break;
            case R.id.room_view_query:
                if (informationModels.size()!=0) {
                    showSetQueryDialog();
                }else {
                    msg="请选择区域";
                    handler.post(toast);
                }
                break;
            case R.id.room_view_switch_btn:
                swicthView();
                break;
        }
    }

}
