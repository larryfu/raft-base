package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.util.RandomString;

public class AppendEntryRsp extends Msg {
    long term;
    boolean success;
    AppendEntry request;

    public AppendEntryRsp(long term, boolean success, AppendEntry request) {
        this.msgId = RandomString.genMsgId();
        this.from = request.getTo();
        this.to = request.getFrom();
        this.term = term;
        this.success = success;
        this.request = request;
    }
    public AppendEntryRsp(){
        this.msgId = RandomString.genMsgId();
    }

    public AppendEntry getRequest() {
        return request;
    }

    public void setRequest(AppendEntry request) {
        this.request = request;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
