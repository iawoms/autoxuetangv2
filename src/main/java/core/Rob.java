package core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import model.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tool.RobUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static String XUE_EXAMPREV = DOMAIN + "exam/exampreview.htm?";

    public static String EXAM_CACHEANSWERS = DOMAIN + "exam/Services/StyService.svc/GetUserExamCacheAnswersOfList";
    public static String EXAM_SUBMIT = DOMAIN + "exam/Services/StyService.svc/SubmitOrSaveUserExam";
    public static String examId_fix = "${ExamID}";
    public static String arrangeId_fix = "${arrangeId}";
    public static String EXAM_ANSWER = "https://api-qidatestin.yunxuetang.cn/v1/ote/web/userexam/" + examId_fix + "/answer?" + "arrangeId=" + arrangeId_fix;
    public static String EXAM_RESULT = DOMAIN + "exam/test/examquestionpreview.htm?userExamID=";


    HttpClient client;
    CookieManager cookieManager = new CookieManager();
    SSLContext sslContext = null;

    private String usr;
    private String pwd;
    private String sessionid;
    private String xue_session_loginurl;
    private String shareToken;
    LWUser lwUser;
    List<LWPlan> planList = new ArrayList<>();
    LogHandle logHandle;

    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

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

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }


        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(120))
                .build();

    }

    public void close() {
    }


    public void resetClientLogin() throws Exception {
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(120))
                .build();
        login_lbsp();
        singleLogin();
        loginToXueTang();
        loadUserInfo();
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
                    String taskurl = clps[0];
                    String taskType = clps[1];
                    if (taskType.equalsIgnoreCase("DocumentKnowledge")) {
                        logHandle.sendLog("Doc knowledge not sported : [" + title + "]");
                    } else {
                        logHandle.sendLog(taskurl);
                        List<LWTask> tasks = explanTask(taskurl);
                        if (tasks.size() == 0) {
                            LWTask task = new LWTask();
                            task.title = title;
                            task.path = "kng" + taskurl;
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
                task.title = ln.attr("title");
                if (StringUtils.isEmpty(task.title)) {
                    continue;
                }
                String path = ln.attr("href");
                String[] parms = path.split(",");
                task.taskType = parms[1].replace("'", "");
//            javascript:void(StudyRowClick('/package/ebook/8fbac16704f5460881af2e2f79f0648b_2fedb53090c24268bad8bba85c031a1e.html?MasterID=6c694eff-f54e-439d-9364-950782466487&MasterType=Plan','CourseKnowledge','','False', 'True','True',''));
//                javascript:void(StudyRowClick('/exam/exampreview.htm?examarrangeid=60d334d8-9a62-4203-9479-e0dcf86ef20e&packageid=06cd4c95-f9d6-42d9-952e-516189f10537&MasterID=2a3f217b-4fa1-43d0-924a-88a482f9de3c&MasterType=PlanStudy','OteExam','Evaluating','True', 'True','True'));
                task.path = "kng/course" + RobUtils.cutStr(path, "StudyRowClick('", "','");
                task.masterID = RobUtils.cutStr(path, "MasterID=", "&");
                openTaskPage(task);
                list.add(task);
            }
        }
        return list;
    }

    public void openTaskPage(LWTask task) throws Exception {
        if (task.taskType.equalsIgnoreCase("OteExam")) {
            LWExam exam = RobUtils.parseByUrl(task.path, LWExam.class);
            doTask(exam);
        } else {
            logHandle.sendLog("openning task page ...");
            String url = DOMAIN + task.path;
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
            if (liFavorite == null) {
                logHandle.sendLog(task.title + " load faild ! skip ...");
                return;
            }
            Element span = liFavorite.getElementsByTag("span").get(0);
            String idstr = RobUtils.cutStr(span.attr("onclick"), "CheckAddToFavorite(", ");");
            String[] ids = idstr.split(",");
            task.knowledgeID = ids[2].replace("'", "");
            task.packageId = ids[3].replace("'", "");
            task.masterID = ids[4].replace("'", "");
            task.referer = url + "?MasterID=" + task.masterID + "&MasterType=Plan";
            logHandle.sendLog("loading token ...");

            Element divCourseTree = doc.getElementById("divCourseTree");
            Elements ctreeA = divCourseTree.getElementsByTag("a");
            if (ctreeA != null) {
                DivCourseTree dct = RobUtils.parseByUrl(ctreeA.get(1).attr("href"), DivCourseTree.class);
                task.studyChapterIds = dct.id;
            }
            Element tokeninput = doc.getElementById("hidIndexPage");
            if (tokeninput == null) {
                String tokenstr = RobUtils.findLine(body, "token:");
                String token = RobUtils.cutStr(tokenstr, "token: '", "'");
                System.out.println("token : " + token);
                task.token = token;
            } else {
                String tokenstr = tokeninput.val();
                String token = RobUtils.cutStr(tokenstr, "token%3d", "%26productid");
                task.token = token;
            }

            logHandle.sendLog("gen enc json ...");
            task.genStudyJson();
            doTask(task);
        }
    }

    private void doTask(LWExam task) throws Exception {
        for (int ti = 0; ti < 5; ti++) {
            logHandle.sendLog("loading exam page ... try + " + ti);
            String url = XUE_EXAMPREV + RobUtils.genPostBody(task);
            HttpRequest req = genGet(url).build();
            HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
            String body = resp.body();
            Document doc = Jsoup.parse(body);
            Element hidurl = doc.getElementById("hidExamUrl");
            url = DOMAIN + hidurl.val();
            task.submitReferer = url;
            LWExam nexam = RobUtils.parseByUrl(hidurl.val(), LWExam.class);
            task.ExamID = nexam.ExamID;
            task.UserExamMapID = nexam.UserExamMapID;
            req = genGet(url).build();
            resp = client.send(req, new SimpleRespHlr());
            body = resp.body();
            task.token = RobUtils.findToken(body);
            doc = Jsoup.parse(body);
            Element hidUserExamID = doc.getElementById("hidUserExamID");
            task.UserExamID = hidUserExamID.val();
            Element hidUniqueId = doc.getElementById("hidUniqueId");
            task.uniqueId = hidUniqueId.val();

            Elements quesList = doc.getElementsByClass("ques-list");
            List<LiQuestion> questions = new ArrayList<>();
            for (Element ques : quesList) {
                Elements eliqs = ques.getElementsByAttributeValue("name", "li_Question");
                for (Element eliq : eliqs) {
                    LiQuestion liq = new LiQuestion(eliq);
                    Elements liTexts = eliq.getElementsByClass("text-grey");
                    Elements inputs = eliq.getElementsByTag("input");
                    for (int i = 0; i < inputs.size(); i++) {
                        Element input = inputs.get(i);
                        LiAnswer lia = new LiAnswer(input);
                        try {
                            lia.text = liTexts.get(i).text().trim();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        liq.addAnswer(lia);
                    }
                    questions.add(liq);
                }
            }
            task.questions = questions;
            ExamResultPreview prev = answerQuestion(task);
            if (prev.isPassed) {
                break;
            }
        }
    }

    private ExamResultPreview answerQuestion(LWExam examAndQues) throws Exception {
        String url = EXAM_ANSWER.replace(examId_fix, examAndQues.UserExamID).replace(arrangeId_fix, examAndQues.ExamArrangeID);
        for (LiQuestion liq : examAndQues.questions) {
            try {
                Answer ans = new Answer(liq);
                ans.answer = examAndQues.genAnswer(liq);
                logHandle.sendLog(liq.text);
                ExamAnswerReq ereq = new ExamAnswerReq(ans);
                String payLoad = JSON.toJSONString(ereq, SerializerFeature.WriteNullStringAsEmpty);
                HttpRequest req = RobUtils.genAppjsonPost(url, payLoad)
                        .header("Token", examAndQues.token)
                        .build();
                for (int i = 0; i < 5; i++) {
                    try {
                        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
                        String body = resp.body();
                        System.out.println(body);
                        break;
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        System.out.println("retry ...");
                        resetClientLogin();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SubmitExam submit = new SubmitExam(examAndQues);
        String payLoad = JSON.toJSONString(submit);
        HttpRequest req = RobUtils.genTextjsonPost(EXAM_SUBMIT, payLoad)
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Origin", "https://linewelle-learning.yunxuetang.cn")
                .header("sec-ch-ua", "\"Chromium\";v=\"92\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"92\"")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7,ja;q=0.6,es;q=0.5,fr;q=0.4,la;q=0.3,vi;q=0.2,th;q=0.1,de;q=0.1,nl;q=0.1,it;q=0.1,gl;q=0.1,cy;q=0.1,mt;q=0.1,da;q=0.1,pt;q=0.1,fa;q=0.1,ru;q=0.1")
                .header("sec-ch-ua-mobile", "?0")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Referer", examAndQues.submitReferer)
                .build();
        var resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        System.out.println(body);
        ExamResultPreview prev = loadExamResult(examAndQues.UserExamID);
        logHandle.sendLog(prev);
        examAndQues.resultPreview = prev;
        return prev;
    }

    public ExamResultPreview loadExamResult(String userExamId) throws Exception {
        logHandle.sendLog("loading exam result ...");
        String url = EXAM_RESULT + userExamId;
        HttpRequest req = genGet(url).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String body = resp.body();
        Document doc = Jsoup.parse(body);
        ExamResultPreview prev = new ExamResultPreview(doc);
        return prev;
    }

    public String getEncrypt(LWTask task) throws Exception {
        logHandle.sendLog("loading enc ...");
        String url = XUE_GETEYPT;
        System.out.println(task.encJson);
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

    public void doTask(LWTask task) throws Exception {
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
//        Rob rob = new Rob("hcanrong", "Fuck4linewell");
        Rob rob = new Rob("林雪红", "Xuehong@521");

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
