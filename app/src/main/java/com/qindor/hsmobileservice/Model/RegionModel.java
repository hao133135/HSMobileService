package com.qindor.hsmobileservice.Model;

public class RegionModel {
    private String sQYH;
    private String sQYM;

    public RegionModel() {
    }

    /**
     *
     * @param sQYH 区域编号
     * @param sQYM  区域名称
     */
    public RegionModel(String sQYH, String sQYM) {
        this.sQYH = sQYH;
        this.sQYM = sQYM;
    }

    public String getsQYH() {
        return sQYH;
    }

    public void setsQYH(String sQYH) {
        this.sQYH = sQYH;
    }

    public String getsQYM() {
        return sQYM;
    }

    public void setsQYM(String sQYM) {
        this.sQYM = sQYM;
    }
}
