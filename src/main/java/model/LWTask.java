package model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LWTask {
    public String token;
    public String referer;
    public String title;
    public String path;
    public String distcript;
    public String progress;
    public String taskType;
    public String masterID;
    public String knowledgeID;
    public String packageId;
    public String studyJson;
    public String studyChapterIds;
    public SubmitStudy study;
    public String encJson;
    public void genStudyJson() {
        study = new SubmitStudy();
        study.knowledgeId = knowledgeID;
        study.masterId = masterID;
        study.packageId = packageId;
        study.studyChapterIds = studyChapterIds;
        studyJson = JSON.toJSONString(study);
//        encJson = studyJson;
        Map<String,String> map = new HashMap<>();
        map.put("body",studyJson);
        encJson = JSON.toJSONString(map);
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