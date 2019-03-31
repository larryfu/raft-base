package cn.larry.consensus.raft;

import cn.larry.consensus.raft.storage.Logs;
import cn.larry.consensus.raft.msg.*;
import cn.larry.consensus.raft.net.MessageSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RaftAlgorithm implements Runnable {


    private Logger logger = LogManager.getLogger("StateFlow");

    private MessageSender messageSender;

    private ServerInfo thisServer;

    protected long currentTerm;
    protected int voteFor;

    protected long commitIndex;
    protected long lastApplied;

    protected int currentLeader;

    private Logs logs;

    private String currentState;

    private Leader leader;
    private Follower follower;
    private Candidate candidate;

    private List<ServerInfo> clusterServers;

    private LinkedBlockingQueue<Msg> msgQueue = new LinkedBlockingQueue<>();

    /**
     * raft算法处理完消息之后回调
     */
    private ConcurrentHashMap<String, Consumer<Object>> msgCallBack = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Consumer<Object>> getClientMsgCallBack() {
        return clientMsgCallBack;
    }

    private ConcurrentHashMap<String, Consumer<Object>> clientMsgCallBack = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Consumer<Object>> getMsgCallBack() {
        return msgCallBack;
    }

    public void setMsgCallBack(ConcurrentHashMap<String, Consumer<Object>> msgCallBack) {
        this.msgCallBack = msgCallBack;
    }

    public ServerInfo getLeaderInfo() {
        for (ServerInfo info : getClusterServers())
            if (info.getServerId() == currentLeader)
                return info;
        return null;
    }

    public void putMessage(Msg msg, Consumer callback) {
        if (callback != null)
            msgCallBack.put(msg.getMsgId(), callback);
        msgQueue.offer(msg);
    }

    public void putClientMessage(Msg msg, Consumer callback) {
        logger.error("receive client req is leader:{}", isLeader());
        if (!isLeader()) {
            ClientRsp rsp = new ClientRsp();
            rsp.setRetCode(-1000);
            rsp.setMsg("please send req to leader");
            rsp.setLeader(getLeaderInfo().getServerName());
            rsp.setLeaderPort(getLeaderInfo().getPort());
            if (callback != null)
                callback.accept(rsp);
        } else {
            if (callback != null)
                clientMsgCallBack.put(msg.getMsgId(), callback);
            msgQueue.offer(msg);
        }
    }


    public void run() {
        while (!Thread.interrupted()) {
            try {
                Msg msg = msgQueue.poll(10, TimeUnit.SECONDS);
                logger.debug("poll msg result:{}", msg);
                if (msg == null)
                    continue;
                Msg result = null;
                if (isLeader()) {
                    result = leaderHandleMessage(msg);
                } else if (isFollower()) {
                    result = followerHandleMessage(msg);
                } else if (isCandidate()) {
                    result = candidateHandleMessage(msg);
                } else {

                }

                Consumer<Object> consumer = msgCallBack.get(msg.getMsgId());
                if (consumer != null)
                    consumer.accept(result);
            } catch (Exception e) {
                logger.error("exception handle msg ", e);
            }
        }
        logger.error("thread is interrupted exit");
    }

    /**
     * 向其他server发送消息，消息目的地址写在msg里
     *
     * @param msg
     */
    public void sendMessage(Msg msg) {
        logger.debug("send msg :{}", msg);
        messageSender.sendMessage(msg, new Consumer<Msg>() {
            @Override
            public void accept(Msg msg) {
                putMessage(msg, null);
            }
        });
        logger.debug("send msg finish");
    }

    private Msg candidateHandleMessage(Msg msg) {
        logger.debug("candidate handle msg:{}", msg);
        Msg result = null;
        if (msg instanceof AppendEntry) {
            result = candidate.onAppendEntry((AppendEntry) msg);
        } else if (msg instanceof RequestVote) {
            result = candidate.onRequestVote((RequestVote) msg);
        } else if (msg instanceof RequestVoteRsp) {
            candidate.onRequestVoteRsp((RequestVoteRsp) msg);
        } else if (msg instanceof RestartCandidateMsg) {
            candidate.onRestartCandidate((RestartCandidateMsg) msg);
        } else {
            logger.error("candidate can not handel msg:{}", msg);
        }
        //TODO complete logic
        return result;
    }


    private Msg followerHandleMessage(Msg msg) {
        logger.debug("follower handle msg:{}", msg);
        Msg rsp = null;
        if (msg instanceof AppendEntry) {
            rsp = getFollower().onAppendEntry((AppendEntry) msg);
        } else if (msg instanceof RequestVote) {
            rsp = getFollower().onRequestVote((RequestVote) msg);
        } else if (msg instanceof ConvertStatusMsg) {
            getFollower().onConvertStatus((ConvertStatusMsg) msg);
        } else if (msg instanceof ClientRequest) {
            getFollower().onClientRequest((ClientRequest) msg);
        } else {
            logger.error("follower can not handle message ignore , " + msg.getClass().getCanonicalName());
        }
        //TODO complete logic
        return rsp;
    }

    private Msg leaderHandleMessage(Msg msg) {
        logger.debug("leader handle msg {}", msg.getMsgId());
        if (msg instanceof AppendEntry) {
            return leader.onAppendEntry((AppendEntry) msg);
        } else if (msg instanceof RequestVote) {
            return leader.onRequestVote((RequestVote) msg);
        } else if (msg instanceof AppendEntryRsp) {
            leader.onAppendEntryRsp((AppendEntryRsp) msg);
            return null;
        } else if (msg instanceof ClientRequest) {
            leader.onClientRequest((ClientRequest) msg);
        } else {
            logger.error("leader can not handle msg:{}", msg);
        }
        //TODO complete logic
        return null;
    }

    public RaftAlgorithm(List<ServerInfo> clusterServers, ServerInfo self, MessageSender messageSender) {
        this.clusterServers = clusterServers;
        this.currentTerm = 0;
        this.currentState = ServerStatus.CANDIDATE;
        thisServer = self;
        this.leader = new Leader(this);
        this.follower = new Follower(this, 15);
        this.candidate = new Candidate(this);
        this.messageSender = messageSender;
        this.logs = new Logs();
        this.convertToCandidate();
        //  run();
    }

    public AppendEntryRsp applyLog(AppendEntry appendEntry) {

        logger.debug("append entry log : {}", appendEntry.getEntries());
        if (appendEntry.getPreLogIndex() == 0) {  // preLogIndex 为0 执行全量替换
            logs.getLogEntries().clear();
        } else {
            int appendStart = -1;
            for (int i = 0; i < logs.getLogEntries().size(); i++) { //找到匹配位置
                if (logs.getLogEntries().get(i).getIndex() == appendEntry.getPreLogIndex() &&
                        logs.getLogEntries().get(i).getTerm() == appendEntry.getPreLogTerm()) {
                    appendStart = i;
                }
            }
            if (appendStart == -1) {  //没能找到和prelogindex prelogterm 匹配的日志
                logger.debug("can not find pre index {} term {} log ",appendEntry.getPreLogIndex(),appendEntry.getTerm());
                return new AppendEntryRsp(currentTerm, false, appendEntry);
            }
            //移除掉匹配位置后的日志
            for (int index = logs.getLogEntries().size() - 1; index > appendStart; index--) {
                logs.getLogEntries().remove(index);
            }
        }
        //添加leader的日志
        logs.getLogEntries().addAll(appendEntry.getEntries());
        commitIndex = Math.min(appendEntry.getLeaderCommit(), logs.getLastLogindex());
        logger.debug("current log :{}  commit index {}", logs.getLastEntry(), commitIndex);
        return new AppendEntryRsp(currentTerm, true, appendEntry);
    }

    public boolean isLeader() {
        return currentState.equals(ServerStatus.LEADER);
    }

    public boolean isFollower() {
        return currentState.equals(ServerStatus.FOLLOWER);
    }

    public boolean isCandidate() {
        return currentState.equals(ServerStatus.CANDIDATE);
    }


    public void convertToFollower(long term, int leader) {
        logger.debug("convert to follower cur stat:{} leader:{} ", getCurrentState(), leader);
        if (term < currentTerm)
            return;
        currentTerm = term;
        currentLeader = leader;
        this.currentState = ServerStatus.FOLLOWER;
        this.thisServer.setState(ServerStatus.FOLLOWER);
        this.follower.init();

    }

    public void convertToLeader() {
        //  if (isLeader())
        //       return;
        logger.debug("convert to leader cur stat:{}", getCurrentState());
        this.currentState = ServerStatus.LEADER;
        this.thisServer.setState(ServerStatus.LEADER);
        this.currentLeader = thisServer.getServerId();
        this.leader.init();
    }

    /**
     * 增加当前term，重置投票
     */
    void incrCurrentTerm() {
        this.currentTerm++;
        this.voteFor = 0;
    }

    public void convertToCandidate() {
        //    if (isCandidate())
        //        return;
        //   if (isFollower()) {  //从follower切换到candidate
        logger.debug("convert to candidate cur stat:{}", getCurrentState());
        this.currentState = ServerStatus.CANDIDATE;
        this.thisServer.setState(ServerStatus.CANDIDATE);
        this.candidate.init();
        // }
    }


    public ServerInfo getThisServer() {
        return thisServer;
    }

    public void setThisServer(ServerInfo thisServer) {
        this.thisServer = thisServer;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }


    public interface ServerStatus {
        String LEADER = "leader";
        String FOLLOWER = "follower";
        String CANDIDATE = "candidate";
    }


    public void setVoteFor(int voteFor) {
        this.voteFor = voteFor;
    }


    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public void setLastApplied(long lastApplied) {
        this.lastApplied = lastApplied;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public void setFollower(Follower follower) {
        this.follower = follower;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public void setClusterServers(List<ServerInfo> clusterServers) {
        this.clusterServers = clusterServers;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public int getVoteFor() {
        return voteFor;
    }


    public long getCommitIndex() {
        return commitIndex;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public String getCurrentState() {
        return currentState;
    }

    public Leader getLeader() {
        return leader;
    }

    public Follower getFollower() {
        return follower;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public List<ServerInfo> getClusterServers() {
        return clusterServers;
    }

    public Logs getLogs() {
        return logs;
    }

    public void setLogs(Logs logs) {
        this.logs = logs;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public int getCurrentLeader() {
        return currentLeader;
    }

    public void setCurrentLeader(int currentLeader) {
        this.currentLeader = currentLeader;
    }
}
