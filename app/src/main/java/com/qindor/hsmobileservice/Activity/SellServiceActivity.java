package com.qindor.hsmobileservice.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qindor.hsmobileservice.Adpater.SellServiceAdpater;
import com.qindor.hsmobileservice.Model.BaseModel;
import com.qindor.hsmobileservice.Model.ProjectAndPlistModel;
import com.qindor.hsmobileservice.Model.ProjectModel;
import com.qindor.hsmobileservice.Model.RoomsModel;
import com.qindor.hsmobileservice.Model.TechnicianModel;
import com.qindor.hsmobileservice.R;
import com.qindor.hsmobileservice.Utils.Configuration;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SellServiceActivity  extends AppCompatActivity implements View.OnClickListener{
    private Button b1,b2;
    private ImageButton m1;
    private Configuration configuration;
    private Spinner spinner;
    private Handler handler;
    private String msg;
    private Map map;
    private BaseModel baseModel;
    private RoomsModel roomModel;
    private String resultData,pro1,pro2;
    private List<TechnicianModel> technicianModels;
    private List<String> tslist;
    private ArrayAdapter<String> adapter;
    private TextView t1,p1,t2,p2;
    private boolean isDate = false;
    private ProjectAndPlistModel projectAndPlistModel;
    private SellServiceAdpater sellServiceAdpater;
    private ListView listView1,listView2;
    private SimpleAdapter saImageItems = null;
    private GridView gridView1,gridView2;
    private LoadingDialog dialog1;
    private LoadingDialog.Builder builder1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wristband_sell_service);
        init();
    }

    private void init() {
        builder1=new LoadingDialog.Builder(SellServiceActivity.this)
                .setMessage("加载中...")
                .setCancelable(false);
        dialog1=builder1.create();
        gridView1 = findViewById(R.id.SGridView);
        gridView2 = findViewById(R.id.SPGridView);
        b1 = findViewById(R.id.room_wristband_service_btn);
        b2 = findViewById(R.id.room_wristband_service_out_btn);
        m1 = findViewById(R.id.sell_page_back_btn);
        m1.setVisibility(View.VISIBLE);
        t1 = findViewById(R.id.room_wristband_service_iSZZC);
        p1 = findViewById(R.id.room_wristband_service_fSZDJ);
        t2 = findViewById(R.id.room_wristband_service_iJZZC);
        p2 = findViewById(R.id.room_wristband_service_fJZDJ);
        spinner = findViewById(R.id.service_t_sp);
        SharedPreferences sharedPreferences=getSharedPreferences("config",0);
        baseModel = new BaseModel(sharedPreferences.getString("ip",""),sharedPreferences.getString("store",""),sharedPreferences.getString("library",""),sharedPreferences.getString("mac",""),sharedPreferences.getString("port",""));
        isDate = sharedPreferences.getBoolean("isDate",false);
        Bundle bundle =this.getIntent().getExtras();
        roomModel = (RoomsModel) bundle.get("rooms");
        configuration = new Configuration();
        technicianModels = new ArrayList<>();
        handler = new Handler();
        tslist = new ArrayList<>();
        map = new HashMap();
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        m1.setOnClickListener(this);
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
            loadView(projectAndPlistModel.getProjectModels());
        }

    }

    private void loadView(List<ProjectModel> projectModels) {
        int length = projectModels.size();
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("sXMLX", projectModels.get(i).getsXMLX());
            lstImageItem.add(map);
        }
        lstImageItem = getSingle(lstImageItem);
        //生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new SimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.service_icon_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sXMLX"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.service_btn});
        //添加并且显示
        gridView1.setAdapter(saImageItems);
        final ArrayList<HashMap<String, Object>> finalLstImageItem = lstImageItem;
        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pro1 = finalLstImageItem.get(position).get("sXMLX").toString();
                for (int i=0;i< parent.getCount();i++)
                {
                    View v=parent.getChildAt(i);
                    if (position == i) {
                        TextView mChoosedTv = (TextView) v.findViewById(R.id.service_btn);
                        mChoosedTv.setBackgroundResource(R.drawable.shape_edit_back_green);
                    } else {
                        TextView mNormalTv = (TextView) v.findViewById(R.id.service_btn);
                        mNormalTv.setBackgroundResource(R.drawable.shape_ac_login_btn_back_fill);
                    }
                }
                handler.post(loadPro);
            }
        });
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
    Runnable loadPro = new Runnable() {
        @Override
        public void run() {
            setPData(projectAndPlistModel.getProjectModels());
        }
    };

    private void setPData(List<ProjectModel> projectModels) {
        int length = projectModels.size();
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            if (pro1.equals(projectModels.get(i).getsXMLX()))
            {
                map.put("sXMMC", projectModels.get(i).getsXMMC());
                lstImageItem.add(map);
            }
        }
        lstImageItem = getSingle(lstImageItem);
        //生成适配器的ImageItem 与动态数组的元素相对应
        saImageItems = new SimpleAdapter(this,
                lstImageItem,//数据来源
                R.layout.service_icon_items,//item的XML实现
                //动态数组与ImageItem对应的子项
                new String[]{"sXMMC"},
                //ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[]{R.id.service_btn});
        //添加并且显示
        gridView2.setAdapter(saImageItems);
        final ArrayList<HashMap<String, Object>> finalLstImageItem = lstImageItem;
        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pro2 = finalLstImageItem.get(position).get("sXMMC").toString();
                for (int i=0;i< parent.getCount();i++)
                {
                    View v=parent.getChildAt(i);
                    if (position == i) {
                        TextView mChoosedTv = (TextView) v.findViewById(R.id.service_btn);
                        mChoosedTv.setBackgroundResource(R.drawable.shape_edit_back_green);
                    } else {
                        TextView mNormalTv = (TextView) v.findViewById(R.id.service_btn);
                        mNormalTv.setBackgroundResource(R.drawable.shape_ac_login_btn_back_fill);
                    }
                }
                handler.post(loadTer);
            }
        });
    }
    Runnable loadTer = new Runnable() {

        @Override
        public void run() {
            getTData();
            for (ProjectModel p : projectAndPlistModel.getProjectModels()) {
                if (p.getsXMMC().equals(pro2)) {
                    t1.setText(p.getiSZZC());
                    p1.setText(p.getfSZDJ());
                    t2.setText(p.getiJZZC());
                    p2.setText(p.getfJZDJ());
                }
            }
        }
    };
    private void getPData() {
        //{"code":"getjxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        map.put("code","getjxm");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
        dialog1.show();
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"getjxm\",\"ret\":\"0\",\"msg\":[{\"sXMLX\":\"按摩类\",\"sXMMC\":\"推背\",\"iSZZC\":\"30\",\"fSZDJ\":\"80\",\"iJZZC\":\"15\",\"fJZDJ\":\"30\"},{\"sXMLX\":\"按摩类\",\"sXMMC\":\"按摩\",\"iSZZC\":\"30\",\"fSZDJ\":\"100\",\"iJZZC\":\"15\",\"fJZDJ\":\"50\"},{\"sXMLX\":\"洗浴类\",\"sXMMC\":\"足疗\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类1\",\"sXMMC\":\"足疗1\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类2\",\"sXMMC\":\"足疗2\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类3\",\"sXMMC\":\"足疗3\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类4\",\"sXMMC\":\"足疗4\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"},{\"sXMLX\":\"洗浴类5\",\"sXMMC\":\"足疗5\",\"iSZZC\":\"30\",\"fSZDJ\":\"50\",\"iJZZC\":\"15\",\"fJZDJ\":\"20\"}]}";
        dialog1.dismiss();
        returnedPData(resultData);
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
                handler.post(setPTData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    Runnable setPTData = new Runnable() {
        @Override
        public void run() {
            loadView(projectAndPlistModel.getProjectModels());
        }
    };
    private void getTData() {
        //{"code":"getjsl","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148"}}
        map.put("code","getjsl");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.put("msg",data);
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
                    if (pro2.substring(0,1).equals(jsonObject1.getString("sBM").substring(0,1)))
                    {
                        if (jsonObject1.getString("sZT").equals("空闲")) {
                            tslist.add(jsonObject1.getString("sXM"));
                        }
                    }
                }
                new TSpinnerTask().execute();
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
            tspinnerClick();
        }
    }


    private void tspinnerClick() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tslist);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        spinner.setAdapter(adapter);
        int k= adapter.getCount();
        for(int i=0;i<k;i++){
            if("自动分配".equals(adapter.getItem(i).toString())){
                spinner.setSelection(i,true);
                break;
            }
        }
    }

    /*private Handler phander = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (ProjectModel p : projectAndPlistModel.getProjectModels()) {
                if (p.getsXMMC().equals(pSpinner.getSelectedItem().toString())) {
                    t1.setText(p.getiSZZC());
                    p1.setText(p.getfSZDJ());
                    t2.setText(p.getiJZZC());
                    p2.setText(p.getfJZDJ());
                }
            }
            pSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            });
        }
    };*/
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.room_wristband_service_btn:
                if(pro2!=null) {
                    sell();
                }else {
                    msg = "请选择服务项目";
                    handler.post(toast);
                }
                break;
            case R.id.room_wristband_service_out_btn:
                back();
                break;
            case R.id.sell_page_back_btn:
                back();
                break;
        }
    }

    private void sell() {
        //{"code":"dodxm","msg":{"sMAC":"A8-1E-84-81-70-CD","sIP":"10.1.3.148","sWD":"WQT0182","sXM":"推背","sJS":"A001","sTH":"301",}}
        map.clear();
        map.put("code","getwdh");
        JSONObject data = new JSONObject();
        try {
            data.put("sMAC",baseModel.getMac());
            data.put("sIP",baseModel.getIp());
            data.put("sWD",roomModel.getModels().get(0).getsWDBH());
            // data.put("sXM",pSpinner.getSelectedItem().toString());
            if(!"自动分配".equals(spinner.getSelectedItem().toString())){
                for (TechnicianModel t: technicianModels)
                {
                    if (t.getsXM().equals(spinner.getSelectedItem().toString()))
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
        dialog1.show();
        //resultData = httpUtils.baseHttp(WristbandActivity.this,baseModel,"spring",map);
        //resultData = httpUtils.baseOkHttp(baseModel,userid,sKey,map);
        resultData = "{\"code\":\"dodxm\",\"ret\":\"0\",\"msg\":\"点服务成功\"}";
        dialog1.dismiss();
        returnedSData(resultData);
    }
    private void returnedSData(String resultData) {
        try {
            JSONObject jsonObject = new JSONObject(resultData);
            String ret = jsonObject.getString("ret");
            if (ret.equals("0"))
            {
                msg = jsonObject.getString("msg");
                handler.post(toast);
                Intent i = new Intent(SellServiceActivity.this, WristbandActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("rooms",roomModel);
                i.putExtras(bundle1);
                startActivity(i);
                finish();
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
        Intent i = new Intent(SellServiceActivity.this, WristbandActivity.class);
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
}
