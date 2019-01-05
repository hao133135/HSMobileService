/*
package com.qindor.hsmobileservice.Utils;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.device.PrinterManager;
import android.text.TextUtils;

import com.qmx.iclient.AppContext;
import com.qmx.iclient.bean.User;
import com.qmx.iclient.lease.bean.LeaseBean;
import com.qmx.iclient.lease.bean.PaySuccessBean;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import static com.qmx.iclient.lease.view.LeaseListFragment.PRINTSUCCESSCODE;
import static com.qmx.iclient.lease.view.LeasePaySuccessActivity.PAYSUCCESSMSG;

public class LeasePrintBillService extends IntentService {

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private PrinterManager printer;

    public LeasePrintBillService() {
        super("bill");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        printer = new PrinterManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PaySuccessBean mPaySuccessBean = intent.getParcelableExtra("mPaySuccessBean");

        if (mPaySuccessBean == null) {
            EventBus.getDefault().post(new EventMsg(PRINTSUCCESSCODE, "打印失败"));
            return;
        }
        try {


            int openState =printer.getStatus();
            if (openState != 0) {
                EventBus.getDefault().post(new EventMsg(PRINTSUCCESSCODE, "打印失败"));
                return;
            }
            printer.prn_open();
            printer.prn_setupPage(380, -1);
            doPrintRentInfo(mPaySuccessBean);
//        printer.prn_drawTextEx("租赁信息\n租赁信息租赁信息租赁信息租赁信息租赁信息\n租赁信息租赁信息租赁信息\n\n\n\n", 0, 0,360,-1, "宋体", 28, 0,0x0000, 0);
            printer.prn_printPage(0);
            printer.prn_close();

            EventBus.getDefault().post(new EventMsg(PRINTSUCCESSCODE, PAYSUCCESSMSG));
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new EventMsg(PRINTSUCCESSCODE, "打印失败"));
        }
    }


    private void doPrintRentInfo(PaySuccessBean mPaySuccessBean) {
        if (mPaySuccessBean == null) {

            EventBus.getDefault().post(new EventMsg(PRINTSUCCESSCODE, "打印失败"));
        }
        LeaseBean u = mPaySuccessBean.getmLeaseBean();
        // 标准打印，每个字符打印所占位置可能有一点出入（尤其是英文字符）
        String mediumSpline = "";
        for (int i = 0; i < 45; i++) {
            mediumSpline += "-";
        }


        int width = 360;
        String frontName = "宋体";
        int frontType = 0x0000;

        int frontSize = 24;
        printer.prn_drawTextEx("租赁信息", 100, 0, width, -1, frontName, 40, 0, frontType, 0);

        StringBuffer sb = new StringBuffer();
        sb.append("\n设备名称：");
        sb.append(DataDispose.isNull(u.getRentname()));
        sb.append("\n");
        sb.append("设备编号：");
        sb.append(DataDispose.isNull(u.getRentnum()));
        sb.append("\n");
        sb.append("操 作 员：");
        AppContext ac = (AppContext) this.getApplication();
        User user = ac.getLoginInfo();
        sb.append(user.getName());
        sb.append("\n");

        //分割线
        sb.append(mediumSpline);
        sb.append("\n");

        sb.append("押　　金：");
        sb.append(DataDispose.isNull(DataDispose.twoDecimalPlaces(mPaySuccessBean.getYjjg())));
        sb.append("\n");

        sb.append("开始时间：");
        sb.append(DataDispose.isNull(mPaySuccessBean.getDta()));
        sb.append("\n");

        sb.append("结束时间：");
        sb.append(DataDispose.isNull(mPaySuccessBean.getDtb()));
        sb.append("\n");

        //分割线
        sb.append(mediumSpline);
        sb.append("\n");


        if (TextUtils.equals(u.getState(), "0")) {

            sb.append("交易时间：");
            sb.append(DataDispose.isNull(mPaySuccessBean.getTradetime()));
            sb.append("\n");


            sb.append("实　　收：");
            sb.append(DataDispose.isNull(DataDispose.twoDecimalPlaces(mPaySuccessBean.getYjjg())));
            sb.append("\n");

        }

        if (TextUtils.equals(u.getState(), "1")) {
            sb.append("消费金额：");
            sb.append(DataDispose.isNull(DataDispose.twoDecimalPlaces(mPaySuccessBean.getCostmoney())));
            sb.append("\n");
            //  0、退钱  1、补钱
            if (TextUtils.equals(mPaySuccessBean.getCoststate(), "1")) {
                sb.append("补交金额：");
                sb.append(DataDispose.isNull(DataDispose.twoDecimalPlaces(mPaySuccessBean.getContinueMoney())));
                sb.append("\n");
            }

            if (TextUtils.equals(mPaySuccessBean.getCoststate(), "0")) {
                sb.append("退款金额：");
                sb.append(DataDispose.isNull(DataDispose.twoDecimalPlaces(mPaySuccessBean.getRefundMoney())));
                sb.append("\n");
            }

        }
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        printer.prn_drawTextEx(sb.toString(), 0, 50, width, -1, frontName, frontSize, 0, frontType, 0);
    }


    */
/**
     * 计算空格
     *
     * @param size
     * @return
     *//*

    public static String getBlankBySize(int size) {
        String resultStr = "";
        for (int i = 0; i < size; i++) {
            resultStr += " ";
        }
        return resultStr;
    }


    */
/**
     * 获取数据长度
     *
     * @param msg
     * @return
     *//*

    @SuppressLint("NewApi")
    public static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

}
*/
