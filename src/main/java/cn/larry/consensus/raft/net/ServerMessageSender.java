package cn.larry.consensus.raft.net;


import cn.larry.consensus.raft.msg.*;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import cn.larry.consensus.raft.proto.CommProtocolProto.CommonRequest;
import cn.larry.consensus.raft.proto.CommProtocolProto.MSG_TYPE;
import cn.larry.consensus.raft.util.RandomString;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ServerMessageSender implements MessageSender {

    public static  AtomicInteger seqGenerator = new AtomicInteger(1);

    private TCPIOClient client;

    private ExecutorService senderService = Executors.newCachedThreadPool();

    private final Logger logger = LogManager.getLogger("RpcFlow");

    public ServerMessageSender(TCPIOClient client) {
        this.client = client;
    }

    public void sendASync(String host, int port, Msg msg, Consumer<Msg> callback) {
        logger.debug("send msg:{} to :{}", msg, host + port);
        senderService.execute(new Runnable() {
            @Override
            public void run() {
                CommonRequest request = buildRequest(msg);
                try {
                    CommProtocolProto.CommonResponse response = client.sendSync(host, port, request.toBuilder(), null);
                    Msg rspMsg = extractResponse(response, msg);
                    callback.accept(rspMsg);
                } catch (Exception e) {
                    logger.error("exception send message", e);
                }
            }
        });
    }

    private Msg extractResponse(CommProtocolProto.CommonResponse response, Msg req) throws InvalidProtocolBufferException {
        Message message = parseMessage(response.getMsgType(), response.getBody());
        Msg msg = MsgPbConverter.pb2Msg(message);
        msg.setFrom(req.getTo());
        msg.setTo(req.getFrom());
        msg.setReceiveTime(System.currentTimeMillis());
        msg.setMsgId(RandomString.genMsgId());
        if (msg instanceof AppendEntryRsp && req instanceof AppendEntry) {
            ((AppendEntryRsp) msg).setRequest((AppendEntry) req);
        }
        if (msg instanceof RequestVoteRsp && req instanceof RequestVote) {
            ((RequestVoteRsp) msg).setRequest((RequestVote) req);
        }
        return msg;
    }

    private Message parseMessage(int msgType, ByteString body) throws InvalidProtocolBufferException {
        if (msgType == MSG_TYPE.APPEND_ENTRY_RSP_VALUE) {
            CommProtocolProto.AppendEntryRsp rsp = CommProtocolProto.AppendEntryRsp.parseFrom(body);
            return rsp;
        } else if (msgType == MSG_TYPE.REQUEST_VOTE_RSP_VALUE) {
            CommProtocolProto.RequestVoteRsp rsp = CommProtocolProto.RequestVoteRsp.parseFrom(body);
            return rsp;
        }
        return null;
    }

    public CommonRequest buildRequest(Msg msg) {
        Message message = MsgPbConverter.msg2PB(msg);
        CommonRequest.Builder reqBuilder = CommonRequest.newBuilder();
        reqBuilder.setBody(message.toByteString());
        reqBuilder.setSeq(seqGenerator.incrementAndGet());
        int msgType = msg instanceof AppendEntry ? MSG_TYPE.APPEND_ENTRY_VALUE :
                msg instanceof RequestVote ? MSG_TYPE.REQUEST_VOTE_VALUE : 0;
        reqBuilder.setMsgType(msgType);
        return reqBuilder.build();
    }

    @Override
    public void sendMessage(Msg msg, Consumer<Msg> rspHandler) {
        String dest = msg.getTo();

        if (dest == null || dest.isEmpty()) {
            logger.error("invalid destination :{}", dest);
            return;
        }
        String[] hostAndPort = dest.split(":");
        int port = Integer.parseInt(hostAndPort[1]);
        sendASync(hostAndPort[0], port, msg, rspHandler);
    }
}
