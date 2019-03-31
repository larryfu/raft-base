package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.proto.CommProtocolProto.LogEntry;
import cn.larry.consensus.raft.util.RandomString;
import com.googlecode.protobuf.format.JsonFormat;

import java.util.List;

public class AppendEntry extends Msg {
    long term;
    int leaderId;
    long preLogIndex;
    long preLogTerm;
    List<LogEntry> entries;
    long leaderCommit;


    public AppendEntry(){
        this.msgId = RandomString.genMsgId();
    }

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

    public long getPreLogIndex() {
        return preLogIndex;
    }

    public void setPreLogIndex(long preLogIndex) {
        this.preLogIndex = preLogIndex;
    }

    public long getPreLogTerm() {
        return preLogTerm;
    }

    public void setPreLogTerm(long preLogTerm) {
        this.preLogTerm = preLogTerm;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("term:[").append(term).append("]\n");
        sb.append("leaderId:[").append(leaderId).append("]\n");
        sb.append("preLogIndex:[").append(preLogIndex).append("]\n");
        sb.append("preLogTerm:[").append(preLogTerm).append("]\n");
        sb.append("entries:[");
        for(LogEntry entry:entries){
            sb.append(JsonFormat.printToString(entry));
        }
        sb.append("]\n");
        sb.append("leaderCommit:[").append(leaderCommit).append("]\n");
        return sb.toString();
    }
}
