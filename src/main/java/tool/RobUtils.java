package tool;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpRequest;

public class RobUtils {
    public static String AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36";

    public static String genPostBody(Object body) throws IllegalAccessException {
        Field[] fields = body.getClass().getDeclaredFields();
        String bodyStr = "";
        for (Field f : fields) {
            if (StringUtils.isNotEmpty(bodyStr)) {
                bodyStr += "&";
            }
            String pair = f.getName() + "=" + f.get(body).toString();
            bodyStr += pair;
        }
        return bodyStr;
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
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") ;
        return req;
    }
}
