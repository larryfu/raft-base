package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CommonRequestEncoder extends MessageToByteEncoder<CommProtocolProto.CommonRequest> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, CommProtocolProto.CommonRequest request, ByteBuf byteBuf) throws Exception {

        byte[] bytes = request.toByteArray();
        int len = bytes.length+4;
        byteBuf.writeShort((short)len);
        byteBuf.writeByte(CommonPacket.START_BYTE);
        byteBuf.writeBytes(bytes);
        byteBuf.writeByte(CommonPacket.END_BYTE);
    }
}
