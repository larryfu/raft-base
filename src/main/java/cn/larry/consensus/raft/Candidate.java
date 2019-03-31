package cn.larry.consensus.raft;

import cn.larry.consensus.raft.msg.*;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Candidate {

    private  Logger logger = LogManager.getLogger("StateFlow");

    private RaftAlgorithm serverState;



    private int candidateTimeoutMs = 3000;

    public final List<RequestVoteRsp> receivedRsp = new ArrayList<RequestVoteRsp>();

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public Candidate(RaftAlgorithm serverState) {
        this.serverState = serverState;
    }

    /**
     * 进入candidate状态或者超时开始新一轮选举
     */
    public void init() {
        this.receivedRsp.clear();
        serverState.incrCurrentTerm(); //递增Term
        logger.debug("candidate init ");
        for (ServerInfo serverInfo : serverState.getClusterServers()) {
            logger.debug("send candidate msg to :{}",serverInfo.getServerName());
            RequestVote requestVote = new RequestVote();
            requestVote.setCandidateId(serverState.getThisServer().getServerId());
            requestVote.setTerm(serverState.getCurrentTerm());
            CommProtocolProto.LogEntry logEntry = serverState.getLogs().getLastEntry();
            long lastLogterm  = logEntry == null?0:logEntry.getTerm();
            long lastLogIndex = logEntry ==null?0:logEntry.getIndex();
            requestVote.setLastLogTerm(lastLogterm);
            requestVote.setLastLogIndex(lastLogIndex);
            requestVote.setFrom(serverState.getThisServer().getServerName());
            requestVote.setTo(serverInfo.getServerName());
            serverState.sendMessage(requestVote);
        }
        serverState.setVoteFor(serverState.getThisServer().getServerId());
        Random random = new Random();
        //candidate选举超时
        executorService.schedule(new Runnable() {
            public void run() {
                checkTimeout();
            }
        }, candidateTimeoutMs + random.nextInt(500), TimeUnit.MILLISECONDS);
    }

    public void checkTimeout() {
        logger.debug("check candidate time out ");
        if (serverState.isCandidate()) {
            logger.debug(" candidate time out restart ");
            RestartCandidateMsg message = new RestartCandidateMsg();
            message.setFrom(serverState.getThisServer().getHost());
            message.setTo(serverState.getThisServer().getHost());
            serverState.putMessage(message,null);
        }else {
            logger.error("this server is not candidate any more");
        }
    }

    public void onRestartCandidate(RestartCandidateMsg message) {
        logger.debug("restart candidate");
        init();
    }

    /**
     * @param appendEntry
     * @return
     */
    public AppendEntryRsp onAppendEntry(AppendEntry appendEntry) {
        logger.debug("handle appendentry :{}",appendEntry);
        if (appendEntry.getTerm() >= serverState.getCurrentTerm()) {
            serverState.convertToFollower(appendEntry.getTerm(), appendEntry.getLeaderId());
            serverState.putMessage(appendEntry,null);
            return null;
        } else {
            return new AppendEntryRsp(serverState.currentTerm, false, appendEntry);
        }
    }

    /**
     * 处理RequestVote请求
     *
     * @param requestVote
     */
    public RequestVoteRsp onRequestVote(RequestVote requestVote) {
        logger.debug("********************receive RequestVote :{}",requestVote);
        if (requestVote.getTerm() > serverState.getCurrentTerm()) { //自己term小于requestVote，更新term并投票给请求者
            serverState.setCurrentTerm(requestVote.getTerm());
            this.receivedRsp.clear();
            serverState.setVoteFor(requestVote.getCandidateId());
            return new RequestVoteRsp(serverState.currentTerm, true,requestVote);
        } else if (requestVote.getTerm() < serverState.getCurrentTerm()) {
            return new RequestVoteRsp(serverState.currentTerm, false,requestVote);
        } else if (serverState.getVoteFor() > 0 && serverState.getVoteFor() != requestVote.getCandidateId()) {
            return new RequestVoteRsp(serverState.currentTerm, false,requestVote);
        } else {
            serverState.setVoteFor(requestVote.getCandidateId());
            return new RequestVoteRsp(serverState.currentTerm, true,requestVote);
        }
    }


    /**
     * 处理RequestVote回复
     *
     * @param requestVoteRsp
     */
    public void onRequestVoteRsp(RequestVoteRsp requestVoteRsp) {
        logger.debug("handle RequestVoteRsp :{}",requestVoteRsp);
        this.receivedRsp.add(requestVoteRsp);
        if (requestVoteRsp.isVoteGranted()) {
            logger.debug("***********************received vote from :{}",requestVoteRsp.getFrom());
            int supporter = 0;
            for (RequestVoteRsp rsp : receivedRsp) {
                if (rsp.isVoteGranted()) supporter++;
            }
            if (supporter > serverState.getClusterServers().size() / 2) {
                serverState.convertToLeader();
            }
        }else if(requestVoteRsp.getTerm() > serverState.getCurrentTerm()){

        }

    }

}
