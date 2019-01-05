package com.qindor.hsmobileservice.Model;

public class RoomAdpaterModel {
    private String sJSGH;
    private String sXMMC;
    private String sDateYMDHMSSZ;
    private int state;
    private String sDWID;
    private String sZT;

    public RoomAdpaterModel() {
    }

    public RoomAdpaterModel(String sJSGH, String sXMMC, String sDateYMDHMSSZ, int state,String sDWID,String sZT) {
        this.sJSGH = sJSGH;
        this.sXMMC = sXMMC;
        this.sDateYMDHMSSZ = sDateYMDHMSSZ;
        this.state = state;
        this.sDWID = sDWID;
        this.sZT = sZT;
    }

    public String getsJSGH() {
        return sJSGH;
    }

    public void setsJSGH(String sJSGH) {
        this.sJSGH = sJSGH;
    }

    public String getsXMMC() {
        return sXMMC;
    }

    public void setsXMMC(String sXMMC) {
        this.sXMMC = sXMMC;
    }

    public String getsDateYMDHMSSZ() {
        return sDateYMDHMSSZ;
    }

    public void setsDateYMDHMSSZ(String sDateYMDHMSSZ) {
        this.sDateYMDHMSSZ = sDateYMDHMSSZ;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getsDWID() {
        return sDWID;
    }

    public void setsDWID(String sDWID) {
        this.sDWID = sDWID;
    }

    public String getsZT() {
        return sZT;
    }

    public void setsZT(String sZT) {
        this.sZT = sZT;
    }
}
