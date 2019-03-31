package cn.larry.consensus.raft.net.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger();


    private static Map<ChannelId,UnixTime> channelMsgMap = new ConcurrentHashMap<>();

//    @Override
//    public void channelActive(ChannelHandlerContext context) {
//     //  logger.info("channel id:"+context.channel().id()+" thread :"+Thread.currentThread());
//    //   UnixTime time = channelMsgMap.get(context.channel().id());
//     //  context.writeAndFlush( new UnixTime());
//        //context.flush();
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        UnixTime m = (UnixTime) msg;
        System.out.println( "receive server response:"+m);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
