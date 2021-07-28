package model;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
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
    public List<LiQuestion> questions;
    @JSONField(serialize = false)
    public String token;
    @JSONField(serialize = false)
    public String uniqueId;
    @JSONField(serialize = false)
    public String submitReferer;
    @JSONField(serialize = false)
    public ExamResultPreview resultPreview;
    @JSONField(serialize = false)
    public int redoTimes = 0;

    public List<String> genAnswer(LiQuestion liq) {
        List<String> comAns = new ArrayList<>();
        if (resultPreview != null && resultPreview.quesResults != null) {
            for (CardQuesRow res : resultPreview.quesResults) {
                if (res.quesText.equalsIgnoreCase(liq.text)) {
                    for (LiAnswer lian : liq.answers) {
                        if (res.isRightAnswer(lian.text)) {
                            comAns.add(lian.value);
                        }
                    }
                }
            }
        }
        if (comAns.size() > 0) {
            return comAns;
        }
        return liq.nextCombinAns();
    }

}
