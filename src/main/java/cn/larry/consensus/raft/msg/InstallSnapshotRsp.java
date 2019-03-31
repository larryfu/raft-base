package cn.larry.consensus.raft.msg;

public class InstallSnapshotRsp extends Msg {
    long term;
    InstallSnapshot request;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public InstallSnapshot getRequest() {
        return request;
    }

    public void setRequest(InstallSnapshot request) {
        this.request = request;
    }
}
