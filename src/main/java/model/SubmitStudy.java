package model;

import com.alibaba.fastjson.annotation.JSONField;

public class SubmitStudy {
    @JSONField(ordinal = 1)
    public String knowledgeId = "";
    @JSONField(ordinal = 2)
    public String masterId = "";
    @JSONField(ordinal = 3)
    public String masterType = "Plan";
    @JSONField(ordinal = 4)
    public String packageId = "";
    @JSONField(ordinal = 5)
    public int pageSize = 1;
    @JSONField(ordinal = 6)
    public int studySize = 1;
    @JSONField(ordinal = 7)
    public int studyTime = 120;
    @JSONField(ordinal = 8)
    public int type = 0;
    @JSONField(ordinal = 9)
    public boolean offLine = false;
    @JSONField(ordinal = 10)
    public boolean end = true;
    @JSONField(ordinal = 11)
    public boolean care = true;
    @JSONField(ordinal = 12)
    public String deviceId = "";
    @JSONField(ordinal = 13)
    public String studyChapterIds = "";
    @JSONField(ordinal = 14)
    public int viewSchedule = 0;
//    @JSONField(ordinal = 15)
//    public int multiple ;
//    @JSONField(ordinal = 16)
//    public int realHour ;

}
