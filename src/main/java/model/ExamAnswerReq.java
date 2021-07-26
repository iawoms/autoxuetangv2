package model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class ExamAnswerReq {

    @JSONField(ordinal = 1)
    public List<Answer> answers;

    @JSONField(ordinal = 2)
    public int submitType;

    @JSONField(ordinal = 3)
    public int isHalfwayLeave;

    @JSONField(ordinal = 4)
    public int usedTime;

    @JSONField(ordinal = 5)
    public String uniqueId;

    public ExamAnswerReq() {

    }

    public ExamAnswerReq(Answer ans) {
        answers = new ArrayList<>();
        answers.add(ans);
    }
}
