package web.svlt;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import web.ws.model.SockMsg;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static web.WebServer.indexPage;
import static web.ws.model.MsgType.AUTH_FAILED;
import static web.ws.model.MsgType.REDIRECT;

public class Login extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String usr = req.getParameter("usr");
        String pwd = req.getParameter("pwd");
        resp.setContentType("application/json;charset=utf-8");
        SockMsg msg = new SockMsg(AUTH_FAILED,"用户名或密码错误");
        if (StringUtils.isNotEmpty(usr) && StringUtils.isNotEmpty(pwd) && usr.equals("rh") && pwd.equals("123")) {
//            req.getSession().setAttribute("user", new User(usr, pwd));
            msg = new SockMsg(REDIRECT,indexPage);
        }
        resp.getWriter().write(JSON.toJSONString(msg));
    }
}
