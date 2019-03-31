package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.msg.CommMessageProcessor;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import cn.larry.consensus.raft.proto.CommProtocolProto.CommonResponse;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerTest {

    private static  Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {
        CommMessageProcessor processor = new CommMessageProcessor() {
            @Override
            public CommonResponse process(ChannelHandlerContext context, CommProtocolProto.CommonRequest request) {
                logger.error("receive request:{}",request);
                CommonResponse.Builder rspBuilder = CommonResponse.newBuilder();
                rspBuilder.setMsg("ok");
                rspBuilder.setCode(0);
                rspBuilder.setSeq(request.getSeq());
                rspBuilder.setMsgType(request.getMsgType());
               // context.channel().writeAndFlush(rspBuilder.build());
                return rspBuilder.build();
            }
        };

        TCPIOServer server = new TCPIOServer("127.0.0.1",9000,processor);
        server.start();
    }
}
