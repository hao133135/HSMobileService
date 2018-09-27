package com.qindor.hsmobileservice.Model;

import java.io.Serializable;

public class BaseModel implements Serializable {


    private String ip,libraryNum,storeNum,mac,port;

    public BaseModel() {
    }

    public BaseModel(String ip, String libraryNum, String storeNum, String mac, String port) {
        this.ip = ip;
        this.libraryNum = libraryNum;
        this.storeNum = storeNum;
        this.mac = mac;
        this.port = port;

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLibraryNum() {
        return libraryNum;
    }

    public void setLibraryNum(String libraryNum) {
        this.libraryNum = libraryNum;
    }

    public String getStoreNum() {
        return storeNum;
    }

    public void setStoreNum(String storeNum) {
        this.storeNum = storeNum;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
