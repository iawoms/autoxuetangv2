package web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import web.svlt.Login;
import web.ws.WsServlet;

public class WebServer {

    public static String indexPage = "/index.html";

    public static void main(String[] args) throws Exception {

        Server server = new Server(808);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{indexPage});
        resource_handler.setResourceBase("./web");


        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        SessionHandler sesHdr = new SessionHandler();
        sesHdr.setMaxInactiveInterval(60 * 20);
        handler.setSessionHandler(sesHdr);
        handler.addServlet(Login.class, "/login");
        handler.addServlet(WsServlet.class, "/ws");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, handler, new DefaultHandler()});
        server.setHandler(handlers);

        server.start();
        server.join();


    }
}
