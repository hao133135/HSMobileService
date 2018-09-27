package com.qindor.hsmobileservice.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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

import java.util.HashMap;
import java.util.Map;

public class SetActivity extends Commontools implements View.OnClickListener {
    private Button saveBtn;
    private ImageButton backBtn;
    private TextView library,store,ip,port;
    private View storeLayout,libraryLayout;
    private String mac,resultData,libraryNum,storeNum,msg,userid,sKey;
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Configuration configuration;
    private Map<String, Object> map;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        init();
    }

    private void init() {
        saveBtn = findViewById(R.id.set_save_btn);
        //queryBtn = findViewById(R.id.set_query_btn);
        backBtn = findViewById(R.id.set_page_back_btn);
        ip = findViewById(R.id.set_ip);
        port =findViewById(R.id.set_port);
        library = findViewById(R.id.set_library_number);
        store = findViewById(R.id.set_store_number);
        storeLayout = findViewById(R.id.set_store_number_layout);
        libraryLayout = findViewById(R.id.set_library_number_layout);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));        ip.setText(baseModel.getIp());
        port.setText(baseModel.getPort());
        if (baseModel.getLibraryNum()!=null||baseModel.getStoreNum()!=null) {
            library.setText(baseModel.getLibraryNum());
            store.setText(baseModel.getStoreNum());
        }
        configuration = new Configuration();
        handler = new Handler();
        httpUtils = new HttpUtils();
        map = new HashMap<>();
        if (!isNetworkAvailable(SetActivity.this))
        {
            msg="当前没有可用网络";
            handler.post(toast);

        }else {
            mac = getLocalMacAddress();
        }
        saveBtn.setOnClickListener(this);
       // queryBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.set_save_btn:
                if (mac!=null){
                    successMethod();
                }else
                {
                    msg="当前没有可用网络";
                    handler.post(toast);
                }
                break;
           /* case R.id.set_query_btn:
                if (mac!=null) {
                    query();
                }
                else {
                    msg="当前没有可用网络";
                    handler.post(toast);
                }
                break;*/
            case R.id.set_page_back_btn:
                startActivity(configuration.getIntent(SetActivity.this,LoginActivity.class));
                finish();
                break;
        }
    }

   /* private void query() {
        map.clear();
        map.put("code","getfdh");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",mac);
            data.put("sIP", GetIpUtils.getIP(SetActivity.this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        baseModel = new BaseModel(ip.getText().toString(),library.getText().toString(),store.getText().toString(),mac,port.getText().toString());
        resultData = null;
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        //resultData = httpUtils.baseHttp(SetActivity.this,baseModel,"spring",map);
        resultData = "{\"code\":\"getfdh\",\"ret\":\"0\",\"msg\":{\"sFDH\":\"018 门票-景区餐饮\",\"sFKH\":\"0004 宴会厅\"}}";
        returnedValue(resultData);
    }*/

    private void successMethod() {
        SharedPreferences sp=getSharedPreferences("config",0);
        SharedPreferences.Editor editor=sp.edit();
        //把数据进行保存
        StringBuffer output = new StringBuffer();
        editor.putString("ip",ip.getText().toString());
        editor.putString("port",port.getText().toString());
        editor.putString("mac",mac);
        //提交数据
        editor.commit();
        msg="保存成功";
        handler.post(toast);
        startActivity(configuration.getIntent(SetActivity.this,LoginActivity.class));
        finish();
    }

    /*private void returnedValue(String resultData) {
        //{"code":"getfdh","ret":"0","msg":{"sFDH":"018 门票-景区餐饮","sFKH":"0004 宴会厅"}}
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
                baseModel = new BaseModel(ip.getText().toString(),jsonObject1.getString("sFKH"),jsonObject1.getString("sFDH"),mac,port.getText().toString());
                handler.post(openQueryData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
    /*Runnable openQueryData = new Runnable() {
        @Override
        public void run() {
            library.setText(baseModel.getLibraryNum());
            store.setText(baseModel.getStoreNum());
            libraryLayout.setVisibility(View.VISIBLE);
            storeLayout.setVisibility(View.VISIBLE);
        }
    };*/
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
    /**
     * 监听返回键
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            startActivity(configuration.getIntent(SetActivity.this,LoginActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
