package cn.larry.consensus.raft;

import cn.larry.consensus.raft.net.client.TCPIOClient;
import cn.larry.consensus.raft.proto.CommProtocolProto;

import java.util.Random;

public class Client {

    //public static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        System.setProperty("log.subdir","client");

        TCPIOClient ioClient = new TCPIOClient();
         String ip = "127.0.0.1";
         int port = 16000;
        CommProtocolProto.CommonRequest.Builder commonreqBuilder = CommProtocolProto.CommonRequest.newBuilder();
        CommProtocolProto.ClientRequest.Builder requestBuilder = CommProtocolProto.ClientRequest.newBuilder();
        String[] baseCommand = {"append","update","delete"};
        Random random = new Random();

        for(int i= 0;i<100;i++){
            String  command = baseCommand[random.nextInt(3)];
            int value = random.nextInt(10);
            String cmd= command+" "+value;
            System.out.println(cmd);
            requestBuilder.setCommand(cmd);
            commonreqBuilder.setServerIp(ip);
            commonreqBuilder.setBody(requestBuilder.build().toByteString());
            commonreqBuilder.setVersion(1);
            commonreqBuilder.setMsgType(CommProtocolProto.MSG_TYPE.CLIENT_REQUEST_VALUE);
            CommProtocolProto.CommonResponse response = ioClient.sendSync(ip, port, commonreqBuilder, null);
            System.out.println("response: "+response.toString());
        }

    }
}
