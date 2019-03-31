package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.channel.ChannelHandlerContext;

public interface CommMessageProcessor {

   CommProtocolProto.CommonResponse process(ChannelHandlerContext context, CommProtocolProto.CommonRequest request);
}
