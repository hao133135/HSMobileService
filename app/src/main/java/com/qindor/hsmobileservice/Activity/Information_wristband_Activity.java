package com.qindor.hsmobileservice.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.device.PiccManager;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.RoomListAdpater;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.Model.RoomAdpaterModel;
import com.qindor.hsmobileservice.Model.RoomModel;
import com.qindor.hsmobileservice.Model.RoomsModel;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Information_wristband_Activity extends AppCompatActivity implements View.OnClickListener {

    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private InformationModel informationModel = null;
    private RoomsModel rmodels;
    private String resultData,resultData1,resultData2;
    protected NfcAdapter nfcAdapter;
    protected MifareClassic mifareClassic;
    private LoadingDialog dialog1;
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    private RoomListAdpater roomListAdpater = null;
    private ListView listView;
    private TextView textView,title,sbtn,outBtn;
    private String msg,userid,sKey;
    private Timer timer,timer1,timer2;
    private int code=0;
    private PiccManager piccReader;
    int scan_card = -1;
    int SNLen = -1;
    private static long lastClickTime1;
    int state = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_information_wristband);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(Information_wristband_Activity.this)
                .setMessage("加载中...")
                .setCancelable(true).create();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        sbtn = findViewById(R.id.information_wristband_scanner);
        outBtn = findViewById(R.id.information_wristband_wristband_off);
        listView = findViewById(R.id.room_information_wristband_list_view);
        textView = findViewById(R.id.room_information_wristband_number);
        title = findViewById(R.id.hotspring_title);
        title.setText("台号");
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));   String temp = sharedPreferences.getString("informationModel", "");
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            informationModel = (InformationModel) ois.readObject();
            title.setText("台号："+informationModel.getsTBH());
            //textView.setText("台号："+informationModel.getsTBH());
        } catch (Exception e) {
            msg=e.toString();
            handler.post(toast);
        }
        rmodels =new RoomsModel();
        httpUtils = new HttpUtils();
        configuration = new Configuration();
        handler = new Handler();
        sbtn.setOnClickListener(this);
        outBtn.setOnClickListener(this);
        getData();
       // checkNfc();
        onNewIntent(getIntent());
        timer = new Timer();
        piccReader = new PiccManager();
        piccReader.open();
        timer1 = new Timer();
        timer1.schedule(new Task(), 1 * 200);
        state = 1;
       /* timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getData();
            }
        }, 15000, 15000);*/

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
                rmodels.setCode(x);
               /* msg = c+"/"+x;
                handler.post(toast);*/
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime1) >= 2000) {
                    if(code == 0 )
                    {
                        scan(x);
                    }else if(code ==1 )
                    {
                        outDialog.dismiss();
                        out(x);
                    }
                }
                lastClickTime1 = curClickTime;
               /* tecNum = x;
               *//* msg = x;
                handler.post(toast);*//*
                piccReader.close();
                if(tecNum!=null||!tecNum.equals("")) {
                    handler.post(setSData);
                }*/
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
    private void getData() {
        //{"code":"gettxx","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sTBH":"301"}}
        Map<String, Object> map = new HashMap<>();
        map.put("code","gettxx");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sTBH",informationModel.getsTBH());
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
                returnedData(resultData);
            }
        });
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                //resultData = "{\"code\":\"gettxx\",\"ret\":\"0\",\"msg\":[{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"20.00\",\"fSL\":\"1.00\",\"fXMJE\":\"20.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"张三\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:41:41\",\"sDateYMDHMSXZ\":\"2018-08-11 16:41:41\",\"iZSC\":\"60\",\"iSY\":\"60\"},{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"25.00\",\"fSL\":\"1.00\",\"fXMJE\":\"25.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"李四\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:42:42\",\"sDateYMDHMSXZ\":\"2018-08-11 16:42:42\",\"iZSC\":\"60\",\"iSY\":\"60\"}]}";
                returnedData(resultData);
            }
        }).start();*/
    }

    private void returnedData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                if(rmodels.getModels()!=null) {
                    rmodels.getModels().clear();
                }
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    RoomModel roomModel = new RoomModel(jsonObject1.getString("sDWID"),jsonObject1.getString("sWDBH"),jsonObject1.getString("sXMMC"),jsonObject1.getString("fXMDJ"),jsonObject1.getString("fSL"),jsonObject1.getString("fXMJE"),jsonObject1.getString("sJSGH"),jsonObject1.getString("sJSXM"),jsonObject1.getString("sZLX"),jsonObject1.getString("sDateYMDHMSSZ"),jsonObject1.getString("sDateYMDHMSXZ"),jsonObject1.getString("iZSC"),jsonObject1.getString("iSY"),jsonObject1.getString("sZT"),jsonObject1.getString("iZF"));
                    roomModel.setsTBH(informationModel.getsTBH());
                    rmodels.getModels().add(roomModel);
                }
                handler.post(setData);
            }else
            {
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // RoomListAdpater roomListAdpater = new RoomListAdpater<>(InformationActivity2.this,informationModel,R.layout.room_information_items);
    }
    Runnable setData = new Runnable() {
        @Override
        public void run() {
            if (rmodels.getModels().size()!=0) {
                loadView(rmodels.getModels());
            }else
            {
                startActivity(configuration.getIntent(Information_wristband_Activity.this,Room_wristband_Activity.class));
                finish();
            }
        }
    };
    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    };
    public void loadView(final List<RoomModel> roomModels){
        final List<RoomAdpaterModel> list = new ArrayList<>();
        String sd="";
        for (RoomModel r : roomModels)
        {
            if (!r.getsZLX().equals("商品")) {
                if (r.getsZLX().equals("首钟")||r.getsZLX().equals("加钟")) {
                    list.add(new RoomAdpaterModel(r.getsWDBH(), r.getsXMMC(), r.getsDateYMDHMSSZ(), 0,r.getsDWID(),r.getsZT()));
                }else {
                    sd = r.getsWDBH();
                    list.add(new RoomAdpaterModel(r.getsWDBH(), r.getsXMMC(), r.getsDateYMDHMSSZ(), 1,r.getsDWID(),r.getsZT()));
                }
            }
        }
        for  ( int  i  =   0 ; i  <  list.size()  -   1 ; i ++ )  {
            for  ( int  j  =  list.size()  -   1 ; j  >  i; j -- )  {
                if  (list.get(j).getsJSGH().equals(list.get(i).getsJSGH()))  {
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date time1 = formatter.parse(list.get(j).getsDateYMDHMSSZ());
                        Date time2 = formatter.parse(list.get(i).getsDateYMDHMSSZ());
                        if (time1.before(time2)) {
                            list.remove(j);
                        }else {
                            list.remove(i);
                        }
                    }catch (Exception e){
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            }
        }
        for (int i =0;i<list.size();i++)
        {
           if(list.get(i).getsJSGH().equals(sd))
           {
               list.get(i).setState(1);
           }
        }
        roomListAdpater = new RoomListAdpater(Information_wristband_Activity.this,list,R.layout.room_information_items);
        listView.setAdapter(roomListAdpater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<RoomModel> rs = rmodels.getModels();
                List<RoomModel> r = new ArrayList<>();
                for (int i =0;i<rs.size();i++)
                {
                    if (rs.get(i).getsWDBH().equals(list.get(position).getsJSGH()))
                    {
                        r.add(rs.get(i));
                    }
                }
                rmodels.getModels().clear();
                rmodels.setModels(r);
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(rmodels);//把对象写到流里
                    String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                    editor.putString("rooms", temp);
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent i = new Intent(Information_wristband_Activity.this, WristbandActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.information_wristband_scanner:
                if(clickUtils.isFastClick()){
                    code=0;
                    scanMethod();
                }
                break;
            case R.id.information_wristband_wristband_off:
                if(clickUtils.isFastClick()){
                    openInformation();
                }
                break;
        }
    }


    public void back(){
        if(state == 1)
        {
            timer1.cancel();
        }else if(state ==2)
        {
            timer2.cancel();
        }
        startActivity(configuration.getIntent(Information_wristband_Activity.this,Room_wristband_Activity.class));
        finish();
    }
    /**.
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

    protected void checkNfc() {
        if (!nfcAdapter.isEnabled()) {
            startActivity(new Intent(
                    android.provider.Settings.ACTION_NFC_SETTINGS));
        }
         try {
        nfcAdapter.wait();
         } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
         msg=e.toString();
         handler.post(toast);
        }
    }
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
        } else {
            if (tag != null) {
                // 获取卡id
                byte[] id = tag.getId();
                String c =  ByteArrayToHexString(id);
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
                code = 0;
                rmodels.setCode(x);
                handler.post(setSData);
            }

        }
    }
    protected String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F" };
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private void scanMethod() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(Information_wristband_Activity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(Information_wristband_Activity.this, CaptureActivity.class);
        startActivityForResult(intent, 1);
    }
    private void outScanMethod() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(Information_wristband_Activity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(Information_wristband_Activity.this, CaptureActivity.class);
        startActivityForResult(intent, 2);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if ( resultCode >0) {
            Bundle bundle = data.getExtras();
            rmodels.setCode(bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN));
            handler.post(setSData);
        }
    }


    Runnable setSData = new Runnable() {
        @Override
        public void run() {
            //{"code":"getwdh","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sCD":"012489"}}
           /* map.clear();
            map.put("code","getwdh");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sCD",rmodels.getCode());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.put("msg",data);
            resultData=null;
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
                    //returnedData(resultData);
                    resultData1 = response.body().string();
                    if(code == 0 )
                    {
                        scan(resultData1);
                    }else if(code ==1 )
                    {
                        out(resultData1);
                    }
                }
            });*/
            if(code == 0 )
            {
                scan(rmodels.getCode());
            }else if(code ==1 )
            {
                out(rmodels.getCode());
            }
        }
    };
    public void scan(String resultSData){
        List<RoomModel> rs = rmodels.getModels();
        List<RoomModel> r = new ArrayList<>();
        for (int i=0;i<rs.size();i++)
        {
            if (rs.get(i).getsWDBH().equals(resultSData))
            {
                r.add(rs.get(i));
            }
        }
        if(r.size()!=0) {
            rmodels.getModels().clear();
            rmodels.setModels(r);
            SharedPreferences sp = getSharedPreferences("config", 0);
            SharedPreferences.Editor editor = sp.edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(rmodels);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                editor.putString("rooms", temp);
                editor.putString("pro","");
                editor.putString("tec","");
                editor.putString("type","");
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(state == 1)
            {
                timer1.cancel();
            }else if(state ==2)
            {
                timer2.cancel();
            }
            Intent i = new Intent(Information_wristband_Activity.this, WristbandActivity.class);
            startActivity(i);
            finish();
        }
        else
        {
            RoomModel roomModel = new RoomModel();
            roomModel.setsWDBH(resultSData);
            roomModel.setsTBH(informationModel.getsTBH());
            r.add(roomModel);
            rmodels.setModels(r);
            SharedPreferences sp = getSharedPreferences("config", 0);
            SharedPreferences.Editor editor = sp.edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(rmodels);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                editor.putString("rooms", temp);
                editor.putString("pro","");
                editor.putString("tec","");
                editor.putString("type","");
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(state == 1)
            {
                timer1.cancel();
            }else if(state ==2)
            {
                timer2.cancel();
            }
            Intent i = new Intent(Information_wristband_Activity.this, wristbandServiceActivity.class);
            startActivity(i);
            finish();
        }
    }
    public void out(String resultsData){

        Map<String, Object> map = new HashMap<>();
        map.put("code","doklt");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sWD",resultsData);
            data.put("sTH",informationModel.getsTBH());
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
                resultData2 = response.body().string();
                returnedOutData(resultData2);
            }
        });
    }
    private void openInformation() {
        code=1;
        timer2 = new Timer();
        timer2.schedule(new Task(), 0,1 * 200);
        state = 2;
        outDialog =  new AlertDialog.Builder(Information_wristband_Activity.this)
                .setTitle("离台提示")
                .setMessage("\n是否离台！\t\n1.使用黄色物理键扫码离台\n2.点击确认进行摄像头扫码离台\n3.点击取消返回上一界面\n")
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
                                code=0;
                                back();
                                dialog.dismiss();
                            }
                        }).show();


    }
    private void returnedOutData(String resultData) {
        JSONObject j = null;
        if(state == 1)
        {
            timer1.cancel();
        }else if(state ==2)
        {
            timer2.cancel();
        }
        try {
            j = new JSONObject(resultData);
            String ret1 = j.getString("ret");
            if (ret1.equals("0")) {
                msg = j.getString("msg");
                handler.post(toast);
                startActivity(configuration.getIntent(Information_wristband_Activity.this,Room_wristband_Activity.class));
                finish();
            }else {
                msg = j.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            msg = e.toString();
            handler.post(toast);
        }
    }
    private  AlertDialog outDialog;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private boolean isScaning = false;
    private int soundid;
    private String barcodeStr;
    private static long lastClickTime;
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
            barcodeStr = new String(barcode, 0, barcodelen);
            rmodels.setCode(barcodeStr);
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= 2000) {
                if(code == 0 )
                {
                    scan(barcodeStr);
                }else if(code ==1 )
                {
                    out(barcodeStr);
                }
            }
            lastClickTime = curClickTime;



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
