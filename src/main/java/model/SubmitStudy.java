package model;

public class SubmitStudy {
    private String knowledgeId = "";
    private String masterId = "";
    private String masterType = "Plan";
    private String packageId = "";
    private int pageSize = 1;
    private int studySize = 1;
    private int studyTime = 120;
    private int type = 0;
    private boolean offLine = false;
    private boolean end = true;
    private boolean care = true;
    private String deviceId = "";
    private String studyChapterIds = "";
    private int viewSchedule = 0;

    public String getKnowledgeId() {
        return knowledgeId;
    }

    public void setKnowledgeId(String knowledgeId) {
        this.knowledgeId = knowledgeId;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getMasterType() {
        return masterType;
    }

    public void setMasterType(String masterType) {
        this.masterType = masterType;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getStudySize() {
        return studySize;
    }

    public void setStudySize(int studySize) {
        this.studySize = studySize;
    }

    public int getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(int studyTime) {
        this.studyTime = studyTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isOffLine() {
        return offLine;
    }

    public void setOffLine(boolean offLine) {
        this.offLine = offLine;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public boolean isCare() {
        return care;
    }

    public void setCare(boolean care) {
        this.care = care;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStudyChapterIds() {
        return studyChapterIds;
    }

    public void setStudyChapterIds(String studyChapterIds) {
        this.studyChapterIds = studyChapterIds;
    }

    public int getViewSchedule() {
        return viewSchedule;
    }

    public void setViewSchedule(int viewSchedule) {
        this.viewSchedule = viewSchedule;
    }
}
