package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.util.JSONUtil;
import cn.larry.consensus.raft.util.RandomString;

public class Msg {

    protected String from;
    protected String to;
    protected long receiveTime;
    protected  long sendTime;
    protected String msgId;

    public Msg(){
        this.msgId = RandomString.genMsgId();
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
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

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return JSONUtil.toJson(this);
    }
}
