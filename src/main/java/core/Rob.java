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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tool.RobUtils.*;

public class Rob {
//    public static String LW_DOMAIN = "http://59.61.216.122";
    public static String LW_DOMAIN = "http://mss.linewell.com";
    public static String LW_LOGIN_URL = LW_DOMAIN + "/lbsp/login.action";
    public static String LW_SINGLE_LOGIN_URL = LW_DOMAIN + "/lbsp/BaseAction.action?type=yunXueTang&act=single_sign_on";

    public static String DOMAIN = "https://linewelle-learning.yunxuetang.cn/";
    public static String XUE_STY = DOMAIN + "sty/index.htm";
    public static String XUE_MYSTUDYTASK = DOMAIN + "sty/mystudytask.htm";
    public static String XUE_MYPLANS = DOMAIN + "Services/MyIndexService.svc/GetTaskCenterPersonalStudyPlan";
    public static String XUE_GETEYPT = DOMAIN + "kng/services/KngComService.svc/GetEncryptRequest";
    public static String XUE_SUBMIT = "https://api-qidatestin.yunxuetang.cn/v1/study/submit?encryption=";

    HttpClient client;
    CookieManager cookieManager = new CookieManager();

    private String usr;
    private String pwd;
    private String sessionid;
    private String xue_session_loginurl;
    private String shareToken;
    LWUser lwUser;
    List<LWPlan> planList = new ArrayList<>();
    LogHandle logHandle;

    public Rob(String usr, String pwd) {
        this(usr, pwd, null);
    }

