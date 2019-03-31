package cn.larry.consensus.raft.net.test;

import cn.larry.consensus.raft.net.CommonRequestEncoder;
import cn.larry.consensus.raft.net.CommonResponseDecoder;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeClient {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";  // args[0];
        int port = 8080;//Integer.parseInt(args[1]);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new CommonRequestEncoder(), new CommonResponseDecoder(), new CommonClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port);
            //   f.sync();
            // (5)
            // f.channel();
            // logger.info(f.channel().id());
            // Wait until the connection is closed.

            CommProtocolProto.CommonRequest.Builder reqBuiler = CommProtocolProto.CommonRequest.newBuilder();
            reqBuiler.setSeq(1);
            reqBuiler.setVersion(1);
            reqBuiler.setClientIp("127.0.0.1");
            reqBuiler.setServerIp("127.0.0.1");
            reqBuiler.setBody(ByteString.copyFromUtf8("get time "));

           // f.channel().writeAndFlush(reqBuiler.build());
            logger.error("start sync");
            f.sync();
            f.channel().writeAndFlush(reqBuiler.build());
            logger.error("end sync");
            Thread.sleep(3000);
            f.channel().close();
         //   f.channel().writeAndFlush(reqBuiler.build());
          //  Thread.sleep(3000);
          //  f.channel().writeAndFlush(reqBuiler.build());
         //   f.channel().closeFuture().sync();
            logger.error("close");
            //f.channel().close();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
