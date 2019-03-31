package cn.larry.consensus.raft.net.test;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonClientHandler extends SimpleChannelInboundHandler<CommProtocolProto.CommonResponse>{

    Logger logger = LogManager.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommProtocolProto.CommonResponse msg) throws Exception {
        logger.error("receive rsp :{}",msg);
        ByteString bytes = msg.getBody();
        logger.error("rsp is:{}",bytes.toStringUtf8());
    }
}
