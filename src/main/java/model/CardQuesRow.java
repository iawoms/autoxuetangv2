package model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardQuesRow {
    public String goQuesId;
    public  int quesNum;
    public boolean isCorrected;
    public String quesText;
    public String rightanswer;
    public List<ExamChoise> choises;
    public CardQuesRow(Element subjectBox){
        try {
            subjectBox.getElementsByClass("exam-serial-number").forEach(h -> {
                quesNum = Integer.parseInt(h.text());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        Elements quesHash_a = subjectBox.getElementsByClass("ques-hash-a");
        quesHash_a.forEach(ele -> {
            goQuesId = ele.attr("name");
            return;
        });
        isCorrected = subjectBox.getElementsByClass("exam-icon-correct").size() > 0;
        subjectBox.getElementsByClass("exam-vignette-con").forEach(ele -> {
            quesText = ele.text().trim();
            return;
        });
        choises = new ArrayList<>();
        subjectBox.getElementsByAttributeValue("class","mt5 clearfix pl30").forEach(div -> {
            choises.add(new ExamChoise(div));
        });
        subjectBox.getElementsByClass("rightanswer").forEach(ele -> {
            rightanswer = ele.text().replace("：","").trim();
            return;
        });
        for (ExamChoise c : choises) {
            c.isRightAnswer = StringUtils.containsIgnoreCase(rightanswer,c.cho);
        }
    }
    public boolean isRightAnswer(String text){
        for (ExamChoise c : choises) {
            if(c.text.equalsIgnoreCase(text)&&c.isRightAnswer){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CardQuesRow{" +
                "quesNum=" + quesNum +
                ", goQuesId='" + goQuesId + '\'' +
                ", isCorrected=" + isCorrected +
                ", rightanswer='" + rightanswer + '\'' +
                '}';
    }
}
