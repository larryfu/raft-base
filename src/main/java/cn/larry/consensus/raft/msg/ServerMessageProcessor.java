package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.RaftAlgorithm;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import cn.larry.consensus.raft.proto.CommProtocolProto.CommonResponse;
import cn.larry.consensus.raft.proto.CommProtocolProto.MSG_TYPE;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerMessageProcessor implements CommMessageProcessor {


    private RaftAlgorithm raftAlgorithm;

    public ServerMessageProcessor(RaftAlgorithm raftAlgorithm) {
        this.raftAlgorithm = raftAlgorithm;
    }

    private static Logger logger = LogManager.getLogger();


    final Map<String, Object> processResult = new ConcurrentHashMap<>();

    @Override
    public CommonResponse process(ChannelHandlerContext context, CommProtocolProto.CommonRequest request) {
        logger.debug("process received msg");
        CommonResponse.Builder rspBuilder = CommonResponse.newBuilder();
        rspBuilder.setSeq(request.getSeq());
        InetSocketAddress fromAddress = (InetSocketAddress) context.channel().remoteAddress();
        try {
            if (request.getMsgType() == MSG_TYPE.APPEND_ENTRY_VALUE) {
                logger.debug("process append entry msg");
                CommProtocolProto.AppendEntryReq req = CommProtocolProto.AppendEntryReq.parseFrom(request.getBody());
                CommProtocolProto.AppendEntryRsp rsp = processAppendEntry(req, fromAddress);
                rspBuilder.setMsgType(MSG_TYPE.APPEND_ENTRY_RSP_VALUE);
                if (rsp == null) {
                    rspBuilder.setCode(10003);
                    rspBuilder.setMsg("process msg error");
                    return rspBuilder.build();
                } else {
                    rspBuilder.setCode(0);
                    rspBuilder.setMsg("ok");
                    rspBuilder.setBody(rsp.toByteString());
                }
            } else if (request.getMsgType() == MSG_TYPE.REQUEST_VOTE_VALUE) {
                logger.error("process request vote");
                CommProtocolProto.RequestVoteReq req = CommProtocolProto.RequestVoteReq.parseFrom(request.getBody());
                CommProtocolProto.RequestVoteRsp rsp = processRequestVote(req, fromAddress);
                rspBuilder.setMsgType(MSG_TYPE.REQUEST_VOTE_RSP_VALUE);
                if (rsp == null) {
                    rspBuilder.setCode(10003);
                    rspBuilder.setMsg("process msg error");
                    return rspBuilder.build();
                } else {
                    rspBuilder.setCode(0);
                    rspBuilder.setMsg("ok");
                    rspBuilder.setBody(rsp.toByteString());
                }
            }else  if(request.getMsgType() == MSG_TYPE.CLIENT_REQUEST_VALUE) {
                CommProtocolProto.ClientRequest request1 = CommProtocolProto.ClientRequest.parseFrom(request.getBody());
                CommProtocolProto.ClientResponse rsp = processClientRequest(request1, fromAddress);
                rspBuilder.setMsgType(MSG_TYPE.CLIENT_REQUEST_RSP_VALUE);
                if (rsp == null) {
                    rspBuilder.setCode(10003);
                    rspBuilder.setMsg("process msg error");
                    return rspBuilder.build();
                } else {
                    rspBuilder.setCode(0);
                    rspBuilder.setMsg("ok");
                    rspBuilder.setBody(rsp.toByteString());
                }
            }else {
                logger.error("can not handle msg type:{}",request.getMsgType());
            }
            return rspBuilder.build();
        } catch (InvalidProtocolBufferException e) {
            logger.error("invalid msg :{}", request);
            rspBuilder.setCode(10005);
            rspBuilder.setMsg("invalid msg");
            return rspBuilder.build();
        } catch (Exception e) {
            logger.error("process req error", e);
            rspBuilder.setCode(10001);
            rspBuilder.setMsg(e.getMessage());
            return rspBuilder.build();
        }
    }

    public CommProtocolProto.ClientResponse processClientRequest(CommProtocolProto.ClientRequest request,InetSocketAddress fromAddress) throws InterruptedException{
        ClientRequest req = new ClientRequest();
        req.setCommand(request.getCommand());
        String ip = fromAddress.getAddress().getHostAddress();
        String from = ip + ":" + fromAddress.getPort();
        req.setFrom(from);
        final CountDownLatch latch = new CountDownLatch(1);
        raftAlgorithm.putClientMessage(req, new Consumer() {
            @Override
            public void accept(Object o) {
                logger.debug("msgId:{} object:{}",req.msgId,o);
                processResult.put(req.msgId, o);
                latch.countDown();
            }
        });
        boolean result = latch.await(10, TimeUnit.SECONDS);
        if (!result) {
            logger.error("wait process result time out msgid:{}", req.msgId);
        }
        Object obj = processResult.remove(req.msgId);
        if (obj instanceof ClientRsp) {
            ClientRsp rsp = (ClientRsp)obj;
           CommProtocolProto.ClientResponse.Builder builder = CommProtocolProto.ClientResponse.newBuilder();
           builder.setRetcode(rsp.retCode);
           builder.setLeader(rsp.leader);
           builder.setLeaderPort(rsp.leaderPort);
           builder.setMsg(rsp.msg);
           return builder.build();
        }
        logger.error("process result invalid msgid:{} result:{}", obj);
        return null;
    }


    public CommProtocolProto.RequestVoteRsp processRequestVote(CommProtocolProto.RequestVoteReq req, InetSocketAddress fromAddress) throws InterruptedException {
        RequestVote requestVote = new RequestVote();
        requestVote.setReceiveTime(System.currentTimeMillis());
        requestVote.setCandidateId(req.getCandidateId());
        requestVote.setTerm(req.getTerm());
        requestVote.setLastLogIndex(req.getLastLogIndex());
        requestVote.setLastLogTerm(req.getLastLogTerm());
        String ip = fromAddress.getAddress().getHostAddress();
        String from = ip + ":" + fromAddress.getPort();
        requestVote.setFrom(from);
        final CountDownLatch latch = new CountDownLatch(1);
        raftAlgorithm.putMessage(requestVote, new Consumer() {
            @Override
            public void accept(Object o) {
                logger.debug("msgId:{} object:{}",requestVote.msgId,o);
                processResult.put(requestVote.msgId, o);
                latch.countDown();
            }
        });
        boolean result = latch.await(10, TimeUnit.SECONDS);
        if (!result) {
            logger.error("wait process result time out msgid:{}", requestVote.msgId);
        }
        Object obj = processResult.remove(requestVote.msgId);
        if (obj instanceof RequestVoteRsp) {
            CommProtocolProto.RequestVoteRsp.Builder rspBuilder = CommProtocolProto.RequestVoteRsp.newBuilder();
            rspBuilder.setVoteGranted(((RequestVoteRsp) obj).isVoteGranted());
            rspBuilder.setTerm(((RequestVoteRsp) obj).getTerm());
            return rspBuilder.build();
        }
        logger.error("process result invalid msgid:{} result:{}", obj);
        return null;
    }


    private CommProtocolProto.AppendEntryRsp processAppendEntry(CommProtocolProto.AppendEntryReq req, InetSocketAddress fromAddress) throws InterruptedException {
        AppendEntry appendEntry = new AppendEntry();
        appendEntry.setReceiveTime(System.currentTimeMillis());
        String ip = fromAddress.getAddress().getHostAddress();
        String from = ip + ":" + fromAddress.getPort();
        appendEntry.setFrom(from);
        appendEntry.setTo(raftAlgorithm.getThisServer().getHost() + ":" + raftAlgorithm.getThisServer().getPort());
        appendEntry.setLeaderId(req.getLeaderId());
        appendEntry.setLeaderCommit(req.getLeaderCommit());
        appendEntry.setTerm(req.getTerm());
        appendEntry.setPreLogIndex(req.getPreLogIndex());
        appendEntry.setPreLogTerm(req.getPreLogTerm());
        appendEntry.setAllReplace(req.getAllReplace());
        appendEntry.setEntries(req.getEntriesList());
        final CountDownLatch latch = new CountDownLatch(1);
        raftAlgorithm.putMessage(appendEntry, new Consumer() {
            @Override
            public void accept(Object o) {
                processResult.put(appendEntry.msgId, o);
                latch.countDown();
            }
        });
        boolean result = latch.await(10, TimeUnit.SECONDS);
        if (!result) {
            logger.error("wait process result time out msgid:{}", appendEntry.msgId);
        }
        Object obj = processResult.remove(appendEntry.msgId);
        if (obj instanceof AppendEntryRsp) {
            CommProtocolProto.AppendEntryRsp.Builder rspBuilder = CommProtocolProto.AppendEntryRsp.newBuilder();
            rspBuilder.setSuccess(((AppendEntryRsp) obj).success);
            rspBuilder.setTerm(((AppendEntryRsp) obj).getTerm());
            return rspBuilder.build();
        }
        logger.error("process result invalid msgid:{} result:{}", obj);
        return null;
    }
}
