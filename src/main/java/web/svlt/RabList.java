package web.svlt;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class RabList extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
//        String usr = ((User)req.getSession().getAttribute("user")).getUsr();
//        resp.setContentType("application/json;charset=utf-8");
//        String json = JSON.toJSONString(WebServer.getLiveByUser(usr).robs.get(0).getRabsPage().getItems());
//        resp.getWriter().write(json);
    }
}