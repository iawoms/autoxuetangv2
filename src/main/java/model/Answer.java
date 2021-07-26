package model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.List;

public class Answer {

    @JSONField(ordinal = 1)
    public String questionId;
    @JSONField(ordinal = 2)
    public String questionType;
    @JSONField(ordinal = 3)
    public List<String> answer;
    @JSONField(ordinal = 4)
    public String[] attach;
    @JSONField(ordinal = 5)
    public int submitLot = 0;
    @JSONField(ordinal = 6,format = "yyyy-MM-dd HH:mm:ss")
    public Date lastSubmitTime;

    public Answer() {

    }

    public Answer(LiQuestion que) {
        questionId = que.qid;
        questionType = que.qt;
        attach = new String[0];
        lastSubmitTime = new Date();
        answer = que.nextCombinAns();
    }

}
