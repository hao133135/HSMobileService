package com.qindor.hsmobileservice.Model;

import java.io.Serializable;

public class ProjectModel implements Serializable{
    private static final long serialVersionUID = 3434239728814739495L;
    private String sXMLX;
    private String sXMMC;
    private String iSZZC;
    private String fSZDJ;
    private String iJZZC;
    private String fJZDJ;

    public ProjectModel() {
    }

    public ProjectModel(String sXMLX, String sXMMC, String iSZZC, String fSZDJ, String iJZZC, String fJZDJ) {
        this.sXMLX = sXMLX;
        this.sXMMC = sXMMC;
        this.iSZZC = iSZZC;
        this.fSZDJ = fSZDJ;
        this.iJZZC = iJZZC;
        this.fJZDJ = fJZDJ;
    }

    public String getsXMLX() {
        return sXMLX;
    }

    public void setsXMLX(String sXMLX) {
        this.sXMLX = sXMLX;
    }

    public String getsXMMC() {
        return sXMMC;
    }

    public void setsXMMC(String sXMMC) {
        this.sXMMC = sXMMC;
    }

    public String getiSZZC() {
        return iSZZC;
    }

    public void setiSZZC(String iSZZC) {
        this.iSZZC = iSZZC;
    }

    public String getfSZDJ() {
        return fSZDJ;
    }

    public void setfSZDJ(String fSZDJ) {
        this.fSZDJ = fSZDJ;
    }

    public String getiJZZC() {
        return iJZZC;
    }

    public void setiJZZC(String iJZZC) {
        this.iJZZC = iJZZC;
    }

    public String getfJZDJ() {
        return fJZDJ;
    }

    public void setfJZDJ(String fJZDJ) {
        this.fJZDJ = fJZDJ;
    }
}
