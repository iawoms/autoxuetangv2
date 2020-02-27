package core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.LWPlan;
import model.LWTask;
import model.LWUser;
import model.LbspLogin;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tool.RobUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tool.RobUtils.*;

public class Rob {
    public static String LW_LOGIN_URL = "http://mss.linewell.com/lbsp/login.action";
    public static String LW_SINGLE_LOGIN_URL = "http://mss.linewell.com/lbsp/BaseAction.action?type=yunXueTang&act=single_sign_on";

    public static String DOMAIN = "https://linewelle-learning.yunxuetang.cn/";
    public static String XUE_STY = DOMAIN + "sty/index.htm";
    public static String XUE_MYSTUDYTASK = DOMAIN + "sty/mystudytask.htm";
    public static String XUE_MYPLANS = DOMAIN + "Services/MyIndexService.svc/GetTaskCenterPersonalStudyPlan";
    public static String XUE_GETEYPT = DOMAIN + "kng/services/KngComService.svc/GetEncryptRequest";
    public static String XUE_SUBMIT = "https://api-qidatestin.yunxuetang.cn/v1/study/submit?encryption=";

    HttpClient client;
    WebSocket webSocket;
    CookieManager cookieManager = new CookieManager();

    String usr;
    String pwd;
    String sessionid;
    String xue_session_loginurl;
    LWUser lwUser;
    List<LWPlan> planList = new ArrayList<>();

