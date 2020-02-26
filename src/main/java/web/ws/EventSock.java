package web.ws;

import com.alibaba.fastjson.JSON;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;
import web.WebServer;
import web.ws.model.MsgType;
import web.ws.model.SockMsg;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventSock extends WebSocketAdapter {
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketAdapter>> wsClients = new ConcurrentHashMap();

    public static void brodcast(SockMsg msg) {
        for (String s : wsClients.keySet()) {
            sendToUser(s, msg);
        }
    }

    public static void sendToUser(String usr, SockMsg msg) {
        ConcurrentHashMap<String, WebSocketAdapter> clis = wsClients.get(usr);
        if (clis != null) {
            for (WebSocketAdapter v : clis.values()) {
                sendMsg(v.getRemote(), msg);
            }
        }
    }

    public static void sendMsg(RemoteEndpoint endpoint, SockMsg msg) {
        try {
            endpoint.sendString(JSON.toJSONString(msg), new WriteCallback() {
                @Override
                public void writeFailed(Throwable x) {

                    System.out.println("writefailed");
                }

                @Override
                public void writeSuccess() {
                    System.out.println("writesuess");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String id;

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        id = UUID.randomUUID().toString().replace("-", "");
        System.out.println("Socket Connected: " + sess);
    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("Received TEXT message: " + message);
        SockMsg msg = JSON.parseObject(message, SockMsg.class);
        switch (msg.getMsgType()) {
            case BACKERROR:
                break;
            case REQ_AUTH: {
//                user = JSON.parseObject(msg.getContent().toString(), User.class);
//                String usrid = user.getUsr();
//                ConcurrentHashMap<String, WebSocketAdapter> userClents = wsClients.get(usrid);
//                if (userClents == null) {
//                    userClents = new ConcurrentHashMap<>();
//                    wsClients.put(usrid, userClents);
//                }
//                userClents.put(id, this);
            }
            break;
            case REQ_COOKIE:
                break;
            case AUTH_SUCCESS:
                break;
            case AUTH_FAILED:
                break;
            case SERVERTIME: {
            }
            break;
            case LOGININFOS:
                break;
            case RABPAGE:
                break;
            case LOGIN:
                break;
            case REDIRECT:
                break;
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
}
