package cn.larry.consensus.raft.msg;

public class ClientRsp extends Msg {

    int retCode;
    String msg;
    Object value;
    String leader;
    int leaderPort;

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public int getLeaderPort() {
        return leaderPort;
    }

    public void setLeaderPort(int leaderPort) {
        this.leaderPort = leaderPort;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
