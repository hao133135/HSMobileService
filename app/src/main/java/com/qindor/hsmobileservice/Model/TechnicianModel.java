package com.qindor.hsmobileservice.Model;

public class TechnicianModel {
    private String sGH;
    private String sXM;
    private String sBM;
    private String sGZ;
    private String sJB;
    private String sZT;
    private String sXB;

    public TechnicianModel() {
    }

    public TechnicianModel(String sGH, String sXM, String sBM, String sGZ, String sJB, String sZT, String sXB) {
        this.sGH = sGH;
        this.sXM = sXM;
        this.sBM = sBM;
        this.sGZ = sGZ;
        this.sJB = sJB;
        this.sZT = sZT;
        this.sXB = sXB;
    }

    public String getsGH() {
        return sGH;
    }

    public void setsGH(String sGH) {
        this.sGH = sGH;
    }

    public String getsXM() {
        return sXM;
    }

    public void setsXM(String sXM) {
        this.sXM = sXM;
    }

    public String getsBM() {
        return sBM;
    }

    public void setsBM(String sBM) {
        this.sBM = sBM;
    }

    public String getsGZ() {
        return sGZ;
    }

    public void setsGZ(String sGZ) {
        this.sGZ = sGZ;
    }

    public String getsJB() {
        return sJB;
    }

    public void setsJB(String sJB) {
        this.sJB = sJB;
    }

    public String getsZT() {
        return sZT;
    }

    public void setsZT(String sZT) {
        this.sZT = sZT;
    }

    public String getsXB() {
        return sXB;
    }

    public void setsXB(String sXB) {
        this.sXB = sXB;
    }
}
