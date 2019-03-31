package cn.larry.consensus.raft.net.test;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger();
//    @Override
//    public void channelActive(final ChannelHandlerContext ctx) { // (1)
//        ChannelFuture f = ctx.writeAndFlush(new UnixTime());
//        f.addListener(ChannelFutureListener.CLOSE);
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CommProtocolProto.CommonResponse time = (CommProtocolProto.CommonResponse) msg;
        System.out.println("receive time:{}"+time.toString());
        ChannelFuture f = ctx.channel().writeAndFlush(msg);
      //  f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}