package cn.larry.consensus.raft.net;

import cn.larry.consensus.raft.proto.CommProtocolProto;
import cn.larry.consensus.raft.proto.CommProtocolProto.CommonRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientTest {

    private static  Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        TCPIOClient client = new TCPIOClient();
        CommonRequest.Builder reqBuilder = CommonRequest.newBuilder();
        reqBuilder.setSeq(1);
        reqBuilder.setClientIp("127.0.0.1");
        reqBuilder.setServerIp("127.0.0.1");
        reqBuilder.setMsgType(1000);
        reqBuilder.setVersion(1000);
        CommProtocolProto.CommonResponse response = client.sendSync("127.0.0.1",8080,reqBuilder,null);
        logger.error("response:{}", response);

    }
}
