package web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import web.svlt.Login;
import web.svlt.RabList;
import web.svlt.SockGroup;
import web.ws.WsServlet;

public class WebServer {

    public static String indexPage = "/pages/index.html";

    public static void main(String[] args) throws Exception {
        String usr = "rh";
//        RabLive live = new RabLive(usr);
//        live.addRob(new RabitRob("15394581398", "zrh713600",new MsgHdlImpl(usr)));
//        live.addRob(new RabitRob("19959880119", "zrh713600",new MsgHdlImpl(usr)));
//        live.addRob(new RabitRob("18659568069", "123456",new MsgHdlImpl(usr)));
//        live.addRob(new RabitRob("13459811158", "123456ll",new MsgHdlImpl(usr)));
//        live.addRob(new RabitRob("15980968861", "fqx19950229",new MsgHdlImpl(usr)));
//        live.addRob(new RabitRob("13105063409", "watc319",new MsgHdlImpl(usr)));
//
//        live.loginAll();
//        live.alive();
//        userLives.putIfAbsent(live.user,live);

        Server server = new Server(8080);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{indexPage});
        resource_handler.setResourceBase("./web");


        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        SessionHandler sesHdr = new SessionHandler();
        sesHdr.setMaxInactiveInterval(60 * 20);
        handler.setSessionHandler(sesHdr);
        handler.addServlet(RabList.class, "/rablist");
        handler.addServlet(Login.class, "/login");
        handler.addServlet(SockGroup.class, "/sockgroup");
        handler.addServlet(WsServlet.class, "/ws");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, handler, new DefaultHandler()});
        server.setHandler(handlers);

        server.start();
        server.join();


    }
}
