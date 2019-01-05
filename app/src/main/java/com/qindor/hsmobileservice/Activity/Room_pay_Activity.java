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
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
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

public class Room_pay_Activity extends AppCompatActivity implements View.OnClickListener {
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
    private String re,code,th,msg,regionName,number,no;
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
    private Timer timer,timer1;
    private PrinterManager printer = new PrinterManager();
    private final static String PRNT_ACTION = "android.prnt.message";
    private PiccManager piccReader;
    private static long lastClickTime1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        init();
    }

    private void init() {
        dialog1=new LoadingDialog.Builder(Room_pay_Activity.this)
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
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        regionName = sharedPreferences.getString("regionName","");
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        httpUtils = new HttpUtils();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        imodel = new InformationModel();
        configuration = new Configuration();
        handler = new Handler();
        informationModels = new ArrayList<>();
        sbool = false;
        piccReader = new PiccManager();
        swicthView();
        regionDialogBtn.setOnClickListener(this);
        queryBtn.setOnClickListener(this);
        swicthBtn.setOnClickListener(this);
        dialog1.show();
        getRegionData();
        Time t=new Time();
        t.setToNow();
        //checkNfc();
        day = t.monthDay;
        if(day-sharedPreferences.getInt("day",0)!=0){
            isDate=true;
        }
        timer = new Timer();
        piccReader.open();
        /*timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                getRegionData();
            }
        }, 15000, 15000);*/
    }

    void doprintwork(String th, String userid, String no, double unpaid,RoomsModel rmodels) {
        int openState =printer.getStatus();
        if (openState != 0) {
            msg = "打印失败";
            handler.post(toast);
            return;
        }
        printer.prn_open();
        printer.prn_setupPage(380, -1);
        doPrintRentInfo(th,userid, no,unpaid,rmodels);
        printer.prn_printPage(0);
        printer.prn_close();
        handler.post(toInfo);
    }

    private void doPrintRentInfo(String th, String userid, String no, double unpaid,RoomsModel rmodels) {
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
        printer.prn_drawTextEx("商品消费收据", 100, 80, width, -1, frontName, 30, 0, frontType, 0);
       SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n台  号："+th);
        sb.append("\n时  间：");
        sb.append(df.format(new Date()));
        sb.append("\n");
        sb.append("收单人：");
        sb.append(userid);
        sb.append("\n");
        sb.append("单据号：");
        sb.append(no);
        sb.append("\n");
        sb.append(mediumSpline);
        int cont = 0;
        for (RoomModel r:rmodels.getModels()) {
            if(r.getsTBH().equals(th)){
                if (cont != 0) {
                    sb.append("\n");
                }
                sb.append("\n名称："+r.getsXMMC()+"\n单价："+r.getfXMDJ()+"\n数量："+r.getfSL());
                cont+=Integer.parseInt( r.getfSL());
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
        if(code.length()==18){
            int tp = Integer.parseInt(code.substring(0,2));
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
    private void getData(RegionModel regionModel) {
        //{"code":"gettls","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sQYH":"001002"}}
        Map<String, Object> map = new HashMap<>();
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
        Map<String, Object> map = new HashMap<>();
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
                if(rmap!=null)
                {
                    if(!regionName.equals("")) {
                        re = regionName;
                        myHandler.sendEmptyMessageDelayed(0, 1000);
                    }else {
                        re = jsonArray.getJSONObject(0).getString("sQYM");
                        myHandler.sendEmptyMessageDelayed(0, 1000);
                    }
                }else {
                    msg = "获取区域失败";
                    handler.post(toast);
                    dialog1.dismiss();
                }
            }else
            {
                msg =resultData;
                handler.post(toast);
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
                    rmodels.setInformationModel(informationModelss.get(position));
                    saveData(rmodels);
                    startActivity(configuration.getIntent(Room_pay_Activity.this,Information_pay_Activity.class));
                    finish();
                }else {
                    th = informationModelss.get(position).getsTBH();
                    imodel =informationModelss.get(position);
                    openInformation();
                }
            }
        });
        saImageItems.notifyDataSetChanged();
        informactionAdpater = new InformactionAdpater(this,informationModelss,R.layout.room_list_items);
        listView.setAdapter(informactionAdpater);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(isOpen(informationModelss.get(position)))
                {
                    rmodels.setInformationModel(informationModelss.get(position));
                    saveData(rmodels);
                    startActivity(configuration.getIntent(Room_pay_Activity.this,Information_pay_Activity.class));
                    finish();
                }else {
                    th = informationModelss.get(position).getsTBH();
                    imodel =informationModelss.get(position);
                    openInformation();
                }
            }
        });
        informactionAdpater.notifyDataSetChanged();
    }
    private void openInformation() {
        Button save,out;
        final TextView numText;
        AlertDialog.Builder setDeBugDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.open_information_pay_dialog, null);
        save = dialogView.findViewById(R.id.open_information_save);
        out = dialogView.findViewById(R.id.open_information_out);
        numText = dialogView.findViewById(R.id.open_information_num);
        setDeBugDialog = new AlertDialog.Builder(this);
        setDeBugDialog.setView(dialogView);
        alertDialog = setDeBugDialog.create();
        alertDialog.show();
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = numText.getText().toString();
                if (number.equals(""))
                {
                    number = "1";
                }
                /*temp=0;
                code = barcodeStr;
                if(code!=null&&!code.equals("")) {
                    handler.post(getIData);
                }else {
                    scanMethod();
                }*/
                handler.post(getIData);
                //handler.post(toQI);
                alertDialog.dismiss();
            }
        });
        /*new AlertDialog.Builder(RoomActivity.this)
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
                        }).show();*/
    }
    private void scanMethod() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(Room_pay_Activity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(Room_pay_Activity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if ( resultCode >0) {
            Bundle bundle = data.getExtras();
            code=bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
           /* if(temp==0){
                handler.post(getIData);
            }else if(temp==1)
            {
                handler.post(sellprice);
            }*/
            handler.post(sellprice);
        }
    }
    Runnable sellprice =new Runnable() {
        @Override
        public void run() {
            Map<String, Object> map = new HashMap<>();
            map.put("code","dotzf");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sCode", Base64Utils.getBase64(code));
                data.put("sTH",th);
                data.put("fJE",unpaid);
                if(code.length()==18){
                    int tp = Integer.parseInt(code.substring(0,2));
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
                data.put("iJZ",0);
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
                    timer1.cancel();
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.getString("ret").equals("0"))
                        {
                            no = jsonObject.getString("no");
                            msg= jsonObject.getString("msg");
                            handler.post(toast);
                            handler.post(getInfData);
                        }else {
                            handler.post(toInfo);
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
    Runnable getInfData = new Runnable() {
        @Override
        public void run() {
            getInfData();
        }
    };
    private void getInfData() {
        //{"code":"gettxx","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sTBH":"301"}}
        Map<String,Object> map = new HashMap<>();
        map.put("code","gettxx");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sTBH",th);
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
                returnedinData(resultData);
            }
        });
    }
    private RoomsModel rmodels = new RoomsModel();
    private void returnedinData(String resultData) {
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
                    roomModel.setsTBH(th);
                    rmodels.getModels().add(roomModel);
                }
                doprintwork(th,userid,no,unpaid,rmodels);
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
    Runnable getIData = new Runnable() {
        @Override
        public void run() {
            //{"code":"dotkt","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sTH":"301"}}
            Map<String, Object> map = new HashMap<>();
            map.put("code","dotkt");
            JSONObject data = new JSONObject();
            try {
                data.put("sMAC",baseModel.getMac());
                data.put("sIP",baseModel.getIp());
                data.put("sWD","");
                data.put("sTH",th);
                data.put("iRS",number);
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
                            msg = jsonObject.getString("msg");
                            handler.post(toast);
                            handler.post(toQI);
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
                            if(unpaid == 0.0){
                                handler.post(toInfo);
                            }else {
                                handler.post(toSell);
                            }
                        }
                       /* msg = jsonObject.getString("msg");
                        handler.post(toast);*/
                    } catch (JSONException e) {
                        msg = e.toString();
                        handler.post(toast);
                    }
                }
            });
        }
    };
    private AlertDialog openDialog;
    Runnable toSell = new Runnable() {
        @Override
        public void run() {
            timer1 = new Timer();
            timer1.schedule(new Task(), 0,1 * 200);
            openDialog = new AlertDialog.Builder(Room_pay_Activity.this)
                    .setTitle("开台提示")
                    .setMessage("\n未付金额：" + unpaid + "(元)\n是否支付！\t\n1.使用黄色物理键扫码支付\n2.点击确认进行摄像头扫码支付\n3.点击取消稍后离台时支付\n")
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
                                    timer1.cancel();
                                    handler.post(toInfo);
                                    dialog.dismiss();
                                }
                            }).show();

        }
    };
    int scan_card = -1;
    int SNLen = -1;
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
                code = x;
               /* msg = c+"/"+x;
                handler.post(toast);*/
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime1) >= 2000) {
                    openDialog.dismiss();
                    handler.post(sellprice);
                }
                lastClickTime1 = curClickTime;

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
    Runnable toInfo = new Runnable() {
        @Override
        public void run() {
            rmodels.setInformationModel(imodel);
            saveData(rmodels);
            startActivity(configuration.getIntent(Room_pay_Activity.this,Information_pay_Activity.class));
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


    public void saveData(RoomsModel roomsModel){
        SharedPreferences sp=getSharedPreferences("config",0);
        SharedPreferences.Editor editor=sp.edit();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(roomsModel);//把对象写到流里
            String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            editor.putString("roomsModel", temp);
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
            roomDialogAdpater = new RoomDialogAdpater(Room_pay_Activity.this,sslist,R.layout.service_room_icon);
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
            roomQueryAdpater = new RoomQueryAdpater(Room_pay_Activity.this,slist,R.layout.service_query_icon);
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
            }else {
                regionName="";
                returnedData(resultData);
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
        startActivity(configuration.getIntent(Room_pay_Activity.this,LoginActivity.class));
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
                if(clickUtils.isFastClick()){
                    showSetDeBugDialog();
                }

                break;
            case R.id.room_view_query:
                if(clickUtils.isFastClick()){
                    if (informationModels.size()!=0) {
                        showSetQueryDialog();
                    }else {
                        msg="请选择区域";
                        handler.post(toast);
                    }
                }

                break;
            case R.id.room_view_switch_btn:


               /* if(printer == null){
                    printer = new PrinterManager();
                }
                doprintwork(th, userid, unpaid, rmodels);*/
                if(clickUtils.isFastClick()){
                    swicthView();
                }

                break;
        }
    }
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
            code = barcodeStr;
            long curClickTime = System.currentTimeMillis();
            if ((curClickTime - lastClickTime) >= 2000) {
                handler.post(sellprice);
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
    protected NfcAdapter nfcAdapter;
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
                code = x;
                long curClickTime = System.currentTimeMillis();
                if ((curClickTime - lastClickTime) >= 2000) {
                    handler.post(sellprice);
                }
                lastClickTime = curClickTime;
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
}
