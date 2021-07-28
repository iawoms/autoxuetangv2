package model;


import org.jsoup.nodes.Element;

public class ExamChoise {
    public String cho;
    public String text;
    public boolean isRightAnswer;
    public ExamChoise(Element choDiv){
        choDiv.getElementsByTag("h3").forEach(element -> {
            cho = element.text().replace("、","").trim();
            return;
        });
        choDiv.getElementsByTag("div").forEach(element -> {
            text = element.text().trim();
            return;
        });
    }
}
