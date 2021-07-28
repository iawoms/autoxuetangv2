package model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

public class SubmitExam {
    public String examArrangeID;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date hidCurrentSystemTime;
    public String hidRestTime = "00:00:00";
    public String info = "%5B%5D";
    public String offsetSeconds = "0";
    public String submitType = "0";
    public String timeStr = "start";
    public String uniqueId;
    public String userAllAnswers = "%5B%5D";
    public String userExamID;

    public SubmitExam(){

    }
    public SubmitExam(LWExam exam){
        examArrangeID = exam.ExamArrangeID;
        hidCurrentSystemTime = new Date();
        uniqueId = exam.uniqueId;
        userExamID = exam.UserExamID;
    }
}
