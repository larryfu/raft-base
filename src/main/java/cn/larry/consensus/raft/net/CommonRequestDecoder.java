package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class CommonRequestDecoder  extends ByteToMessageDecoder {

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> list) throws Exception {
        if (buf.readableBytes() < 3) {
            return;
        }
        buf.markReaderIndex();
        // 协议的头两个字节为总长度
        short pkgLength = buf.readShort();
        if (pkgLength < 4) {
            throw new IllegalStateException(
                    " package must not less than 4");
        }
        // 起始字节必须为0x28
        if (buf.readByte() != CommonPacket.START_BYTE) {
            throw new IllegalStateException(
                    " package start byte must be 0x28");
        }
        // 剩余的协议包体和结束字节
        if (buf.readableBytes() < pkgLength - 3) {
            buf.resetReaderIndex();
            return;
        }
        byte[] array = new byte[pkgLength - 4];
        buf.readBytes(array);
        CommProtocolProto.CommonRequest request = CommProtocolProto.CommonRequest.parseFrom(array);
        byte endByte = buf.readByte();
        if (endByte != CommonPacket.END_BYTE) {
            throw new IllegalStateException(
                    " package end byte must be 0x3");
        }
        list.add(request);
    }
}
