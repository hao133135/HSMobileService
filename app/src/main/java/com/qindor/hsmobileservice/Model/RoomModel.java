package com.qindor.hsmobileservice.Model;

import java.io.Serializable;

public class RoomModel implements Serializable {

    private String sTBH;//台编号
    private String sDWID;//定位ID;
    private String sWDBH;//腕带编号;
    private String sXMMC;//项目名称;
    private String fXMDJ;//项目单价;
    private String fSL;//项目数量;
    private String fXMJE;//姓名金额;
    private String sJSGH;//技师工号;
    private String sJSXM;//技师姓名;
    private String sZLX;//钟类型;
    private String sDateYMDHMSSZ;//上钟时间;
    private String sDateYMDHMSXZ;//下钟时间;
    private String iZSC;//钟时长（分钟）;
    private String iSY;//钟剩余时间（分钟）;
    public RoomModel() {
    }

    public RoomModel( String sDWID, String sWDBH, String sXMMC, String fXMDJ, String fSL, String fXMJE, String sJSGH, String sJSXM, String sZLX, String sDateYMDHMSSZ, String sDateYMDHMSXZ, String iZSC, String iSY) {
        this.sDWID = sDWID;
        this.sWDBH = sWDBH;
        this.sXMMC = sXMMC;
        this.fXMDJ = fXMDJ;
        this.fSL = fSL;
        this.fXMJE = fXMJE;
        this.sJSGH = sJSGH;
        this.sJSXM = sJSXM;
        this.sZLX = sZLX;
        this.sDateYMDHMSSZ = sDateYMDHMSSZ;
        this.sDateYMDHMSXZ = sDateYMDHMSXZ;
        this.iZSC = iZSC;
        this.iSY = iSY;
    }

    public String getsTBH() {
        return sTBH;
    }

    public void setsTBH(String sTBH) {
        this.sTBH = sTBH;
    }

    public String getsDWID() {
        return sDWID;
    }

    public void setsDWID(String sDWID) {
        this.sDWID = sDWID;
    }

    public String getsWDBH() {
        return sWDBH;
    }

    public void setsWDBH(String sWDBH) {
        this.sWDBH = sWDBH;
    }

    public String getsXMMC() {
        return sXMMC;
    }

    public void setsXMMC(String sXMMC) {
        this.sXMMC = sXMMC;
    }

    public String getfXMDJ() {
        return String.valueOf(Double.parseDouble(fXMDJ));
    }

    public void setfXMDJ(String fXMDJ) {
        this.fXMDJ = fXMDJ;
    }

    public String getfSL() {
        return fSL;
    }

    public void setfSL(String fSL) {
        this.fSL = fSL;
    }

    public String getfXMJE() {
        return String.valueOf(Double.parseDouble(fXMJE));
    }

    public void setfXMJE(String fXMJE) {
        this.fXMJE = fXMJE;
    }

    public String getsJSGH() {
        return sJSGH;
    }

    public void setsJSGH(String sJSGH) {
        this.sJSGH = sJSGH;
    }

    public String getsJSXM() {
        return sJSXM;
    }

    public void setsJSXM(String sJSXM) {
        this.sJSXM = sJSXM;
    }

    public String getsZLX() {
        return sZLX;
    }

    public void setsZLX(String sZLX) {
        this.sZLX = sZLX;
    }

    public String getsDateYMDHMSSZ() {
        return sDateYMDHMSSZ;
    }

    public void setsDateYMDHMSSZ(String sDateYMDHMSSZ) {
        this.sDateYMDHMSSZ = sDateYMDHMSSZ;
    }

    public String getsDateYMDHMSXZ() {
        return sDateYMDHMSXZ;
    }

    public void setsDateYMDHMSXZ(String sDateYMDHMSXZ) {
        this.sDateYMDHMSXZ = sDateYMDHMSXZ;
    }

    public String getiZSC() {
        return iZSC;
    }

    public void setiZSC(String iZSC) {
        this.iZSC = iZSC;
    }

    public String getiSY() {
        return iSY;
    }

    public void setiSY(String iSY) {
        this.iSY = iSY;
    }

}
