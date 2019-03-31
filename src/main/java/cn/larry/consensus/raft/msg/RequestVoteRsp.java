package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.util.RandomString;

public class RequestVoteRsp extends Msg {
    long term;
    boolean voteGranted;

    public RequestVote getRequest() {
        return request;
    }

    public void setRequest(RequestVote request) {
        this.request = request;
    }

    RequestVote request;

    public RequestVoteRsp(long term, boolean voteGranted,RequestVote request) {
        this.msgId = RandomString.genMsgId();
        this.from = request.getTo();
        this.to = request.getFrom();
        this.term = term;
        this.voteGranted = voteGranted;
        this.request = request;
    }

    public RequestVoteRsp(){
        this.msgId = RandomString.genMsgId();

    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }
}
