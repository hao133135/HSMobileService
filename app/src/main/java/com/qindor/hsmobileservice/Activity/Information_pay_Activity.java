package com.qindor.hsmobileservice.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.device.PiccManager;
import android.device.PrinterManager;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.RoomListAdpater;
import com.qindor.hsmobileservice.Adpater.WristbandDialogAdpater;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.Model.ProjectAndPlistModel;
import com.qindor.hsmobileservice.Model.ProjectModel;
import com.qindor.hsmobileservice.Model.RegionModel;
import com.qindor.hsmobileservice.Model.RoomAdpaterModel;
import com.qindor.hsmobileservice.Model.RoomModel;
import com.qindor.hsmobileservice.Model.RoomsModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Base64Utils;
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
import java.nio.charset.Charset;
import java.text.DecimalFormat;
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


public class Information_pay_Activity extends AppCompatActivity implements View.OnClickListener {

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
    private TextView textView,title,sbtn,outBtn,sell,dtitle;
    private String msg,userid,sKey,selectService,re,sDWID,sGH,no;
    private Timer timer,timer1;
    private int code=0;
    private String barCode;
    private List<String> sslist = new ArrayList<>();
    private AlertDialog alertDialog;
    private ArrayAdapter<String> adapter;
    private RoomModel sXMMC;
    private LayoutInflater inflater;
    private ListView dlistView;
    private boolean isDate=false;
    private ProjectAndPlistModel projectAndPlistModel;
    private WristbandDialogAdpater wristbandDialogAdpater;
    private int tp = 0;
    private Double price = 0.0;
    private AlertDialog payDialog,outDialog;
    private PiccManager piccReader;
    private int state = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_information_pay);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(Information_pay_Activity.this)
                .setMessage("加载中...")
                .setCancelable(true).create();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        sbtn = findViewById(R.id.information_pay_scanner);
        outBtn = findViewById(R.id.information_pay_wristband_off);
        sell = findViewById(R.id.information_pay_sell);
        listView = findViewById(R.id.room_information_pay_list_view);
        textView = findViewById(R.id.room_information_pay_number);
        title = findViewById(R.id.hotspring_title);
        title.setText("台号");
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        String temp = sharedPreferences.getString("roomsModel", "");
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        rmodels =new RoomsModel();
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            rmodels =(RoomsModel) ois.readObject();
            informationModel = rmodels.getInformationModel();
            title.setText("台号："+informationModel.getsTBH());
            //textView.setText("台号："+informationModel.getsTBH());
        } catch (Exception e) {
            msg=e.toString();
            handler.post(toast);
        }


        //checkNfc();

        httpUtils = new HttpUtils();
        configuration = new Configuration();
        handler = new Handler();
        sbtn.setOnClickListener(this);
        outBtn.setOnClickListener(this);
        sell.setOnClickListener(this);
        //getData();
        onNewIntent(getIntent());
        timer = new Timer();
        piccReader = new PiccManager();
        piccReader.open();
        /*timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getData();
            }
        }, 15000, 15000);*/
        projectAndPlistModel= new ProjectAndPlistModel();
        String temp2 = sharedPreferences.getString("projectAndPlistModel", "");
        if(isDate||temp2.equals("")){
            getPData();
        }else {

            ByteArrayInputStream bais2 =  new ByteArrayInputStream(Base64.decode(temp2.getBytes(), Base64.DEFAULT));
            try {
                ObjectInputStream ois = new ObjectInputStream(bais2);
                projectAndPlistModel = (ProjectAndPlistModel) ois.readObject();
            } catch (Exception e) {
                msg=e.toString();
                handler.post(toast);
            }
        }
    }

    private void getData() {
        //{"code":"gettxx","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sTBH":"301"}}
        Map<String,Object> map = new HashMap<>();
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
                startActivity(configuration.getIntent(Information_pay_Activity.this,Room_pay_Activity.class));
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
        RoomModel r1 = new RoomModel();
        for (RoomModel r : roomModels)
        {
            /*if (!r.getsZLX().equals("商品")) {
                if (r.getsZLX().equals("首钟")||r.getsZLX().equals("加钟")) {
                    list.add(new RoomAdpaterModel(r.getsJSGH(), r.getsXMMC(), r.getsDateYMDHMSSZ(), 0));
                }else {
                    sd = r.getsJSGH();
                    list.add(new RoomAdpaterModel(r.getsJSGH(), r.getsXMMC(), r.getsDateYMDHMSSZ(), 1));
                }
            }*/
            if(r.getsJSGH()!=null&&!r.getsJSGH().equals("")) {
                list.add(new RoomAdpaterModel(r.getsJSGH(), r.getsXMMC(), r.getsDateYMDHMSSZ(), 0,r.getsDWID(),r.getsZT()));
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
        /*for (int i =0;i<list.size();i++)
        {
           if(list.get(i).getsJSGH().equals(sd))
           {
               list.get(i).setState(1);
           }
        }*/
        roomListAdpater = new RoomListAdpater(Information_pay_Activity.this,list,R.layout.room_information_items);
        listView.setAdapter(roomListAdpater);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               /* List<RoomModel> rs = rmodels.getModels();
                List<RoomModel> r = new ArrayList<>();
                for (int i =0;i<rs.size();i++)
                {
                    if (rs.get(i).getsJSGH().equals(list.get(position).getsJSGH()))
                    {
                        r.add(rs.get(i));
                    }
                }
                rmodels.getModels().clear();
                rmodels.setModels(r);
                timer.cancel();
                timer=null;
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
                Intent i = new Intent(Information_pay_Activity.this, WristbandActivity.class);
                startActivity(i);
                finish();*/
                sDWID = "";
                sGH = "";
                sDWID = roomModels.get(position).getsDWID();
                price=0.0;
                sXMMC = new RoomModel();
                for (RoomModel r:roomModels) {
                    if (r.getsJSGH().equals(list.get(position).getsJSGH())) {
                        if(!r.getsZT().equals("下钟")) {
                            price += Double.valueOf(r.getfXMJE());
                        }
                    }
                    if(r.getsDWID().equals(list.get(position).getsDWID()))
                    {
                        sGH = list.get(position).getsJSGH();
                        sXMMC = r;
                    }
                }
                showSetDeBugDialog(list.get(position).getsXMMC());
                /*if (roomModels.get(position).getsZLX().equals("首钟")||roomModels.get(position).getsZLX().equals("加钟"))
                {
                    showSetDeBugDialog(roomModels.get(position).getsXMMC());
                    sXMMC = roomModels.get(position);
                }*/
            }
        });
        roomListAdpater.notifyDataSetChanged();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            /*case R.id.information_pay_scanner:
                code=0;
                scanMethod();
                break;*/
            case R.id.information_pay_wristband_off:
                if(clickUtils.isFastClick()){
                    code=1;
                    handler.post(toQI);
                }

                break;
            case R.id.information_pay_sell:
                if(clickUtils.isFastClick()){
                    SharedPreferences sp = getSharedPreferences("config", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(rmodels);//把对象写到流里
                        String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                        editor.putString("rooms", temp);
                        editor.putString("pro", "");
                        editor.putString("tec", "");
                        editor.putString("type", "");
                        editor.commit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(Information_pay_Activity.this, payServiceActivity.class);
                    startActivity(i);
                    finish();
                }

                break;
        }
    }

    public void back(){
        if(state ==1)
        {
            timer1.cancel();
        }else if(state ==2)
        {
            timer2.cancel();
        }
        startActivity(configuration.getIntent(Information_pay_Activity.this,Room_pay_Activity.class));
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

    /*protected void checkNfc() {
        if (!nfcAdapter.isEnabled()) {
            if(code ==1||code==2) {
                startActivity(new Intent(
                        android.provider.Settings.ACTION_NFC_SETTINGS));
            }
        }
        // try {
        // nfcAdapter.wait();
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
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
                barCode = x;
                rmodels.setCode(x);
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime) >= 2000) {
                    if(tp==2)
                    {
                        handler.post(selloutprice);
                    }else if(tp==1){
                        handler.post(sellprice);
                    }
                }
                lastClickTime = curClickTime;
            }

        }
    }*/
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
            ActivityCompat.requestPermissions(Information_pay_Activity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(Information_pay_Activity.this, CaptureActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if ( resultCode >0) {
            Bundle bundle = data.getExtras();
            rmodels.setCode(bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN));
            barCode = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            if(code==1)
            {
                handler.post(sellprice);
            }else if(code==2)
            {
                handler.post(selloutprice);
            }
        }
    }


    Runnable setSData = new Runnable() {
        @Override
        public void run() {
           out();
        }
    };
   /* public void scan(String resultSData){
        List<RoomModel> rs = rmodels.getModels();
        List<RoomModel> r = new ArrayList<>();
        for (int i=0;i<rs.size();i++)
        {
            if (rs.get(i).getsJSGH().equals(resultSData))
            {
                r.add(rs.get(i));
            }
        }
        if(r.size()!=0) {
            timer.cancel();
            timer=null;
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
            Intent i = new Intent(Information_pay_Activity.this, WristbandActivity.class);
            startActivity(i);
            finish();
        }
        else
        {
            RoomModel roomModel = new RoomModel();
            roomModel.setsJSGH(resultSData);
            roomModel.setsTBH(informationModel.getsTBH());
            r.add(roomModel);
            rmodels.setModels(r);
            timer.cancel();
            timer=null;
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
            Intent i = new Intent(Information_pay_Activity.this, wristbandServiceActivity.class);
            startActivity(i);
            finish();
        }
    }*/
    public void out(){
        Map<String,Object> map = new HashMap<>();
        map.put("code","doklt");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sWD","");
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

    private double unpaid =0.0;
    Runnable toQI = new Runnable() {
        @Override
        public void run() {
            Map<String, Object> map = new HashMap<>();
            map.put("code","gettzd");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sTH",informationModel.getsTBH());
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
                    String data="";
                    data = response.body().string();
                    //returnedData(resultData);
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.getString("ret").equals("0"))
                        {
                            //handler.post(toInfo);
                            JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
                            unpaid = jsonObject1.getDouble("fDZF");
                            if(unpaid == 0.0 ){
                                out();
                                //handler.post(toOut);
                            }else {
                                handler.post(toSell);
                            }
                        }else {
                            msg = jsonObject.getString("msg");
                            handler.post(toast);
                        }
                    } catch (JSONException e) {
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            });
        }
    };
    private Timer timer2;
    /**
     * 离台支付
     */
    Runnable toSell = new Runnable() {
        @Override
        public void run() {
            tp = 2;
            code = 2;
            timer2 = new Timer();
            timer2.schedule(new Task(),0, 1 * 200);
            state = 2;
            outDialog = new AlertDialog.Builder(Information_pay_Activity.this)
                    .setTitle("离台提示")
                    .setMessage("\n未付金额：" + unpaid + "(元)是否支付！\t\n1.使用黄色物理键扫码支付\n2.点击确认进行摄像头扫码支付\n3.点击取消暂不离台\n")
                    .setNegativeButton("确认",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    scanMethod();
                                }
                            })
                    .setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    code = 0;
                                    back();
                                    dialog.dismiss();
                                }
                            }).show();
        }
    };
    Runnable toOut = new Runnable() {
        @Override
        public void run() {
            new AlertDialog.Builder(Information_pay_Activity.this)
                    .setTitle("离台提示")
                    .setMessage("是否离台！\t\n1.使用黄色物理键扫码支付\n2.点击确认进行摄像头扫码支付\n")
                    .setNegativeButton("确认",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    out();
                                    dialog.dismiss();
                                }
                            })
                    .setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    code = 0;
                                    dialog.dismiss();
                                }
                            }).show();
        }
    };

    private void returnedOutData(String resultData) {
        JSONObject j = null;
        try {
            j = new JSONObject(resultData);
            String ret1 = j.getString("ret");
            if (ret1.equals("0")) {
                msg = j.getString("msg");
                handler.post(toast);
                startActivity(configuration.getIntent(Information_pay_Activity.this,Room_pay_Activity.class));
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
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private boolean isScaning = false;
    private int soundid;
    private String barcodeStr;
    private static long lastClickTime,lastClickTime1;
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
            barCode = "";
            barCode = barcodeStr;
            rmodels.setCode(barcodeStr);
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= 2000) {
                if(tp==2)
                {
                    handler.post(selloutprice);
                }else if(tp==1){
                    handler.post(sellprice);
                }
            }
            lastClickTime = curClickTime;

            /*if(code==0) {
                handler.post(setSData);
            }*/
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

    private void setServiceData() {
        sslist.clear();
        sslist.add("上钟");
        sslist.add("加钟");
        sslist.add("下钟");
        sslist.add("退单");
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
    Runnable setDListData = new Runnable() {
        @Override
        public void run() {
            setServiceData();
            wristbandDialogAdpater = new WristbandDialogAdpater(Information_pay_Activity.this,sslist,R.layout.service_select_icon);
            dlistView.setAdapter(wristbandDialogAdpater);
            dlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    re = sslist.get(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(Information_pay_Activity.this);
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
                    if (re.equals("上钟"))
                    {
                        if(sXMMC.getsZT().equals(re)){
                            msg = "该项目已上钟";
                            handler.post(toast);
                        }else
                        {
                            builder.create().show();
                        }
                    }else if(re.equals("加钟")||re.equals("下钟"))
                    {
                        if(sXMMC.getsZT().equals("备钟")){
                            msg = "该项目未上钟";
                            handler.post(toast);
                        }else
                        {
                            builder.create().show();
                        }
                    }else if(re.equals("退单")){builder.create().show();}

                }
            });
        }
    };
    //这里处理传过来的数据
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg1) {
            RegionModel regionModel1 = new RegionModel();
            switch (re)
            {
                case "上钟":
                    getServiceData("dojsz");
                    break;
                case "加钟":
                    getServiceData("dojaz");
                    break;
                case "下钟":
                    if(sXMMC.getsZT().equals("上钟")) {
                        handler.post(toPay);
                    }else {
                        msg = "该项目未上钟";
                        handler.post(toast);
                    }
                    break;
                case "退单":
                    if(!sXMMC.getsZT().equals("下钟")) {
                        getServiceData("dojtd");
                    }else {
                        msg = "该项目已下钟";
                        handler.post(toast);
                    }
                    break;

            }
        }
    };

    private void getServiceData(String service) {
        //{"code":"doklt","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sTH":"301"}}}
        //map.clear();
        Map<String, Object> map = new HashMap<String, Object>();;
        map.put("code",service);
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            if (service.equals("dojsz")||service.equals("dojaz")||service.equals("dojxz")) {
                data.put("sTH",informationModel.getsTBH());
                data.put("sGH",sGH);
            }else if(service.equals("dojtd"))
            {
                data.put("sDWID",sXMMC.getsDWID());
            }
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
                returnedSData(response.body().string());
            }
        });
    }
    private void returnedSData(String resultData) {
        try {
            alertDialog.dismiss();
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                getData();
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }else {
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            msg = e.toString();
            handler.post(toast);
            e.printStackTrace();
        }
    }
    Runnable getInfpayData = new Runnable() {
        @Override
        public void run() {
            RoomsModel rs = new RoomsModel();
            rs.getModels().add(sXMMC);
            doprintwork(informationModel.getsTBH(),sXMMC.getsJSXM(),no,price,rs);
        }
    };
    Runnable getInfoutData = new Runnable() {
        @Override
        public void run() {
            doprintwork(informationModel.getsTBH(),userid,no,unpaid,rmodels);
        }
    };
    private PrinterManager printer = new PrinterManager();
    void doprintwork(String th, String userid,String no, double unpaid, RoomsModel roomdels) {
        int openState =printer.getStatus();
        if (openState != 0) {
            msg = "打印失败";
            handler.post(toast);
            return;
        }
        printer.prn_open();
        printer.prn_setupPage(380, -1);
        doPrintRentInfo(th,userid, no,unpaid,roomdels);
//        printer.prn_drawTextEx("租赁信息\n租赁信息租赁信息租赁信息租赁信息租赁信息\n租赁信息租赁信息租赁信息\n\n\n\n", 0, 0,360,-1, "宋体", 28, 0,0x0000, 0);
        printer.prn_printPage(0);
        printer.prn_close();
    }

    private void doPrintRentInfo(String th, String userid, String no, double unpaid, RoomsModel rmodels) {
      /*  if (mPaySuccessBean == null) {

            EventBus.getDefault().post(new EventMsg(PRINTSUCCESSCODE, "打印失败"));
        }
        LeaseBean u = mPaySuccessBean.getmLeaseBean();*/
        // 标准打印，每个字符打印所占位置可能有一点出入（尤其是英文字符）
        String mediumSpline = "";
        for (int i = 0; i < 45; i++) {
            mediumSpline += "-";
        }
        int width = 360;
        String frontName = "宋体";
        int frontType = 0x0000;
        int frontSize = 24;
        printer.prn_drawTextEx("四季贵州移动服务端", 50, 30, width, -1, frontName, 30, 0, frontType, 0);
        printer.prn_drawTextEx("商品消费收据", 100, 70, width, -1, frontName, 30, 0, frontType, 0);
       SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n台  号："+th);
        sb.append("\n时  间：");
        sb.append(df.format(new Date()));
        sb.append("\n");
        sb.append("单据号：");
        sb.append(no);
        sb.append("\n");
        sb.append("收单人：");
        sb.append(userid);
        sb.append("\n");
        sb.append(mediumSpline);
        int cont = 0;
        if(code ==1){
            for (RoomModel r:rmodels.getModels()) {
                if(r.getsTBH().equals(th)){
                    if (cont != 0) {
                        sb.append("\n");
                    }
                    sb.append("\n名称："+r.getsXMMC()+"\n单价："+r.getfXMDJ()+"\n数量："+r.getfSL());
                    cont+=Integer.parseInt( r.getfSL());
                }
            }
        }else if(code == 2)
        {
            for (RoomModel r:rmodels.getModels()) {
                if(r.getsTBH().equals(th)){
                    if (cont != 0) {
                        sb.append("\n");
                    }
                    sb.append("\n名称："+r.getsXMMC()+"\n单价："+r.getfXMDJ()+"\n数量："+r.getfSL());
                    cont+=Integer.parseInt( r.getfSL());
                }
            }
        }

       /* for (int i=0;i<3;i++){

            sb.append("\n名称：名称+"+i+"\n单价：单价+"+i+"\n数量："+i);
        }*/
        sb.append("\n");
        sb.append(mediumSpline);
        sb.append("\n");
        sb.append("数量合计：");
        sb.append(cont);
        sb.append("\n");
        sb.append("金额合计：");
        sb.append(unpaid);
        sb.append("\n");
        sb.append(mediumSpline);
        sb.append("\n");
        String payType = "";
        if(barCode.length()==18){
            int tp = Integer.parseInt(barCode.substring(0,2));
            if(tp==10||tp==11||tp==12||tp==13||tp==14||tp==15){
                //（0腕带挂账、1微信、2支付宝）
                payType = "微信";
            }else if(tp==18||tp==28)
            {
                payType = "支付宝";
            }
        }else {
            payType = "腕带挂账";
        }
        sb.append("消费方式：");
        sb.append(payType);
        sb.append("\n");
        sb.append("消费店号：");
        sb.append(baseModel.getStoreNum());
        sb.append("\n");
        sb.append("欢迎光临，谢谢惠顾");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        printer.prn_drawTextEx(sb.toString(), 0, 110, width, -1, frontName, frontSize, 0, frontType, 0);
    }


    /**
     * 计算空格
     *
     * @param size
     * @return
     */
    public static String getBlankBySize(int size) {
        String resultStr = "";
        for (int i = 0; i < size; i++) {
            resultStr += " ";
        }
        return resultStr;
    }


    /**
     * 获取数据长度
     *
     * @param msg
     * @return
     */
    @SuppressLint("NewApi")
    public static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }
    int scan_card = -1;
    int SNLen = -1;
    /**
     * 下钟支付
     */
    Runnable toPay = new Runnable() {
        @Override
        public void run() {
            String text = "";
            if (sXMMC.getiZF().equals("0"))
            {
                text = "\n未付金额：";
            }else if (sXMMC.getiZF().equals("1")){
                text = "\n超时未付金额：";
            }
            tp = 1;
            code = 1;
            /*payDialog = new AlertDialog.Builder(Information_pay_Activity.this)
                    .setTitle("下钟提示")
                    .setMessage(text + doubleToString(price) + "(元)\n是否支付！\t\n1.使用黄色物理键扫码支付\n2.点击确认进行摄像头扫码支付\n3.点击取消则返回上一界面\n")
                    .setNegativeButton("确认",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    scanMethod();
                                }
                            })
                    .setPositiveButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    back();
                                    dialog.dismiss();
                                }
                            }).show();*/
            if(sXMMC.getiZF().equals("0")) {
                timer1=new Timer();
                timer1.schedule(new Task(),0, 1 * 200);
                state = 1;
                payDialog = new AlertDialog.Builder(Information_pay_Activity.this)
                        .setTitle("下钟提示")
                        .setMessage(text + doubleToString(price) + "(元)\n是否支付！\t\n1.使用黄色物理键扫码支付\n2.点击确认进行摄像头扫码支付\n3.点击取消则返回上一界面\n")
                        .setNegativeButton("确认",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        scanMethod();
                                    }
                                })
                        .setPositiveButton("取消",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        back();
                                        dialog.dismiss();
                                    }
                                }).show();

            }
        }
    };
    public class Task extends TimerTask {
        public void run(){
            try {
                byte CardType[] = new byte[2];
                byte Atq[] = new byte[14];
                char SAK = 1;
                byte sak[] = new byte[1];
                sak[0] = (byte) SAK;
                byte SN[] = new byte[10];
                scan_card = piccReader.request(CardType, Atq);
                if (scan_card > 0) {
                    SNLen = piccReader.antisel(SN, sak);
                    String c = bytesToHexString(SN, SNLen);
                    String code1 = "";
                    for (int i = 0; i < c.length(); i += 2) {
                        String c1 = c;
                        code1 = c1.substring(i, i + 2) + code1;
                    }
                    String c1 = code1.substring(2, code1.length());
                    String x = String.valueOf(Integer.parseInt(c1, 16));
                    if (x.length() < 8) {
                        for (int i = x.length(); i < 8; i++) {
                            x = "0" + x;
                        }
                    }
                    barCode = x;
                    rmodels.setCode(x);
               /* msg = c+"/"+x;
                handler.post(toast);*/
                    // code = x;
               /* msg = x;
                handler.post(toast);*/

                    long curClickTime = System.currentTimeMillis();
                    if ((curClickTime - lastClickTime1) >= 2000) {
                        if (x != null || !"".equals(x)) {
                            if (tp == 2) {
                                outDialog.dismiss();
                                handler.post(selloutprice);
                            } else if (tp == 1) {
                                payDialog.dismiss();
                                handler.post(sellprice);
                            }
                        }
                    }
                    lastClickTime1 = curClickTime;
                }
            }catch (Exception e)
            {
                msg=e.toString();
                handler.post(toast);
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
    Runnable selloutprice =new Runnable() {
        @Override
        public void run() {
            Map<String, Object> map = new HashMap<>();
            map.put("code","dotzf");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sCode",Base64Utils.getBase64(barCode));
                data.put("sTH",informationModel.getsTBH());
                data.put("fJE",unpaid);
                if(rmodels.getCode().length()==18){
                    int tp = Integer.parseInt(barCode.substring(0,2));
                    if(tp==10||tp==11||tp==12||tp==13||tp==14||tp==15){
                        //（0腕带挂账、1微信、2支付宝）
                        data.put("iFS",1);
                    }else if(tp==18||tp==28)
                    {
                        data.put("iFS",2);
                    }
                }else {
                    data.put("iFS",0);
                }
                data.put("iJZ",1);
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
                    String data = response.body().string();
                    //returnedData(resultData);
                    outDialog.dismiss();
                    if(state == 1)
                    {
                        timer1.cancel();
                    }else if(state ==2)
                    {
                        timer2.cancel();
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.getString("ret").equals("0"))
                        {
                            no = jsonObject.getString("no");
                            handler.post(getInfoutData);
                            msg= jsonObject.getString("msg");
                            handler.post(toast);
                            code = 1;
                            back();
                            //handler.post(toOut);
                        }else {
                            msg = jsonObject.getString("msg");
                            handler.post(toast);
                        }
                    } catch (JSONException e) {
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            });
        }
    };
    Runnable sellprice =new Runnable() {
        @Override
        public void run() {
            Map<String,Object> map = new HashMap<>();
            map.put("code","dojzf");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sTH",informationModel.getsTBH());
                data.put("sGH",sGH);
                data.put("sCode", Base64Utils.getBase64(barCode));
                data.put("fJE",price);
                if(barCode.length()==18){
                    int tp = Integer.parseInt(barCode.substring(0,2));
                    if(tp==10||tp==11||tp==12||tp==13||tp==14||tp==15){
                        //（0腕带挂账、1微信、2支付宝）
                        data.put("iFS",1);
                    }else if(tp==18||tp==28)
                    {
                        data.put("iFS",2);
                    }
                }else {
                    data.put("iFS",0);
                }
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
                    String data = response.body().string();
                    //returnedData(resultData);
                    if(state == 1)
                    {
                        timer1.cancel();
                    }else if(state ==2)
                    {
                        timer2.cancel();
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.getString("ret").equals("0"))
                        {

                            payDialog.dismiss();
                            no = jsonObject.getString("no");
                            handler.post(getInfpayData);
                            msg= jsonObject.getString("msg");
                            handler.post(toast);
                            getServiceData("dojxz");
                            //out();
                            /*getData();
                            handler.post(setData);*/
                        }else {
                            msg ="下钟失败！"+ jsonObject.getString("msg");
                            handler.post(toast);
                        }
                    } catch (JSONException e) {
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            });
        }
    };
    private void getPData() {
        //{"code":"getjxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        Map<String,Object> map = new HashMap<>();
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

    /**
     * double转String,保留小数点后两位
     * @param num
     * @return
     */
    public static String doubleToString(double num){
        //使用0.00不足位补0，#.##仅保留有效位
        return new DecimalFormat("0.00").format(num);
    }

}
