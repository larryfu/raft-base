package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import cn.larry.consensus.raft.proto.CommProtocolProto.AppendEntryReq;
import cn.larry.consensus.raft.proto.CommProtocolProto.RequestVoteReq;
import com.google.protobuf.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsgPbConverter {

    public static Logger logger = LogManager.getLogger();

    public static Message msg2PB(Msg msg) {

        //将AppendEntry 转为Pb
        if (msg instanceof AppendEntry) {
            AppendEntry aeMsg = (AppendEntry) msg;
            AppendEntryReq.Builder reqBuilder = AppendEntryReq.newBuilder();
            reqBuilder.setTerm(aeMsg.getTerm());
            reqBuilder.setAllReplace(aeMsg.isAllReplace());
            reqBuilder.addAllEntries(aeMsg.getEntries());
            reqBuilder.setLeaderCommit(aeMsg.getLeaderCommit());
            reqBuilder.setLeaderId(aeMsg.getLeaderId());
            reqBuilder.setPreLogIndex(aeMsg.getPreLogIndex());
            reqBuilder.setPreLogTerm(aeMsg.getPreLogTerm());
            return reqBuilder.build();
            //将RequestVote 转为Pb
        } else if (msg instanceof RequestVote) {
            RequestVote rvMsg = (RequestVote) msg;
            RequestVoteReq.Builder reqBuilder = RequestVoteReq.newBuilder();
            reqBuilder.setCandidateId(rvMsg.candidateId);
            reqBuilder.setLastLogIndex(rvMsg.lastLogIndex);
            reqBuilder.setLastLogTerm(rvMsg.lastLogTerm);
            reqBuilder.setTerm(rvMsg.term);
            return reqBuilder.build();
        }else if(msg instanceof AppendEntryRsp){
            CommProtocolProto.AppendEntryRsp.Builder rsp = CommProtocolProto.AppendEntryRsp.newBuilder();
            rsp.setTerm(((AppendEntryRsp) msg).getTerm());
            rsp.setSuccess(((AppendEntryRsp) msg).isSuccess());
            return rsp.build();
        }else if(msg instanceof RequestVoteRsp){
            CommProtocolProto.RequestVoteRsp.Builder rspBuilder = CommProtocolProto.RequestVoteRsp.newBuilder();
            rspBuilder.setTerm(((RequestVoteRsp) msg).getTerm());
            rspBuilder.setVoteGranted(((RequestVoteRsp) msg).isVoteGranted());
            return rspBuilder.build();
        }else if(msg instanceof  InstallSnapshot) {

        }else if(msg instanceof InstallSnapshotRsp){

        }else {
            logger.error("can not convert msg to pb :{}",msg);

        }

        return null;
    }

    public static Msg pb2Msg(Message pbMsg) {
        //将AppendEntryReq pb转为msg
        if (pbMsg instanceof AppendEntryReq) {
            AppendEntryReq aeReq = (AppendEntryReq) pbMsg;
            AppendEntry appendEntry = new AppendEntry();
            appendEntry.setEntries(aeReq.getEntriesList());
            appendEntry.setAllReplace(aeReq.getAllReplace());
            appendEntry.setPreLogTerm(aeReq.getPreLogTerm());
            appendEntry.setPreLogIndex(aeReq.getPreLogIndex());
            appendEntry.setTerm(aeReq.getTerm());
            appendEntry.setLeaderCommit(aeReq.getLeaderCommit());
            appendEntry.setLeaderId(aeReq.getLeaderId());
            return appendEntry;
            //将RequestVoteReq pb转为msg
        } else if (pbMsg instanceof RequestVoteReq) {
            RequestVote requestVote = new RequestVote();
            RequestVoteReq requestVoteReq = (RequestVoteReq) pbMsg;
            requestVote.setLastLogTerm(requestVoteReq.getLastLogTerm());
            requestVote.setLastLogIndex(requestVoteReq.getLastLogIndex());
            requestVote.setTerm(requestVoteReq.getTerm());
            requestVote.setCandidateId(requestVoteReq.getCandidateId());
            return requestVote;
        }else if(pbMsg instanceof CommProtocolProto.AppendEntryRsp){
            AppendEntryRsp rsp = new AppendEntryRsp();
            rsp.setSuccess(((CommProtocolProto.AppendEntryRsp) pbMsg).getSuccess());
            rsp.setTerm(((CommProtocolProto.AppendEntryRsp) pbMsg).getTerm());
            return rsp;
        }
        else if(pbMsg instanceof CommProtocolProto.RequestVoteRsp){
            RequestVoteRsp rsp = new RequestVoteRsp();
            rsp.setTerm(((CommProtocolProto.RequestVoteRsp) pbMsg).getTerm());
            rsp.setVoteGranted(((CommProtocolProto.RequestVoteRsp) pbMsg).getVoteGranted());
            return rsp;
        }else  {
            logger.error("can not pb  to msg :{}",pbMsg);
        }
        return null;
    }
}
