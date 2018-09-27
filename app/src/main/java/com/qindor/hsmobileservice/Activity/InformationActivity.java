package com.qindor.hsmobileservice.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.RoomListAdpater;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.InformationModel;
import com.qindor.hsmobileservice.Model.RoomModel;
import com.qindor.hsmobileservice.Model.RoomsModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Configuration;
import com.qindor.hsmobileservice.Utils.Constant;
import com.qindor.hsmobileservice.Utils.HttpUtils;
import com.qindor.hsmobileservice.Utils.LoadingDialog;
import com.qindor.hsmobileservice.Utils.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton backBtn;
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private Map<String, Object> map;
    private InformationModel informationModel = null;
    private RoomsModel rmodels;
    private String resultData;
    protected NfcAdapter nfcAdapter;
    protected MifareClassic mifareClassic;
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    private RoomListAdpater roomListAdpater = null;
    private ListView listView;
    private TextView textView,title,sbtn,outBtn;
    private String msg,userid,sKey;
    private Timer timer;
    private int code=0;
    private LoadingDialog.Builder builder1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_information);
        init();
    }

    private void init() {
        builder1=new LoadingDialog.Builder(InformationActivity.this)
                .setMessage("加载中...")
                .setCancelable(false);
        dialog1=builder1.create();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        sbtn = findViewById(R.id.information_scanner);
        outBtn = findViewById(R.id.information_wristband_off);
        listView = findViewById(R.id.room_information_list_view);
        textView = findViewById(R.id.room_information_number);
        backBtn = findViewById(R.id.sell_page_back_btn);
        title = findViewById(R.id.hotspring_title);
        title.setText("台号");
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
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
        map = new HashMap<String, Object>();
        sbtn.setOnClickListener(this);
        outBtn.setOnClickListener(this);
        getData();
        checkNfc();
        onNewIntent(getIntent());
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getData();
            }
        }, 15000, 15000);

    }

    private void getData() {
        //{"code":"gettxx","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sTBH":"301"}}
        map.clear();
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
        resultData = null;
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"gettxx\",\"ret\":\"0\",\"msg\":[{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"20.00\",\"fSL\":\"1.00\",\"fXMJE\":\"20.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"张三\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:41:41\",\"sDateYMDHMSXZ\":\"2018-08-11 16:41:41\",\"iZSC\":\"60\",\"iSY\":\"60\"},{\"sDWID\":\"1808119588186356874011A7E91679CEA4020A\",\"sWDBH\":\"WQT0182\",\"sXMMC\":\"推背\",\"fXMDJ\":\"25.00\",\"fSL\":\"1.00\",\"fXMJE\":\"25.00\",\"sJSGH\":\"A002\",\"sJSXM\":\"李四\",\"sZLX\":\"首钟\",\"sDateYMDHMSSZ\":\"2018-08-11 15:42:42\",\"sDateYMDHMSXZ\":\"2018-08-11 16:42:42\",\"iZSC\":\"60\",\"iSY\":\"60\"}]}";
        returnedData(resultData);

    }

    private void returnedData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                if(rmodels.getModels()!=null)
                    rmodels.getModels().clear();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                    RoomModel roomModel = new RoomModel(jsonObject1.getString("sDWID"),jsonObject1.getString("sWDBH"),jsonObject1.getString("sXMMC"),jsonObject1.getString("fXMDJ"),jsonObject1.getString("fSL"),jsonObject1.getString("fXMJE"),jsonObject1.getString("sJSGH"),jsonObject1.getString("sJSXM"),jsonObject1.getString("sZLX"),jsonObject1.getString("sDateYMDHMSSZ"),jsonObject1.getString("sDateYMDHMSXZ"),jsonObject1.getString("iZSC"),jsonObject1.getString("iSY"));
                    rmodels.getModels().add(roomModel);
                }
                handler.post(setData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // RoomListAdpater roomListAdpater = new RoomListAdpater<>(InformationActivity2.this,informationModel,R.layout.room_information_items);
    }
    Runnable setData = new Runnable() {
        @Override
        public void run() {
            loadView(rmodels.getModels());
        }
    };
    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    };
    public void loadView(final List<RoomModel> roomModels){
        roomListAdpater = new RoomListAdpater(InformationActivity.this,roomModels,R.layout.room_information_items);
        listView.setAdapter(roomListAdpater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<RoomModel> rs = rmodels.getModels();
                List<RoomModel> r = new ArrayList<>();
                for (RoomModel r1 : rs)
                {
                    if (r1.getsWDBH().equals(roomModels.get(position).getsWDBH()))
                    {
                        r.add(r1);
                    }
                }
                rmodels.getModels().clear();
                rmodels.setModels(r);
                timer.cancel();
                Intent i = new Intent(InformationActivity.this, WristbandActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("rooms",rmodels);
                i.putExtras(bundle1);
                startActivity(i);
                finish();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.information_scanner:
                code=0;
                scanMethod();
                break;
            case R.id.sell_page_back_btn:
                back();
                break;
            case R.id.information_wristband_off:
                code=1;
                scanMethod();
                break;
        }
    }


    public void back(){
        timer.cancel();
        startActivity(configuration.getIntent(InformationActivity.this,RoomActivity.class));
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

    protected void checkNfc() {
        if (!nfcAdapter.isEnabled()) {
            startActivity(new Intent(
                    android.provider.Settings.ACTION_NFC_SETTINGS));
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
                rmodels.setCode(ByteArrayToHexString(id));
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
            ActivityCompat.requestPermissions(InformationActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(InformationActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 1);
    }
    private void outScanMethod() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(InformationActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(InformationActivity.this, CaptureActivity.class);
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

    private LoadingDialog dialog1;
    Runnable setSData = new Runnable() {
        @Override
        public void run() {
            //{"code":"getwdh","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sCD":"012489"}}

            map.clear();
            map.put("code","getwdh");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sTBH",rmodels.getCode());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.put("msg",data);
            resultData=null;
            dialog1.show();
            //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
            resultData = "{\"code\":\" getwdh\",\"ret\":\"0\",\"msg\":{\"sCD\":\"012489\",\"sWD\":\"WQT0182\"}}";
            dialog1.dismiss();
            if(code == 0 )
            {
                scan();
            }else if(code ==1 )
            {
                out();
            }
        }
    };
    public void scan(){
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
                String sWD = jsonObject1.getString("sWD");
                List<RoomModel> models = rmodels.getModels();
                List<RoomModel> m = new ArrayList<>();
                for (RoomModel r: models)
                {
                    if (r.getsWDBH().equals(sWD))
                    {
                        m.add(r);
                    }
                }
                rmodels.getModels().clear();
                rmodels.setModels(m);
                timer.cancel();
                Intent i = new Intent(InformationActivity.this, WristbandActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("rooms",  rmodels);
                i.putExtras(bundle1);
                startActivity(i);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void out(){
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
                String sWD = jsonObject1.getString("sWD");
                map.clear();
                map.put("code","dotkt");
                JSONObject data = new JSONObject();
                try {
                    data.put("sMAC",baseModel.getMac());
                    data.put("sIP",baseModel.getIp());
                    data.put("sWD",sWD);
                    data.put("sTH",informationModel.getsTBH());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                map.put("msg",data);
                dialog1.show();
                //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
                String resultDatas = "{\"code\":\"doklt\",\"ret\":\"0\",\"msg\":\"离台成功\"}";
                dialog1.dismiss();
                JSONObject j = new JSONObject(resultDatas);
                String ret1 = j.getString("ret");
                if (ret1.equals("0")) {
                    msg = j.getString("msg");
                    handler.post(toast);
                    startActivity(configuration.getIntent(InformationActivity.this,RoomActivity.class));
                    finish();
                }else {
                    msg = j.getString("msg");
                    handler.post(toast);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
