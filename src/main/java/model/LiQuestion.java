package model;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class LiQuestion {
    public String qt;
    public String qid;
    public String questionid;
    public String id;
    public String text;

    public List<LiAnswer> answers = new ArrayList<>();

    private int ansIdx = 0;

    public LiQuestion(Element eliq) {
        qt = eliq.attr("qt");
        qid = eliq.attr("qid");
        questionid = eliq.attr("questionid");
        id = eliq.attr("id");
        try {
            Elements rowdiv = eliq.getElementsByAttributeValue("class", "col-18 font-size-16");
            Elements divs = rowdiv.get(0).getElementsByTag("div");
            text = divs.get(0).text();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAnswer(LiAnswer ans) {
        answers.add(ans);
    }

    public List<String> nextCombinAns() {
        List<String> res = new ArrayList<>();
        res.add(answers.get(ansIdx).value);
        ansIdx++;
        return res;
    }
}