    public Rob(String usr, String pwd) {
        this.usr = usr;
        this.pwd = pwd;
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(120))
                .build();
    }

    public void close() {

    }


    public void login_lbsp() throws Exception {
        LbspLogin lbsp = new LbspLogin();
        lbsp.userName = usr;
        lbsp.password = DigestUtils.md5Hex(pwd);
        HttpRequest req = genPost(LW_LOGIN_URL, RobUtils.genPostBody(lbsp)).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        System.out.println(resp.body());
        JSONObject datamap = JSON.parseObject(resp.body());
        String errorMsg = datamap.getString("errorMsg");
        if (StringUtils.isNotEmpty(errorMsg)) {
            throw new Exception(errorMsg);
        } else {
            JSONObject session = datamap.getJSONObject("session");
            sessionid = session.get("sessionId").toString();
            System.out.println("sessionid : " + sessionid);
        }
    }

    public void singleLogin() throws Exception {
        String payLoadLogin = "{\"userUnid\":\"" + sessionid + "\"}";
        HttpRequest req = RobUtils.genPost(LW_SINGLE_LOGIN_URL, payLoadLogin)
                .setHeader("Content-type", "application/json;charset=UTF-8")
                .build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        System.out.println(resp.body());
        JSONObject datamap = JSON.parseObject(resp.body());
        JSONObject result = datamap.getJSONObject("result");
        String xueurl = result.getString("data");
        xue_session_loginurl = xueurl;
    }

    public void loginToXueTang() throws Exception {
        HttpRequest req = genGet(xue_session_loginurl).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        System.out.println(resp.body());
    }

    public void loadUserInfo() throws Exception {
        HttpRequest req = genGet(XUE_STY).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        System.out.println(body);
        ByteArrayInputStream is = new ByteArrayInputStream(body.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("var arr = '{\"orgID\"")) {
                lwUser = new LWUser(line);
            }
        }
        System.out.println(lwUser);
    }

    public void loadStudyPlan() throws Exception {
        HttpRequest req = genGet(XUE_MYSTUDYTASK).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String data = resp.body();
        Document doc = Jsoup.parse(data);
        Elements els = doc.getElementsByClass("el-plan-list");
        for (Element el : els) {
            Elements lis = el.getElementsByTag("li");
            for (Element li : lis) {
                String click = li.attr("onclick");
                String href = click.replace("learningKnowledge(\"", "").replace("\")", "");
                LWPlan plan = new LWPlan();
                plan.setUrl(href);
                Elements ellipsises = li.getElementsByClass("ellipsis");
                for (Element ellipsise : ellipsises) {
                    plan.setName(ellipsise.text());
                }
                planList.add(plan);
            }
        }
    }

    public void loadPlanTask(LWPlan plan) throws Exception {
        String url = DOMAIN + plan.getUrl();
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
//        System.out.println(body);
        Document doc = Jsoup.parse(body);
        Elements tbs = doc.getElementsByClass("table1");
        for (Element tb : tbs) {
            Elements trs = tb.getElementsByTag("tr");
            for (Element tr : trs) {
                String onclick = tr.attr("onclick");
                if (StringUtils.isNotEmpty(onclick)) {
                    Element span = tr.getElementsByTag("span").get(3);
                    System.out.println(span.attr("title"));

//                    return StudyRowClick('/plan/package/6c694efff54e439d9364950782466487_699e727586724cecbceb1cd5022c9e98.html','CourseKnowledge','','False', 'False','False','False',0,1,'','acc78727-26f4-48cb-8a7f-f64e7c486918','6a45330b-ba38-479a-bbd2-d826a624d02e');
                    String taskurl = RobUtils.cutStr(onclick, "StudyRowClick('", "','");
                    System.out.println(taskurl);
                    explanTask(taskurl);
                }
            }
        }
    }

    public List<LWTask> explanTask(String taskurl) throws Exception {
        String url = DOMAIN + "kng" + taskurl;
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        List<LWTask> list = new ArrayList<>();
        Document doc = Jsoup.parse(body);
        Element course = doc.getElementById("divcourselist");
        Elements links = course.getElementsByTag("a");
        for (Element ln : links) {
            System.out.println(ln.attr("title"));
            LWTask task = new LWTask();
            task.setTitle(ln.attr("title"));
            String path = ln.attr("href");
//            javascript:void(StudyRowClick('/package/ebook/8fbac16704f5460881af2e2f79f0648b_2fedb53090c24268bad8bba85c031a1e.html?MasterID=6c694eff-f54e-439d-9364-950782466487&MasterType=Plan','CourseKnowledge','','False', 'True','True',''));
            task.setPath(RobUtils.cutStr(path, "StudyRowClick('", "?"));
            task.setMasterID(RobUtils.cutStr(path, "MasterID=", "&"));

            openTaskPage(task);
            learnTask(task);
            list.add(task);
        }
        return list;
    }

    public void openTaskPage(LWTask task) throws IOException, InterruptedException {
        String url = DOMAIN + "kng/course" + task.getPath();
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        Document doc = Jsoup.parse(body);
        Element selected = doc.getElementsByClass("active select").get(0);
        String knid = selected.id();
        task.setKnowledgeID(knid);
        Element liFavorite = doc.getElementById("liFavorite");
        Element span = liFavorite.getElementsByTag("span").get(0);
        String idstr = RobUtils.cutStr(span.attr("onclick"), "CheckAddToFavorite(", ");");
        String[] ids = idstr.split(",");
        task.setPackageId(ids[3].replace("'", ""));
        task.referer = url + "?MasterID=" + task.getMasterID() + "&MasterType=Plan";
        Element tokeninput = doc.getElementById("hidIndexPage");
        String tokenstr = tokeninput.val();
        String token = RobUtils.cutStr(tokenstr, "token%3d", "%26productid");
        task.token = token;
        task.genStudyJson();
    }

    public String getEncrypt(LWTask task) throws Exception {
        String url = XUE_GETEYPT;
        HttpRequest req = genTextjsonPost(url, task.studyJson)
                .header("x-requested-with", "XMLHttpRequest")
                .header("x-tingyun-id", "_hoVbWfXnGI;r=770705243")
                .header("referer", task.referer)
                .build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        if (body.startsWith("\"") && body.endsWith("\"")) {
            body = body.substring(1, body.length() - 1);
        }
        System.out.println(body);
        JSONObject jo = JSON.parseObject(body);
        if (StringUtils.equalsIgnoreCase(jo.get("Status").toString(), "OK")) {
            return jo.get("Data").toString();
        }
        throw new Exception("getEncrypt error : " + body);
    }

    public void learnTask(LWTask task) throws Exception {
        String encrypt = getEncrypt(task);
        String submiturl = XUE_SUBMIT + encrypt;
        HttpRequest req = genAppjsonPost(submiturl, task.studyJson)
                .header("token", task.token)
                .header("referer", task.referer)
                .header("Origin", "https://linewelle-learning.yunxuetang.cn")
                .build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        System.out.println(body);
    }

    public void runStudy() throws Exception {
        login_lbsp();
        singleLogin();
        loginToXueTang();
        loadUserInfo();
        loadStudyPlan();
        System.out.println(planList.size());
        for (LWPlan plan : planList) {
            loadPlanTask(plan);
        }
    }

    public static void main(String[] args) throws Exception {

        Rob rob = new Rob("hcanrong", "123457");
        rob.runStudy();
    }

}

class SimpleRespHlr implements HttpResponse.BodyHandler<String> {

    @Override
    public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo responseInfo) {
        int code = responseInfo.statusCode();
        if (code == 200) {
            return HttpResponse.BodySubscribers.ofString(UTF_8);
        } else if (code == 302) {
            List<String> loc = responseInfo.headers().allValues("location");
            return HttpResponse.BodySubscribers.replacing("code " + responseInfo.statusCode() + " location " + loc.get(0));
        } else {
            return HttpResponse.BodySubscribers.replacing("ERROR " + responseInfo.statusCode());
        }
    }
}
