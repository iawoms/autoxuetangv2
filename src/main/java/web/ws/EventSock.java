package web.ws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import core.LogHandle;
import core.Rob;
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


    public static void sendMsg(RemoteEndpoint endpoint, SockMsg msg) {
        try {
            endpoint.sendString(JSON.toJSONString(msg), new WriteCallback() {
                @Override
                public void writeFailed(Throwable x) {
                    System.out.println("writefailed");
                }

                @Override
                public void writeSuccess() {
//                    System.out.println("writesuess");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
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
                sendMsg(getSession().getRemote(),new SockMsg(MsgType.REQ_AUTH,"init connection ."));
                String json = msg.getContent().toString();
                System.out.println(json);
                JSONObject jo = JSON.parseObject(json);
                Rob rob = new Rob(jo.get("usr").toString(), jo.get("pwd").toString(), new LogHandle() {
                    @Override
                    public void sendLog(Object msg) {
                        sendMsg(getRemote(),new SockMsg(MsgType.REQ_AUTH,msg.toString()));
                        System.out.println(msg);
                    }

                    @Override
                    public void finish() {
                        sendMsg(getRemote(),new SockMsg(MsgType.TASKFINISH));
                    }
                });
                rob.runStudy();
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
