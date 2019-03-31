package cn.larry.consensus.raft.net.test;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import cn.larry.consensus.raft.proto.CommProtocolProto.CommonResponse;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class CommonServerHandler extends SimpleChannelInboundHandler<CommProtocolProto.CommonRequest> {

    Logger logger = LogManager.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommProtocolProto.CommonRequest msg) throws Exception {
        ByteString byteString = msg.getBody();
        logger.error("request "+byteString.toStringUtf8());
        CommonResponse.Builder rspBuilder = CommonResponse.newBuilder();
        rspBuilder.setMsg("ok");
        rspBuilder.setSeq(msg.getSeq());
        rspBuilder.setCode(0);
        rspBuilder.setBody(ByteString.copyFromUtf8("now is "+new Date()));
        ctx.channel().writeAndFlush(rspBuilder.build());
    }
}
