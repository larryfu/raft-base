package cn.larry.consensus.raft;

import cn.larry.consensus.raft.msg.*;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Follower {

    private Logger logger = LogManager.getLogger("StateFlow");

    private RaftAlgorithm serverState;

    private long lastLeaderMsg = 0;  //上一次和Leader通信时间

    private int timeoutSeconds; //Leader超时时间

    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();



    public void init() {
        this.lastLeaderMsg = System.currentTimeMillis();
        scheduledExecutorService.schedule(new Runnable() {
            public void run() {
                checkLeaderTimeout();
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    public Follower(final RaftAlgorithm serverState, int timeoutSeconds) {
        this.lastLeaderMsg = System.currentTimeMillis();
        this.serverState = serverState;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 检查Leader超时
     */
    private void checkLeaderTimeout() {
        if (serverState.isFollower()) {
            if (System.currentTimeMillis() - lastLeaderMsg > timeoutSeconds * 1000) {
                logger.error("leader time out convert to candidate");
                serverState.putMessage(new ConvertStatusMsg(RaftAlgorithm.ServerStatus.CANDIDATE), null);
            } else {
                scheduledExecutorService.schedule(new Runnable() {
                    public void run() {
                        checkLeaderTimeout();
                    }
                }, timeoutSeconds, TimeUnit.SECONDS);
            }
        }
    }

    public void onConvertStatus(ConvertStatusMsg convertStatusMsg){
        if(convertStatusMsg.getNewStatus().equalsIgnoreCase(RaftAlgorithm.ServerStatus.CANDIDATE)){
            serverState.convertToCandidate();
        }
    }


    /**
     * follower处理AppendEntry消息
     *
     * @param appendEntry
     * @return
     */
    public AppendEntryRsp onAppendEntry(AppendEntry appendEntry) {
        logger.debug("follower handle  append entry:{}" ,appendEntry);
        if (serverState.getCurrentTerm() > appendEntry.getTerm()) { //leader已过时
            return new AppendEntryRsp(serverState.getCurrentTerm(), false, appendEntry);
        }
        this.lastLeaderMsg = System.currentTimeMillis();
        if (serverState.getCurrentTerm() < appendEntry.getTerm()) { //更新当前term
            serverState.setCurrentTerm(appendEntry.getTerm());
        }

//        //没有日志，是heartbeat
//        if(appendEntry.getEntries()==null || appendEntry.getEntries().size()==0){
//            return new AppendEntryRsp(serverState.getCurrentTerm(), true, appendEntry);
//        }

//        if (appendEntry.getPreLogIndex() == 0) { //
//            logger.debug("pre log index is 0 execute all replace");
//            serverState.applyLog(appendEntry);
//            return new AppendEntryRsp(serverState.getCurrentTerm(), true, appendEntry);
//        }
//        CommProtocolProto.LogEntry logEntry = serverState.getLogs().getLogEntry(appendEntry.getPreLogIndex());
//        if (logEntry == null || logEntry.getTerm() != appendEntry.getPreLogTerm()) { //没有找到preloginex的日志，或者与leader的不匹配
//            logger.debug("can not find pre index log ");
//            return new AppendEntryRsp(serverState.getCurrentTerm(), false, appendEntry);
//        }
       return serverState.applyLog(appendEntry);
        //return new AppendEntryRsp(serverState.getCurrentTerm(), true, appendEntry);
    }

    public ClientRsp onClientRequest(ClientRequest request){
        ClientRsp rsp = new ClientRsp();
        rsp.setMsg("please send req to leader");
        rsp.setRetCode(-10000);
        ServerInfo info = serverState.getLeaderInfo();
        rsp.setLeader(info.getServerName());
        rsp.setLeaderPort(info.getPort());
        return rsp;
    }

    /**
     * Follower处理RequestVote消息
     *
     * @param requestVote
     * @return
     */
    public RequestVoteRsp onRequestVote(RequestVote requestVote) {
        if (serverState.getCurrentTerm() > requestVote.getTerm()) {
            return new RequestVoteRsp(serverState.getCurrentTerm(), false, requestVote);
        }
        if (serverState.getCurrentTerm() < requestVote.getTerm()) {
            serverState.setCurrentTerm(requestVote.getTerm());
        }
        CommProtocolProto.LogEntry lastEntry = serverState.getLogs().getLastEntry();
        long lastIndex = lastEntry != null ? lastEntry.getIndex() : serverState.getLogs().getPreIndex();
        long lastterm = lastEntry != null ? lastEntry.getTerm() : serverState.getLogs().getPreTerm();
        if (requestVote.getLastLogTerm() >= lastterm && requestVote.getLastLogIndex() >= lastIndex) {
            serverState.setVoteFor(requestVote.getCandidateId());
            return new RequestVoteRsp(serverState.getCurrentTerm(), true, requestVote);
        } else {
            return new RequestVoteRsp(serverState.getCurrentTerm(), false, requestVote);
        }
    }


}
