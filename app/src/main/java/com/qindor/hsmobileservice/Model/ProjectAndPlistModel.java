package com.qindor.hsmobileservice.Model;

import java.io.Serializable;
import java.util.List;

public class ProjectAndPlistModel implements Serializable {
    private static final long serialVersionUID = -3766601091520870550L;
    private List<ProjectModel> projectModels;
    private List<String> pslist;

    public ProjectAndPlistModel() {
    }

    public ProjectAndPlistModel(List<ProjectModel> projectModels, List<String> pslist) {
        this.projectModels = projectModels;
        this.pslist = pslist;
    }

    public List<ProjectModel> getProjectModels() {
        return projectModels;
    }

    public void setProjectModels(List<ProjectModel> projectModels) {
        this.projectModels = projectModels;
    }

    public List<String> getPslist() {
        return pslist;
    }

    public void setPslist(List<String> pslist) {
        this.pslist = pslist;
    }
}
