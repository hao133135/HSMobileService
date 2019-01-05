package com.qindor.hsmobileservice.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.device.PiccManager;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.qindor.hsmobileservice.Utils.Constant;
import com.qindor.hsmobileservice.Utils.HttpUtils;
import com.qindor.hsmobileservice.Utils.LoadingDialog;
import com.qindor.hsmobileservice.Utils.clickUtils;
import com.qindor.hsmobileservice.Utils.zxing.activity.CaptureActivity;

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
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class wristbandServiceActivity extends AppCompatActivity implements View.OnClickListener{
    private Button b1,b2;
    private Configuration configuration;
    private Handler handler;
    private String msg,tec,tecNum;
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
    private PiccManager piccReader;
    private static long lastClickTime,lastClickTime1;
    private Timer timer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wristband_sell_service);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(wristbandServiceActivity.this)
                .setMessage("加载中...")
                .setCancelable(true).create();
        httpUtils = new HttpUtils();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
        piccReader = new PiccManager();
        title.setText("购买服务");
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        isDate = sharedPreferences.getBoolean("isDate",false);
        //tec = sharedPreferences.getString("tec", "");
        //tecNum = sharedPreferences.getString("tecNum", "");
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
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        v1.setOnClickListener(this);
        v2.setOnClickListener(this);
        v3.setOnClickListener(this);
        piccReader.open();
        timer = new Timer();
        timer.schedule(new Task(),0, 1 * 200);
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
        t6.setText("技师扫码");
        /*if (!sharedPreferences.getString("tec","").equals(""))
        {
            t6.setText("技师工号："+sharedPreferences.getString("tec",""));
        }else {
            t6.setText("技师扫码");
        }*/
    }


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

    private void getPData() {
        //{"code":"getjxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        Map<String, Object> map = new HashMap<>();
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

    private void getTData() {
        //{"code":"getjsl","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        Map<String, Object> map = new HashMap<>();
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
    int scan_card = -1;
    int SNLen = -1;

    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        Bundle bundle1 = new Bundle();
        switch (v.getId())
        {
            case R.id.room_wristband_service_btn:
                if(clickUtils.isFastClick()){
                    if(!t5.getText().toString().equals("服务项目")) {
                        sell();
                    }else {
                        msg = "请选择服务项目";
                        handler.post(toast);
                    }
                }

                break;
            case R.id.room_wristband_service_out_btn:
                if(clickUtils.isFastClick()){
                    back();
                }

                break;
            case R.id.service_type:
                if(clickUtils.isFastClick()){
                    timer.cancel();
                    i = new Intent(wristbandServiceActivity.this, ServiceTypeActivity.class);
                    startActivity(i);
                    finish();
                }

                break;
            case R.id.service_project:
                if(clickUtils.isFastClick()){
                    if (t4.getText().toString().equals("服务类型")){
                        msg = "请选择服务类型";
                        handler.post(toast);
                    }else {
                        timer.cancel();
                        i = new Intent(wristbandServiceActivity.this, ServiceProjectActivity.class);
                        bundle1.putSerializable("type", t4.getText().toString().substring(5,t4.getText().toString().length()));
                        i.putExtras(bundle1);
                        startActivity(i);
                        finish();
                    }
                }

                break;
            case R.id.service_technician:
                if(clickUtils.isFastClick()){
                    if (t5.getText().toString().equals("服务项目")){
                        msg = "请选择服务项目";
                        handler.post(toast);
                    }else {
                   /* i = new Intent(wristbandServiceActivity.this, ServiceTechnicianActivity.class);
                    bundle1.putSerializable("pro", t5.getText().toString().substring(5,t5.getText().toString().length()));
                    i.putExtras(bundle1);
                    startActivity(i);
                    finish();*/
                        scanMethod();
                    }
                }

                break;
        }
    }
    public class Task extends TimerTask {
        public void run(){
            byte CardType[] = new byte[2];
            byte Atq[] = new byte[14];
            char SAK = 1;
            byte sak[] = new byte[1];
            sak[0] = (byte) SAK;
            byte SN[] = new byte[10];
            scan_card = piccReader.request(CardType, Atq);
            if(scan_card > 0) {
                SNLen = piccReader.antisel(SN, sak);
                String c = bytesToHexString(SN, SNLen);
                String code1 ="";
                for (int i=0;i<c.length();i+=2)
                {
                    String c1 = c;
                    code1 = c1.substring(i,i+2)+code1;
                }
                String c1 = code1.substring(2,code1.length());
                String x = String.valueOf(Integer.parseInt(c1,16));
                if (x.length()<8)
                {
                    for(int i = x.length();i<8;i++) {
                        x = "0" + x;
                    }
                }
                tecNum = x;
               /* msg = c+"/"+x;
                handler.post(toast);*/
               /* msg = x;
                handler.post(toast);*/

                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime1) >= 2000) {
                    if (t5.getText().toString().equals("服务项目")){
                        msg = "请选择服务项目";
                        handler.post(toast);
                    }else {
                        if(tecNum!=null||!tecNum.equals("")) {
                            handler.post(setSData);
                        }
                    }
                }
                lastClickTime1 = curClickTime;
                //handler.post(setSData);
            }
        }
    }
    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        if (len <= 0) {
            return "";
        }
        for (int i = 0; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    private void sell() {
        //{"code":"dodxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sXM":"推背","sJS":"A001","sTH":"301",}}
        Map<String, Object> map = new HashMap<>();
        map.put("code","dodxm");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sWD",roomModel.getModels().get(0).getsWDBH());
             data.put("sXM",t5.getText().toString().substring(5,t5.getText().toString().length()));
            if(!"技师扫码".equals(t6.getText().toString())){
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
                Intent i = new Intent(wristbandServiceActivity.this, Room_wristband_Activity.class);
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
        timer.cancel();
        Intent i = new Intent(wristbandServiceActivity.this, Information_wristband_Activity.class);
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

    private void scanMethod() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(wristbandServiceActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(wristbandServiceActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if ( resultCode >0) {
            Bundle bundle = data.getExtras();
            tecNum = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            handler.post(setSData);
        }
    }
    Runnable setSData = new Runnable() {
        @Override
        public void run() {
            t6.setText("技师工号："+tecNum);
        }
    };


    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private boolean isScaning = false;
    private int soundid;
    private String barcodeStr;
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            isScaning = false;
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            //showScanResult.setText("");
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            android.util.Log.i("debug", "----codetype--" + temp);
            tecNum = new String(barcode, 0, barcodelen);
            if (t5.getText().toString().equals("服务项目")){
                msg = "请选择服务项目";
                handler.post(toast);
            }else {
                   /* i = new Intent(wristbandServiceActivity.this, ServiceTechnicianActivity.class);
                    bundle1.putSerializable("pro", t5.getText().toString().substring(5,t5.getText().toString().length()));
                    i.putExtras(bundle1);
                    startActivity(i);
                    finish();*/
                handler.post(setSData);
            }
            //showScanResult.setText(barcodeStr);

        }

    };
    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode( 0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(mScanManager != null) {
            mScanManager.stopDecode();
            isScaning = false;
        }
        unregisterReceiver(mScanReceiver);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
        //showScanResult.setText("");
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if(value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver, filter);
    }
}
