package model;

import org.jsoup.nodes.Element;

public class LiAnswer {
    public String type;
    public String dataType;
    public String qid;
    public String value;
    public String text;

    public LiAnswer(Element ele) {
        type = ele.attr("type");
        dataType = ele.attr("data-type");
        qid = ele.attr("name");
        value = ele.attr("value");
    }
}
