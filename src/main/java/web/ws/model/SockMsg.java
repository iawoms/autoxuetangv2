package web.ws.model;

import java.util.Date;

/**
 * Created by iawom on 17-8-30.
 */
public class SockMsg {
    /**
     * 消息类型
     */
    private MsgType msgType;
    /**
     * 消息id
     */
    private long id;
    /**
     * 源ip
     */
    private String from;
    /**
     * 目标ip
     */
    private String to;
    /**
     * 目标业务对象id
     */
    private String target;
    private Date sendTime;
    private Date receiveTime;
    private Object content;

    public MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public SockMsg()
    {

    }
    public SockMsg(MsgType msgType){
        this.msgType = msgType;
    }
    public SockMsg(MsgType msgType, Object content){
        this.msgType = msgType;
        this.content = content;
    }
}
