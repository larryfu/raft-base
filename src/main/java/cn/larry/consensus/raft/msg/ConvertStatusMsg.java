package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.util.RandomString;

public class ConvertStatusMsg extends Msg {

    private String newStatus;

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public ConvertStatusMsg(String newStatus){
        this.msgId = RandomString.genMsgId();
        this.newStatus = newStatus;
        this.from = "localhost";
        this.to = "localhost";
    }
}
