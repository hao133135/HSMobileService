package com.qindor.hsmobileservice.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ansen.http.entity.HttpConfig;
import com.ansen.http.net.HTTPCaller;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Commontools;
import com.qindor.hsmobileservice.Utils.Configuration;
import com.qindor.hsmobileservice.Utils.DownloadUtil;
import com.qindor.hsmobileservice.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SetActivity extends Commontools implements View.OnClickListener {
    private Button saveBtn,upgrade;
    private ImageButton backBtn;
    private TextView library,store,ip,port,ver;
    private View storeLayout,libraryLayout;
    private String mac,resultData,libraryNum,storeNum,msg,userid,sKey,mode;
    private Handler handler;
    private HttpUtils httpUtils;
    private BaseModel baseModel;
    private Spinner modeSpinner;
    private Configuration configuration;
    private Map<String, Object> map;
    private List<String > sslist = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ProgressDialog pd;
    private String newAppName;
    private ProgressDialog progressDialog;

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
        upgrade = findViewById(R.id.set_upgrade_btn);
        ver = findViewById(R.id.set_ver_btn);
        ip = findViewById(R.id.set_ip);
        port =findViewById(R.id.set_port);
        modeSpinner= findViewById(R.id.set_mode);
        library = findViewById(R.id.set_library_number);
        store = findViewById(R.id.set_store_number);
        storeLayout = findViewById(R.id.set_store_number_layout);
        libraryLayout = findViewById(R.id.set_library_number_layout);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        mode =sharedPreferences.getString("mode","支付模式");
        userid = sharedPreferences.getString("userid","");
        sKey = sharedPreferences.getString("sKey","");
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));        ip.setText(baseModel.getIp());
        port.setText(baseModel.getPort());
        if (baseModel.getLibraryNum()!=null||baseModel.getStoreNum()!=null) {
            library.setText(baseModel.getLibraryNum());
            store.setText(baseModel.getStoreNum());
        }
        pd = new ProgressDialog(this);
        configuration = new Configuration();
        handler = new Handler();
        httpUtils = new HttpUtils();
        new SpinnerTask().execute();
        map = new HashMap<>();
        if (!isNetworkAvailable(SetActivity.this))
        {
            msg="当前没有可用网络";
            handler.post(toast);

        }else {
            mac = getLocalMacAddress();
        }
        saveBtn.setOnClickListener(this);
        upgrade.setOnClickListener(this);
        ver.setText("版本号："+getLocalVersion(this));
        if(library!=null&&!"".equals(library))
        {
            upgrade.setVisibility(View.VISIBLE);
        }
       // queryBtn.setOnClickListener(this);
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setAgent(true);//有代理的情况能不能访问
        httpConfig.setDebug(true);//是否debug模式 如果是debug模式打印log
        httpConfig.setTagName("QIMINGXIN");//打印log的tagname

        //可以添加一些公共字段 每个接口都会带上
        httpConfig.addCommonField("pf", "android");
        httpConfig.addCommonField("version_code", "" + getLocalVersion(this));

        //初始化HTTPCaller类
        HTTPCaller.getInstance().setHttpConfig(httpConfig);
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
            case R.id.set_upgrade_btn:
                upgrade();
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
        editor.putString("mode",modeSpinner.getSelectedItem().toString());
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

    class SpinnerTask extends AsyncTask<Object, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Object... params) {
            sslist.add("支付模式");
            sslist.add("腕带模式");
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
        modeSpinner.setAdapter(adapter);
        if(mode.equals("支付模式")) {
            modeSpinner.setSelection(0, true);
        }else if(mode.equals("腕带模式"))
        {
            modeSpinner.setSelection(1, true);
        }
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


    public void upgrade(){
        Map<String, Object> map = new HashMap<>();
        map.put("code","getver");
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
                .url(  "http://"+ip.getText().toString()+":"+port.getText().toString()+"/handheld_device/spring")
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
                returnUpValue(resultData);
            }
        });
    }
    String downloadUrl="";

    private void returnUpValue(String resultData) {
        //{"code":"getfdh","ret":"0","msg":{"sFDH":"018 门票-景区餐饮","sFKH":"0004 宴会厅"}}
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                JSONObject jsonObject1 = jsonObject.getJSONObject("msg");
                SharedPreferences sp=getSharedPreferences("config",0);
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("sVer",jsonObject1.getString("sVer"));
                editor.putString("sFile",jsonObject1.getString("sFile"));
                newAppName = jsonObject1.getString("sFile");
                msg= newAppName;
                handler.post(toast);
                editor.commit();
                if(jsonObject1.getString("sVer")!=null){
                    if(!jsonObject1.getString("sVer").equals(getLocalVersion(this))){//有新版本
                        //downloadUrl="http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/download?filename="+newAppName;
                        //handler.post(showUpdaloadDialog);
                        //downFile("http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/download?filename="+newAppName);
                        Uri uri = Uri.parse("http://"+baseModel.getIp()+":"+baseModel.getPort()+"/handheld_device/download?filename="+newAppName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }else{//没有新版本
                        Toast.makeText(this,"当前为最新版本",Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /*private void showUpdaloadDialog(final String downloadUrl){
        // 这里的属性可以一直设置，因为每次设置后返回的是一个builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置提示框的标题
        builder.setTitle("版本升级").
                setIcon(R.mipmap.ic_launcher). // 设置提示框的图标
                setMessage("发现新版本！请及时更新").// 设置要显示的信息
                setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startUpload(downloadUrl);//下载最新的版本程序
            }
        }).setNegativeButton("取消", null);//设置取消按钮,null是什么都不做，并关闭对话框
        AlertDialog alertDialog = builder.create();
        // 显示对话框
        alertDialog.show();
    }*/
    /*private void startUpload(String downloadUrl){
        progressDialog=new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("正在下载新版本");
        progressDialog.setCancelable(false);//不能手动取消下载进度对话框

        final String fileSavePath=Utils.getSaveFilePath(downloadUrl);
        HTTPCaller.getInstance().downloadFile(downloadUrl,fileSavePath,null,new ProgressUIListener(){

            @Override
            public void onUIProgressStart(long totalBytes) {//下载开始
                progressDialog.setMax((int)totalBytes);
                progressDialog.show();
            }

            //更新进度
            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                progressDialog.setProgress((int)numBytes);
            }

            @Override
            public void onUIProgressFinish() {//下载完成
                Toast.makeText(this,"下载完成",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                openAPK(fileSavePath);
            }
        });
    }*/
    private void downFile(final String url) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                DownloadUtil.get().download(url, "qindor", new DownloadUtil.OnDownloadListener() {
                    //                   版本需要更新，正常下载
                    @Override
                    public void onDownloadSuccess() {//下载完成
                        handler1.sendEmptyMessage(3);
                    }
                    //                  下载中，显示下载进度
                    @Override
                    public void onDownloading(int progress) {//下载中
                        Message message = Message.obtain();
                        Bundle bundle = new Bundle();
                        message.what = 4;
                        bundle.putInt("progress",progress);
                        message.setData(bundle);
                        handler1.sendMessage(message);
                    }
                    @Override
                    public void onDownloadFailed() {//下载失败
                        handler1.sendEmptyMessage(5);
                    }
                },SetActivity.this);
            }
        }.start();
    }
    /*private void openAPK(String fileSavePath){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"+fileSavePath),"application/vnd.android.package-archive");
        startActivity(intent);
    }*/

    private Handler handler1=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==3){
                //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
               // Intent intent = new Intent(Intent.ACTION_cO);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File db = new File(SetActivity.this.getExternalCacheDir(),newAppName);
                //File db = new File(file.getParent());
                Uri data;
//              检测版本号是否大于等于24
                if (Build.VERSION.SDK_INT >= 24){

                    // File parentFlie = new File(db.getParent());
                    data = FileProvider.getUriForFile(SetActivity.this, "com.qindor.hsmobileservice.provider", db);
//              给目标应用一个临时授权
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    intent.setDataAndType(data, "application/vnd.android.package-archive");
                    //intent.setDataAndType(data, "*/Download");
                   // intent.addCategory(Intent.CATEGORY_OPENABLE);
                }else {
                    //db = new File(SetActivity.this.getExternalCacheDir(),newAppName);
                   // F/ile parentFlie = new File(db.getParent());
                    intent.setDataAndType(Uri.fromFile(db), "application/vnd.android.package-archive");
                    //intent.setDataAndType(Uri.fromFile(parentFlie), "*/Download");
                   // intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                pd.dismiss();
                Toast.makeText(SetActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
                startActivity(intent);
               // System.exit(0);
            }
            if(msg.what==4){
                int progress = msg.getData().getInt("progress");
                pd.setMessage("已经下载"+String.valueOf(progress)+"%");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.show();
            }
            if(msg.what==5){
                Toast.makeText(SetActivity.this,"下载失败",Toast.LENGTH_SHORT).show();
            }
        }
    };
    // * 获取本地软件版本号
//	 */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
    private void openAPK(String fileSavePath){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File db = null;
        Uri data;
//              检测版本号是否大于等于24
        if (Build.VERSION.SDK_INT >= 24){
            db = new File(SetActivity.this.getExternalCacheDir(),newAppName);
            data = FileProvider.getUriForFile(SetActivity.this, "com.qindor.hsmobileservice.provider", db);
//              给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(data, "application/vnd.android.package-archive");
        }else {
            db = new File(SetActivity.this.getExternalCacheDir(),newAppName);
            intent.setDataAndType(Uri.fromFile(db), "application/vnd.android.package-archive");
        }
       /* Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        File f = new File(SetActivity.this.getExternalCacheDir(),newAppName);
        intent.setDataAndType(Uri.fromFile(f),"application/vnd.android.package-archive");
        startActivity(intent);*/
    }
   Runnable startUpload = new Runnable() {
       @Override
       public void run() {
           progressDialog=new ProgressDialog(SetActivity.this);
           progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
           progressDialog.setMessage("正在下载新版本");
           progressDialog.setCancelable(false);//不能手动取消下载进度对话框
         // File db = new File(SetActivity.this.getExternalCacheDir(),newAppName);
           final String fileSavePath= SetActivity.this.getExternalCacheDir().toString()+"/"+newAppName;
           HTTPCaller.getInstance().downloadFile(downloadUrl,fileSavePath,null,new ProgressUIListener(){

               @Override
               public void onUIProgressStart(long totalBytes) {//下载开始
                   progressDialog.setMax((int)totalBytes);
                   progressDialog.show();
               }

               //更新进度
               @Override
               public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                   progressDialog.setProgress((int)numBytes);
               }

               @Override
               public void onUIProgressFinish() {//下载完成
                   Toast.makeText(SetActivity.this,"下载完成",Toast.LENGTH_LONG).show();
                   progressDialog.dismiss();
                   openAPK(fileSavePath);
               }
           });
       }
   };



    Runnable showUpdaloadDialog = new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
            // 设置提示框的标题
            builder.setTitle("版本升级").
                    setIcon(R.mipmap.ic_launcher). // 设置提示框的图标
                    setMessage("发现新版本！请及时更新").// 设置要显示的信息
                    setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.post(startUpload);//下载最新的版本程序
                }
            }).setNegativeButton("取消", null);//设置取消按钮,null是什么都不做，并关闭对话框
            AlertDialog alertDialog = builder.create();
            // 显示对话框
            alertDialog.show();
        }
    };



}