    public Rob(String usr, String pwd, LogHandle logHandle) {
        this.usr = usr;
        this.pwd = pwd;
        if (logHandle == null) {
            this.logHandle = new LogHandle() {
                @Override
                public void sendLog(Object msg) {
                    System.out.println(msg);
                }

                @Override
                public void finish() {

                }
            };
        } else {
            this.logHandle = logHandle;
        }
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(120))
                .build();

    }

    public void close() {
    }


    public void login_lbsp() throws Exception {
        logHandle.sendLog("login to mss ..");
        LbspLogin lbsp = new LbspLogin();
        lbsp.userName = usr;
        lbsp.password = DigestUtils.md5Hex(pwd);
        HttpRequest req = genPost(LW_LOGIN_URL, RobUtils.genPostBody(lbsp)).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        JSONObject datamap = JSON.parseObject(resp.body());
        String errorMsg = datamap.getString("errorMsg");
        if (StringUtils.isNotEmpty(errorMsg)) {
            throw new Exception(errorMsg);
        } else {
            JSONObject session = datamap.getJSONObject("session");
            sessionid = session.get("sessionId").toString();
            logHandle.sendLog("sessionid : " + sessionid);
        }
    }

    public void singleLogin() throws Exception {
        logHandle.sendLog("load xue url ...");
        String payLoadLogin = "{\"userUnid\":\"" + sessionid + "\"}";
        HttpRequest req = RobUtils.genPost(LW_SINGLE_LOGIN_URL, payLoadLogin)
                .setHeader("Content-type", "application/json;charset=UTF-8")
                .build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        JSONObject datamap = JSON.parseObject(resp.body());
        JSONObject result = datamap.getJSONObject("result");
        String xueurl = result.getString("data");
        xue_session_loginurl = xueurl;
    }

    public void loginToXueTang() throws Exception {
        logHandle.sendLog("jumping to xuetang");
        HttpRequest req = genGet(xue_session_loginurl).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        logHandle.sendLog(resp.statusCode());
        logHandle.sendLog(resp.body());
    }

    public void loadUserInfo() throws Exception {
        HttpRequest req = genGet(XUE_STY).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
//        logHandle.sendLog(body);
        ByteArrayInputStream is = new ByteArrayInputStream(body.getBytes());
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("var arr = '{\"orgID\"")) {
                    lwUser = new LWUser(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }
        logHandle.sendLog(lwUser);
    }

    public void loadStudyPlan() throws Exception {
        logHandle.sendLog("loading my plan..");
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
                    logHandle.sendLog(plan.getName());
                }
                planList.add(plan);
            }
        }
    }

    public void loadPlanTask(LWPlan plan) throws Exception {
        logHandle.sendLog("loading my tasks ...");
        String url = DOMAIN + plan.getUrl();
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        Document doc = Jsoup.parse(body);
        Elements tbs = doc.getElementsByClass("table1");
        for (Element tb : tbs) {
            Elements trs = tb.getElementsByTag("tr");
            for (Element tr : trs) {
                String onclick = tr.attr("onclick");
                if (StringUtils.isNotEmpty(onclick)) {
                    Element span = tr.getElementsByTag("span").get(3);
                    String title = span.attr("title");
                    logHandle.sendLog(title);
//                    return StudyRowClick('/plan/package/6c694efff54e439d9364950782466487_699e727586724cecbceb1cd5022c9e98.html','CourseKnowledge','','False', 'False','False','False',0,1,'','acc78727-26f4-48cb-8a7f-f64e7c486918','6a45330b-ba38-479a-bbd2-d826a624d02e');
//                    String taskurl = RobUtils.cutStr(onclick, "StudyRowClick('", "','");
                    String clpath = RobUtils.cutStr(onclick, "StudyRowClick('", ")");
                    String[] clps = clpath.split("','");
                    String taskurl =  clps[0];
                    String taskType = clps[1];
                    if(taskType.equalsIgnoreCase("DocumentKnowledge")){
                        logHandle.sendLog("Doc knowledge not sported : [" + title + "]");
                    }else {
                        logHandle.sendLog(taskurl);
                        List<LWTask> tasks = explanTask(taskurl);
                        if (tasks.size() == 0) {
                            LWTask task = new LWTask();
                            task.setTitle(title);
                            task.setPath("kng" + taskurl);
                            openTaskPage(task);
                        }
                    }
                }
            }
        }
    }

    public List<LWTask> explanTask(String taskurl) throws Exception {
        logHandle.sendLog("check next level ...");
        String url = DOMAIN + "kng" + taskurl;
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        List<LWTask> list = new ArrayList<>();
        Document doc = Jsoup.parse(body);
        Element course = doc.getElementById("divcourselist");
        if (course != null) {
            Elements links = course.getElementsByTag("a");
            for (Element ln : links) {
                logHandle.sendLog(ln.attr("title"));
                LWTask task = new LWTask();
                task.setTitle(ln.attr("title"));
                String path = ln.attr("href");
//            javascript:void(StudyRowClick('/package/ebook/8fbac16704f5460881af2e2f79f0648b_2fedb53090c24268bad8bba85c031a1e.html?MasterID=6c694eff-f54e-439d-9364-950782466487&MasterType=Plan','CourseKnowledge','','False', 'True','True',''));
                task.setPath("kng/course" + RobUtils.cutStr(path, "StudyRowClick('", "','"));
                task.setMasterID(RobUtils.cutStr(path, "MasterID=", "&"));
                openTaskPage(task);
                list.add(task);
            }
        }
        return list;
    }

    public void openTaskPage(LWTask task) throws Exception {
        logHandle.sendLog("openning task page ...");
        String url = DOMAIN + task.getPath();
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        Document doc = Jsoup.parse(body);
//        logHandle.sendLog("loading knid ...");
//        Element selected = doc.getElementsByClass("active select").get(0);
//        String knid = selected.id();
//        task.setKnowledgeID(knid);
        logHandle.sendLog("loading packid ...");
        Element liFavorite = doc.getElementById("liFavorite");
        if(liFavorite == null){
            logHandle.sendLog(task.getTitle() + " load faild ! skip ...");
            return;
        }
        Element span = liFavorite.getElementsByTag("span").get(0);
        String idstr = RobUtils.cutStr(span.attr("onclick"), "CheckAddToFavorite(", ");");
        String[] ids = idstr.split(",");
        task.setKnowledgeID(ids[2].replace("'", ""));
        task.setPackageId(ids[3].replace("'", ""));
        task.setMasterID(ids[4].replace("'", ""));
        task.referer = url + "?MasterID=" + task.getMasterID() + "&MasterType=Plan";
        logHandle.sendLog("loading token ...");
        Element tokeninput = doc.getElementById("hidIndexPage");
        if (tokeninput == null) {
            String tokenstr = RobUtils.findLine(body, "token:");
            String token = RobUtils.cutStr(tokenstr, "token: '", "',");
            task.token = token;
        } else {
            String tokenstr = tokeninput.val();
            String token = RobUtils.cutStr(tokenstr, "token%3d", "%26productid");
            task.token = token;
        }

        logHandle.sendLog("gen enc json ...");
        task.genStudyJson();
        learnTask(task);
    }

    public String getEncrypt(LWTask task) throws Exception {
        logHandle.sendLog("loading enc ...");
        String url = XUE_GETEYPT;
        HttpRequest req = genTextjsonPost(url, task.encJson)
                .header("x-requested-with", "XMLHttpRequest")
                .header("x-tingyun-id", "_hoVbWfXnGI;r=219510364")
                .header("referer", task.referer)
                .build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        if (body.startsWith("\"") && body.endsWith("\"")) {
            body = body.substring(1, body.length() - 1);
        }
        logHandle.sendLog(body);
        JSONObject jo = JSON.parseObject(body);
        if (StringUtils.equalsIgnoreCase(jo.get("Status").toString(), "OK")) {
            return jo.get("Data").toString();
        }
        throw new Exception("getEncrypt error : " + body);
    }

    public void learnTask(LWTask task) throws Exception {
        logHandle.sendLog("active task .");
        String encrypt = getEncrypt(task);
        String submiturl = XUE_SUBMIT + encrypt;
        String js = task.studyJson;
        HttpRequest req = genAppjsonPost(submiturl, js)
                .header("token", task.token)
                .header("referer", task.referer)
                .header("Origin", "https://linewelle-learning.yunxuetang.cn")
                .build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        logHandle.sendLog(resp.statusCode() + " " + body);
    }

    public void runStudy() {
        try {
            login_lbsp();
            singleLogin();
            loginToXueTang();
            loadUserInfo();
            loadStudyPlan();
            for (LWPlan plan : planList) {
                loadPlanTask(plan);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logHandle.sendLog(e.getMessage());
        } finally {
            close();
            logHandle.sendLog("all done .");
            logHandle.finish();
        }
    }

    public static void main(String[] args) throws Exception {
        Rob rob = new Rob("lshule", "linewell@2018");
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
            return HttpResponse.BodySubscribers.ofString(UTF_8);
        }
    }
}
