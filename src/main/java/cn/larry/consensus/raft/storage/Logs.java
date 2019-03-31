package cn.larry.consensus.raft.storage;


import cn.larry.consensus.raft.proto.CommProtocolProto.LogEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Logs {

    private long preIndex; //前一个日志的index,初始为0，日志合并后记录合并的最后一条日志的index
    private long preTerm; //前一个日志的term,初始为0，日志合并后记录合并的最后一条日志的term
    private List<LogEntry> logEntries = new ArrayList<>();


    public long getPreIndex() {
        return preIndex;
    }

    public void setPreIndex(long preIndex) {
        this.preIndex = preIndex;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public Pair<LogEntry, LogEntry> getPreTermLastLog(long term) {
        LogEntry preLast = null;
        LogEntry thisFirst = null;
        for (int i = 0; i < logEntries.size(); i++) {
            if (logEntries.get(i).getTerm() == term) {
                thisFirst = logEntries.get(i);
                if (i > 0) {
                    preLast = logEntries.get(i - 1);
                }
            }
        }
        return Pair.of(preLast, thisFirst);
    }

    //合并日志，建立快照
    public void compaction(long commitindex){

    }

    public long getLastLogindex() {
        LogEntry entry = getLastEntry();
        if (entry == null) {
            return preIndex;
        }
        return entry.getIndex();
    }

    public long getLastLogTerm() {
        LogEntry entry = getLastEntry();
        if (entry == null) {
            return preTerm;
        }
        return entry.getTerm();
    }

    public LogEntry getLogEntry(long index) {
        if (logEntries.size() == 0 || index > getLastLogindex() || index < preIndex) {
            return null;
        } else {
            for (int i = logEntries.size() - 1; i >= 0; i--) { //向前遍历找到匹配的index，同一Term内index是连续的
                if (logEntries.get(i).getIndex() == index)
                    return logEntries.get(i);
            }
        }
        return null;
    }

    /**
     *  获取startIndex-endIndex 范围内的日志，startIndex和endIndex都包含
     * @param startIndex
     * @param endIndex
     * @return
     */
    public List<LogEntry> getLogByRange(long startIndex, long endIndex) {

        if (logEntries.size() == 0 || startIndex > getLastLogindex())
            return new ArrayList<>();
        int start = -1;
        for (int i = logEntries.size() - 1; i >= 0; i--) { //向前遍历找到匹配的index，同一Term内index是连续的
            if (logEntries.get(i).getIndex() == startIndex) {
                start = i;
                break;
            }
        }
        List<LogEntry> entries = new ArrayList<>();
        if (start >= 0) {
            for (int i = start; i < logEntries.size(); i++) {
                entries.add(logEntries.get(i));
                if (logEntries.get(i).getIndex() == endIndex)
                    break;
            }
        }
        return entries;
    }

    public LogEntry getPreLogEntry(long index) {
        if (index > getLastLogindex() || index < preIndex) {
            return null;
        } else {
            for (int i = logEntries.size() - 1; i > 0; i--) { //向前遍历找到匹配的index，同一Term内index是连续的
                if (logEntries.get(i).getIndex() == index) {
                    return logEntries.get(i - 1);
                }
            }
        }
        return null;
    }

    public LogEntry getLastEntry() {
        if (logEntries.size() == 0) return null;
        return logEntries.get(logEntries.size() - 1);
    }

    public void addEntry(LogEntry entry) {
        logEntries.add(entry);
    }

    public long getPreTerm() {
        return preTerm;
    }

    public void setPreTerm(long preTerm) {
        this.preTerm = preTerm;
    }
}
