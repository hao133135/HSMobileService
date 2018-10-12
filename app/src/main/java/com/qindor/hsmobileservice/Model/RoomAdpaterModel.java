package com.qindor.hsmobileservice.Model;

public class RoomAdpaterModel {
    private String sWDBH;
    private String sXMMC;
    private String sDateYMDHMSSZ;

    public RoomAdpaterModel() {
    }

    public RoomAdpaterModel(String sWDBH, String sXMMC, String sDateYMDHMSSZ) {
        this.sWDBH = sWDBH;
        this.sXMMC = sXMMC;
        this.sDateYMDHMSSZ = sDateYMDHMSSZ;
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

    public String getsDateYMDHMSSZ() {
        return sDateYMDHMSSZ;
    }

    public void setsDateYMDHMSSZ(String sDateYMDHMSSZ) {
        this.sDateYMDHMSSZ = sDateYMDHMSSZ;
    }
}
