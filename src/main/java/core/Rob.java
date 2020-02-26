package core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.LWPage;
import model.LWPlan;
import model.LWUser;
import model.LbspLogin;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tool.RobUtils;

import java.io.*;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tool.RobUtils.genGet;
import static tool.RobUtils.genPost;

public class Rob {
    public static String LW_LOGIN_URL = "http://mss.linewell.com/lbsp/login.action";
    public static String LW_SINGLE_LOGIN_URL = "http://mss.linewell.com/lbsp/BaseAction.action?type=yunXueTang&act=single_sign_on";

    public static String DOMAIN = "https://linewelle-learning.yunxuetang.cn/";
    public static String XUE_STY = DOMAIN + "sty/index.htm";
    public static String XUE_MYSTUDYTASK = DOMAIN + "sty/mystudytask.htm";
    public static String XUE_MYPLANS = DOMAIN + "Services/MyIndexService.svc/GetTaskCenterPersonalStudyPlan";

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
        ByteArrayInputStream is=new ByteArrayInputStream(body.getBytes());
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line =  br.readLine()) != null) {
            if (line.contains("var arr = '{\"orgID\"")) {
                lwUser = new LWUser(line);
            }
        }
        System.out.println(lwUser);
    }

    public void loadStudyPlan()throws Exception{
        HttpRequest req = genGet(XUE_MYSTUDYTASK).build();
        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
        String data = resp.body();
        org.jsoup.nodes.Document doc = Jsoup.parse(data);
        Elements els = doc.getElementsByClass("el-plan-list");
        for (Element el : els) {
            Elements lis = el.getElementsByTag("li");
            for (Element li : lis) {
                String click = li.attr("onclick");
                String href = click.replace("learningKnowledge(\"","").replace("\")","");
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

    public void loadPlan(String planUrl) throws Exception {
        if(StringUtils.isEmpty(planUrl)){
            planUrl = XUE_MYPLANS;
        }
//        HttpRequest req = RobUtils.genPost(LW_SINGLE_LOGIN_URL, payLoadLogin)
//                .setHeader("Content-type", "application/json;charset=UTF-8")
//                .build();
//        HttpResponse<String> resp = client.send(req, new SimpleRespHlr());
//        httpclient_lbsp = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
//        LWPage page = new LWPage();
//        String payLoad = JSON.toJSONString(page);
//        StringEntity se = new StringEntity(payLoad);
//        HttpUriRequest act = RequestBuilder.post()
//                .setUri(new URI(planUrl))
//                .setHeader("Content-type", "text/json")
//                .setEntity(se)
//                .build();
//        CloseableHttpResponse response = httpclient_lbsp.execute(act);
//        if(response.getStatusLine().getStatusCode() != 200){
//            sendStep("switching url...");
//            loadMyPlan(XUE_MYPLANS_ALLNEW);
//            return;
//        }
//        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//        String line, data = "";
//        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//            data += line.replace("\\", "");
//        }
//        int code = response.getStatusLine().getStatusCode();
//        org.jsoup.nodes.Document doc = Jsoup.parse(data);
//        Elements lis = doc.getElementsByTag("li");
//        for (Element li : lis) {
//            Elements alinks = li.getElementsByTag("a");
//            for (Element alink : alinks) {
//                String href = alink.attr("href");
//                if (StringUtils.isNotEmpty(href)) {
//                    LWPlan plan = new LWPlan();
//                    plan.setUrl(href);
//                    plan.setName(alink.text());
//                    planList.add(plan);
//                    break;
//                }
//            }
//            sendStep("found " + planList.size() + " plans");
//        }
    }

    public static void main(String[] args) throws Exception {
        Rob rob = new Rob("hcanrong", "123457");
        rob.login_lbsp();
        rob.singleLogin();
        rob.loginToXueTang();
        rob.loadUserInfo();
        rob.loadStudyPlan();
        System.out.println(rob.planList.size());

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
