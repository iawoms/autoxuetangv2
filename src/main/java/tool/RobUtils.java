package tool;

import model.LWUser;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class RobUtils {
    public static String AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36";

    public static String genPostBody(Object body) throws IllegalAccessException {
        Field[] fields = body.getClass().getDeclaredFields();
        String bodyStr = "";
        for (Field f : fields) {
            Object val = f.get(body);
            if (val != null) {
                if (StringUtils.isNotEmpty(bodyStr)) {
                    bodyStr += "&";
                }
                String pair = f.getName() + "=" + val.toString();
                bodyStr += pair;
            }

        }
        return bodyStr;
    }

    public static <T> T parseByUrl(String urlParms, Class<T> clazz) throws Exception {
        int idx = urlParms.indexOf("?");
        if (idx > -1) {
            urlParms = urlParms.substring(idx + 1);
        }
        String[] parms = urlParms.split("&");
        Map<String, String> kvm = new CaseInsensitiveMap<>();
        for (String parm : parms) {
            String[] kv = parm.split("=");
            if (kv.length == 2) {
                kvm.put(kv[0], kv[1]);
            }
        }

        T t = clazz.getDeclaredConstructor().newInstance();
        Field[] fs = clazz.getFields();
        for (Field f : fs) {
            String fname = f.getName();
            String val = kvm.get(fname);
            if (val != null) {
                f.set(t, val);
            }
        }

        return t;
    }


    public static HttpRequest.Builder genGet(String uri) {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .header("User-Agent", AGENT);
//                .header(XRHeader, xrHead)
        return req;
    }

    public static HttpRequest.Builder genPost(String uri, String body) {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("User-Agent", AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return req;
    }

    public static HttpRequest.Builder genAppjsonPost(String uri, String body) {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("User-Agent", AGENT)
                .header("Content-Type", "application/json;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        return req;
    }

    public static HttpRequest.Builder genTextjsonPost(String uri, String body) {
        HttpRequest.BodyPublisher bh = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("User-Agent", AGENT)
                .header("Content-Type", "text/json")
                .POST(bh);
        return req;
    }

    public static String findLine(String body, String key) {
        ByteArrayInputStream is = new ByteArrayInputStream(body.getBytes());
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (StringUtils.containsIgnoreCase(line, key)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String cutStr(String str, String pre, String efe) {
        int idx = str.indexOf(pre);
        if (idx >= 0) {
            str = str.substring(idx + pre.length());
            return str.substring(0, str.indexOf(efe));
        }
        return null;
    }

    public static String findToken(String body) {
        String token_fix = "\"Token\": '";
        int idx = body.indexOf(token_fix) + token_fix.length();
        body = body.substring(idx);
        idx = body.indexOf("',");
        return body.substring(0, idx);

    }
}
