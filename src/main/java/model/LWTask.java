package model;

import com.alibaba.fastjson.JSON;

public class LWTask {
    public String token;
    public String referer;
    private String title;
    private String path;
    private String distcript;
    private String progress;
    private String taskType;
    private String masterID;
    private String knowledgeID;
    private String packageId;
    public String studyJson;
    public SubmitStudy study;
    public void genStudyJson() {
        study = new SubmitStudy();
        study.setKnowledgeId(knowledgeID);
        study.setMasterId(masterID);
        study.setPackageId(packageId);
        studyJson = JSON.toJSONString(study);
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDistcript() {
        return distcript;
    }

    public void setDistcript(String distcript) {
        this.distcript = distcript;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getMasterID() {
        return masterID;
    }

    public void setMasterID(String masterID) {
        this.masterID = masterID;
    }

    public String getKnowledgeID() {
        return knowledgeID;
    }

    public void setKnowledgeID(String knowledgeID) {
        this.knowledgeID = knowledgeID;
    }

    private String addcross(String v) {
        StringBuilder sb = new StringBuilder(v);
        sb.insert(8, "-");
        sb.insert(13, "-");
        sb.insert(18, "-");
        sb.insert(23, "-");
        return sb.toString();
    }
}