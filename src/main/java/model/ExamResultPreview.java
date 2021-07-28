package model;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ExamResultPreview {
    public String examName;
    public boolean isPassed;
    public String score;

    public List<CardQuesRow> quesResults;

    public ExamResultPreview(Document doc) {
        examName = doc.getElementById("lblExamName").text();
        score = doc.getElementById("lblScore").text();

        doc.getElementsByClass("my-exam-state").forEach(state -> {
            String pastr = state.attr("class");
            isPassed = StringUtils.containsIgnoreCase(pastr, "pass") && !StringUtils.containsIgnoreCase(pastr, "not-pass");
        });
        Elements subjectBoxs = doc.getElementsByClass("exam-subject-box");
        quesResults = new ArrayList<>();
        for (Element box : subjectBoxs) {
            CardQuesRow row = new CardQuesRow(box);
            quesResults.add(row);
            System.out.println(row);
        }
    }

    @Override
    public String toString() {
        return "ExamResultPreview{" +
                "examName='" + examName + '\'' +
                ", isPassed=" + isPassed +
                ", score='" + score + '\'' +
                ", quesResults=" + quesResults +
                '}';
    }
}
