package com.qindor.hsmobileservice.Model;

import java.io.Serializable;

public class InformationModel implements Serializable {

    private static final long serialVersionUID = -277201969790632535L;
    private String sTBH;
    private String sTMC;
    private String sTZT;
    private String sRTS;
    private String sNTS;

    public InformationModel() {
    }

    /**
     *
     * @param sTBH  台编号
     * @param sTMC  桑拿房
     * @param sTZT  台状态
     * @param sRTS  入台人数
     * @param sNTS  台容纳人数
     */
    public InformationModel(String sTBH, String sTMC, String sTZT, String sRTS, String sNTS) {
        this.sTBH = sTBH;
        this.sTMC = sTMC;
        this.sTZT = sTZT;
        this.sRTS = sRTS;
        this.sNTS = sNTS;
    }

    public String getsTBH() {
        return sTBH;
    }

    public void setsTBH(String sTBH) {
        this.sTBH = sTBH;
    }

    public String getsTMC() {
        return sTMC;
    }

    public void setsTMC(String sTMC) {
        this.sTMC = sTMC;
    }

    public String getsTZT() {
        return sTZT;
    }

    public void setsTZT(String sTZT) {
        this.sTZT = sTZT;
    }

    public String getsRTS() {
        return sRTS;
    }

    public void setsRTS(String sRTS) {
        this.sRTS = sRTS;
    }

    public String getsNTS() {
        return sNTS;
    }

    public void setsNTS(String sNTS) {
        this.sNTS = sNTS;
    }
}
