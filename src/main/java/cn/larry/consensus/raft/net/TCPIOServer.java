package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.RaftAlgorithm;
import cn.larry.consensus.raft.msg.CommMessageProcessor;
import cn.larry.consensus.raft.net.test.CommonServerHandler;
import cn.larry.consensus.raft.net.test.TimeServerHandler;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ChannelHandler.Sharable
public class TCPIOServer extends SimpleChannelInboundHandler<CommProtocolProto.CommonRequest> implements Runnable {

    private  Logger logger = LogManager.getLogger("RpcFlow");


    ServerBootstrap bootstrap;

    protected SocketAddress bindAddr = null;

    public CommMessageProcessor handler;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public TCPIOServer(String bindIp, int bindPort, CommMessageProcessor handler) {
        this.bindAddr = new InetSocketAddress(bindIp, bindPort);
        this.handler = handler;
    }


    public void start() throws InterruptedException {
        if (bootstrap != null)
            throw new IllegalStateException("could not start more than once");

        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new CommonReponseEncoder(),new CommonRequestDecoder(),TCPIOServer.this);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(bindAddr).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    protected void channelRead0(ChannelHandlerContext context, CommProtocolProto.CommonRequest request) throws Exception {
        logger.error(" read msg:{}",request);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                CommProtocolProto.CommonResponse response = handler.process(context, request);
                context.channel().writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            logger.error("write response fail {} ", response.getSeq());
                        }
                    }
                });
            }
        });
    }

    @Override
    public void run() {
        try {
            start();
        }catch (Exception e){
            logger.error("exception run ",e);
        }
    }
}
