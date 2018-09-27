package com.qindor.hsmobileservice.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoomsModel implements Serializable{

    private static final long serialVersionUID = -9068187688817415698L;
    private List<RoomModel> models = new ArrayList<>();
    private String code;

    public RoomsModel() {
    }

    public RoomsModel(List<RoomModel> models, String code) {
        this.models = models;
        this.code = code;
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
