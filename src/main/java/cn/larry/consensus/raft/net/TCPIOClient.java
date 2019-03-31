package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.msg.CallbackMessageHandler;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@ChannelHandler.Sharable
public class TCPIOClient extends SimpleChannelInboundHandler<CommProtocolProto.CommonResponse> {


    private static AtomicInteger seqGenerator = new AtomicInteger();

    private Bootstrap bootstrap;

    private CallbackMessageHandler messageHandler;

    private int channelsPerAddr = 2;

    public static Map<Integer, CommProtocolProto.CommonResponse> msgResponseMap = new ConcurrentHashMap<>();

    protected ConcurrentHashMap<String, MyChannelGroup> groups =
            new ConcurrentHashMap<String, MyChannelGroup>(16);


    private final Logger logger = LogManager.getLogger("RpcFlow");

    public TCPIOClient() throws Exception {
        this.messageHandler = new CallbackMessageHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap(); // (1)
            bootstrap.group(group); // (2)
            bootstrap.channel(NioSocketChannel.class); // (3)
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new CommonRequestEncoder(), new CommonResponseDecoder(), TCPIOClient.this);
                }
            });

        } catch (Exception e) {
            logger.error("exception", e);
        }
    }


    public CommProtocolProto.CommonResponse sendSync(String ip, int port, CommProtocolProto.CommonRequest.Builder request, ChannelFutureListener listener) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        request.setSeq(seqGenerator.incrementAndGet());
        messageHandler.putCallback(request.getSeq(), new Consumer<CommProtocolProto.CommonResponse>() {
            @Override
            public void accept(CommProtocolProto.CommonResponse response) {
                if (response != null)
                    msgResponseMap.put((int) response.getSeq(), response);
                latch.countDown();
            }
        });
        RouterInfo routerInfo = new RouterInfo(ip, port, null);
        Channel channel = getChannel(routerInfo);
       ChannelFuture future = channel.writeAndFlush(request.build());
        if (listener != null)
            future.addListener(listener);
        boolean result = latch.await(5, TimeUnit.SECONDS);
        if(!result){
             logger.error("latch await timeout msg to:{} request:{}",ip+","+port,request);
        }
        channel.close();
        CommProtocolProto.CommonResponse response = msgResponseMap.remove(request.getSeq());
        return response;
    }


    protected Channel getChannel(RouterInfo routerInfo) throws InterruptedException {
        final String key = routerInfo.ip + ":" + routerInfo.port;
        MyChannelGroup group = groups.get(key);
        if (group == null) {
            group = new MyChannelGroup(this.channelsPerAddr);
            MyChannelGroup oGroup = groups.putIfAbsent(key, group);
            if (oGroup != null)
                group = oGroup;
        }
        int chIndex = group.seq.getAndIncrement() % group.channels.length;
        AtomicReference<Channel> ref = group.channels[chIndex];

        logger.debug(Thread.currentThread().getId() + ", index=" + chIndex + ", old=" + ref.get());

        return getChannel(routerInfo, ref, 2);
    }

    protected Channel getChannel(RouterInfo routerInfo, AtomicReference<Channel> ref, int retry) throws InterruptedException {
        Channel ch = ref.get();
        if (ch != null && ch.isActive()) {
            return ch;
        }
        if (ch != null) {
            ch.close();
        }
        ChannelFuture f = bootstrap.connect(routerInfo.getSocketAddr()).sync();
        logger.debug(System.currentTimeMillis() + " new channel " + f.channel().localAddress() + "|" + routerInfo.getSocketAddr());

        if (ref.compareAndSet(ch, f.channel())) {
            logger.error(Thread.currentThread().getId() + ", old=" + ch + ", new" + f.channel());
            return f.channel();
        } else {
            f.cancel(false);
            f.channel().close();
            if (retry <= 0) {
                throw new IllegalStateException("connect to " + routerInfo.getSocketAddr() + " failed with retry, no channel available");
            } else {
                return getChannel(routerInfo, ref, retry - 1);
            }
        }
    }

    static class MyChannelGroup {
        final AtomicInteger seq = new AtomicInteger(0);
        final AtomicReference<Channel>[] channels;

        MyChannelGroup(int channels) {
            this.channels = new AtomicReference[channels];
            for (int i = 0; i < this.channels.length; i++) {
                this.channels[i] = new AtomicReference<Channel>(null);
            }
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommProtocolProto.CommonResponse msg) throws Exception {
        logger.error("read msg:{}", msg);
        Consumer<CommProtocolProto.CommonResponse> callback = messageHandler.getCallBack(msg.getSeq());
        if (callback != null) {
            callback.accept(msg);
        }
    }
}
