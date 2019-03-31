package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CommonReponseEncoder extends MessageToByteEncoder<CommProtocolProto.CommonResponse> {
    protected void encode(ChannelHandlerContext channelHandlerContext, CommProtocolProto.CommonResponse response, ByteBuf byteBuf) throws Exception {
            byte[] bytes = response.toByteArray();
            int len = bytes.length+4;
            byteBuf.writeShort((short)len);
            byteBuf.writeByte(CommonPacket.START_BYTE);
            byteBuf.writeBytes(bytes);
            byteBuf.writeByte(CommonPacket.END_BYTE);
    }
}
