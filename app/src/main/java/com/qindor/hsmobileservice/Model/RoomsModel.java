package com.qindor.hsmobileservice.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoomsModel implements Serializable{

    private static final long serialVersionUID = -9068187688817415698L;
    private List<RoomModel> models = new ArrayList<>();
    private InformationModel informationModel = new InformationModel();
    private String code;

    public RoomsModel() {
    }

    public RoomsModel(List<RoomModel> models, String code) {
        this.models = models;
        this.code = code;
    }

    public RoomsModel(List<RoomModel> models,InformationModel informationModel, String code) {
        this.models = models;
        this.informationModel = informationModel;
        this.code = code;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public InformationModel getInformationModel() {
        return informationModel;
    }

    public void setInformationModel(InformationModel informationModel) {
        this.informationModel = informationModel;
    }

    public List<RoomModel> getModels() {
        return models;
    }

    public void setModels(List<RoomModel> models) {
        this.models = models;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
