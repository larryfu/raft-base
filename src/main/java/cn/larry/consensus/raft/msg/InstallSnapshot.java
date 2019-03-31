package cn.larry.consensus.raft.msg;

public class InstallSnapshot extends Msg{

    long term;
    int leaderId;
    long lastIncludeIndex;
    long lastIncludeTerm;
    long offset;
    byte[] data;
    boolean done;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public long getLastIncludeIndex() {
        return lastIncludeIndex;
    }

    public void setLastIncludeIndex(long lastIncludeIndex) {
        this.lastIncludeIndex = lastIncludeIndex;
    }

    public long getLastIncludeTerm() {
        return lastIncludeTerm;
    }

    public void setLastIncludeTerm(long lastIncludeTerm) {
        this.lastIncludeTerm = lastIncludeTerm;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
