package com.qindor.hsmobileservice.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.device.PrinterManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Commontools;
import com.qindor.hsmobileservice.Utils.Configuration;
import com.qindor.hsmobileservice.Utils.HttpUtils;
import com.qindor.hsmobileservice.Utils.LoadingDialog;
import com.qindor.hsmobileservice.Utils.MD5Utils;
import com.qindor.hsmobileservice.Utils.clickUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends Commontools implements View.OnClickListener {
    private Button loginBtn;
    private ImageButton setBtn;
    private TextView loginUser,loginPwd;
    private String msg,user,pwd,resultData,sKey,sSign,st,mode;
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private LoadingDialog dialog1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

    }

    private void init() {
        dialog1=new LoadingDialog.Builder(LoginActivity.this)
                .setMessage("加载中...")
                .setCancelable(true).create();
        loginBtn = findViewById(R.id.hotspring_login_btn);
        setBtn = findViewById(R.id.login_set_btn);
        setBtn.setVisibility(View.VISIBLE);
        loginUser = findViewById(R.id.hotspring_login_username);
        loginPwd = findViewById(R.id.hotspring_login_password);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));    httpUtils = new HttpUtils();
        loginUser.setText(sharedPreferences.getString("userid",""));
        mode =sharedPreferences.getString("mode","支付模式");
        configuration = new Configuration();
        handler = new Handler();
        loginBtn.setOnClickListener(this);
        setBtn.setOnClickListener(this);
        if (!isNetworkAvailable(LoginActivity.this))
        {
            msg="当前没有可用网络";
            handler.post(toast);
        }


    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.hotspring_login_btn:
                if(clickUtils.isFastClick()){
                    dialog1.show();
                    if (baseModel.getIp()!=null&&!"".equals(baseModel.getIp())) {
                        login();
                  /*  startActivity(configuration.getIntent(LoginActivity.this,Room_wristband_Activity.class));
                    finish();*/
                    }else {
                        dialog1.dismiss();
                        msg="没有配置IP地址";
                        handler.post(toast);
                    }
                }
                //test();
                break;
            case R.id.login_set_btn:
                if(clickUtils.isFastClick()){
                    configuration();
                }

                break;
        }
    }

    private void configuration() {
        startActivity(configuration.getIntent(LoginActivity.this,SetActivity.class));
        finish();
    }
    private void login() {
        user = loginUser.getText().toString();
        pwd = loginPwd.getText().toString();
        if (user.equals("") || pwd.equals("")) {
            dialog1.dismiss();
            msg="请输入用户名和密码";
            handler.post(toast);
        } else  {
             try {
                OkHttpClient mOkHttpClient = new OkHttpClient();//创建OkHttpClient对象。
                FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                formBody.add( "userid",user);
                formBody.add("pass",pwd);
                Request request = new Request.Builder()//创建Request 对象。
                        .url(  "http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/login")
                        .post(formBody.build())//传递请求体
                        .build();
                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        dialog1.dismiss();
                        msg="服务器链接失败";
                        handler.post(toast);
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        resultData = null;
                        resultData = response.body().string();
                        if(resultData!=null) {
                            returnedValue(resultData);
                        }
                    }
                });
            }catch (Exception e)
            {
                msg = e.toString();
                handler.post(toast);
            }
            //{"rlt":"true","msg":"管理员"}
            //{"rlt":"false","msg":"用户ID或密码错误"}


            // resultData = "{\"rlt\":\"true\",\"msg\":\"管理员\"}";
            //returnedValue(resultData);

        }

    }
    private void returnedValue(String data) {
        //{"rlt":"true","msg":"管理员"}
        //{"rlt":"false","msg":"用户ID或密码错误"}
        try {
            JSONObject jsonObject = new JSONObject(data);
            String ret = jsonObject.getString("rlt");
            if (ret.equals("true"))
            {
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("userid",user);
                st = MD5Utils.MD5(jsonObject.getString("user")+pwd);
                editor.putString("sKey",st);
                //提交数据
                editor.commit();
                handler.post(setbtns);
                dialog1.dismiss();
                if(mode.equals("支付模式")) {
                    startActivity(configuration.getIntent(LoginActivity.this, Room_pay_Activity.class));
                }else if(mode.equals("腕带模式")){
                    startActivity(configuration.getIntent(LoginActivity.this, Room_wristband_Activity.class));
                }
                finish();
            }
            else
            {
                dialog1.dismiss();
                msg = jsonObject.getString("msg");
                handler.post(toast);
            }
        } catch (JSONException e) {
            dialog1.dismiss();
            e.printStackTrace();
        }

    }
    private void query() {
        Map<String, Object> map = new HashMap<>();
        map.put("code","getfdh");
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
                .post(httpUtils.baseOkHttp(baseModel,user,st,map).build())//传递请求体
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
                returnValue(resultData);
            }
        });

    }
    private void returnValue(String resultData) {
        //{"code":"getfdh","ret":"0","msg":{"sFDH":"018 门票-景区餐饮","sFKH":"0004 宴会厅"}}
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("store",jsonObject1.getString("sFDH"));
                editor.putString("library",jsonObject1.getString("sFKH"));
                editor.commit();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    Runnable setbtns = new Runnable() {
        @Override
        public void run() {
            query();
            setBtn.setVisibility(View.GONE);
        }
    };
    Runnable toast = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    };
    /**
     * 检查当前网络是否可用
     *
     * @param
     * @return
     */

    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {
            dialog_Exit(LoginActivity.this);
        }
        return false;

        //end onKeyDown
    }
    public static void dialog_Exit(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("确定要退出吗?");
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("确认",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        android.os.Process.killProcess(android.os.Process
                                .myPid());
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

    private PrinterManager printer = new PrinterManager();
    private final static String PRNT_ACTION = "android.prnt.message";
    public void test(){
        int openState =printer.getStatus();
        printer.prn_open();
        printer.prn_setupPage(380, -1);
        doPrintRentInfo();
//        printer.prn_drawTextEx("租赁信息\n租赁信息租赁信息租赁信息租赁信息租赁信息\n租赁信息租赁信息租赁信息\n\n\n\n", 0, 0,360,-1, "宋体", 28, 0,0x0000, 0);
        printer.prn_printPage(0);
        printer.prn_close();
    }
    private void doPrintRentInfo( ) {
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
        printer.prn_drawTextEx("设备名称：0000",0,100,width,-1,frontName,frontSize,0,frontType,0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append("\n时  间：");
        sb.append(df.format(new Date()));
        sb.append("\n");
        sb.append("收 单 人：");
        sb.append(msg);
        sb.append("\n");
        sb.append("单 据 号：");
        sb.append("0000");
        sb.append("\n");
        sb.append(mediumSpline);
        int cont = 0;
        /*for (RoomModel r:model.getModels()) {
            if(r.getsZT().equals(th)){
                sb.append("\n名称："+r.getsXMMC()+"\n单价："+r.getfXMDJ()+"\n数量："+r.getfSL());
                cont+=Integer.parseInt( r.getfSL());
            }
        }*/
        for (int i=0;i<3;i++){
            sb.append("\n名称：名称+"+i+"\n单价：单价+"+i+"\n数量："+i);
        }
        sb.append("\n");
        sb.append(mediumSpline);
        sb.append("\n");
        sb.append("数量合计：");
        sb.append(cont);
        sb.append("\n");
        sb.append("金额合计：");
        sb.append("100");
        sb.append("\n");
        sb.append(mediumSpline);
        sb.append("\n");
        String payType = "";
        /*if(barCode.length()==18){
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
        }*/
        sb.append("消费方式：");
        sb.append("微信");
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



}
