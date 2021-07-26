package model;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class LWExam {
   public String ExamArrangeID;
   public String ExamID;
   public String UserExamMapID;
   public String UserExamID;
   public String MasterId;
   public String MasterType;
   public String packageId;
   @JSONField(serialize = false)
   public String token;
   public List<LiQuestion> questions;


}
