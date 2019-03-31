package cn.larry.consensus.raft;

import cn.larry.consensus.raft.msg.ClientMessageProcessor;
import cn.larry.consensus.raft.msg.CommMessageProcessor;
import cn.larry.consensus.raft.msg.ServerMessageProcessor;
import cn.larry.consensus.raft.net.ServerMessageSender;
import cn.larry.consensus.raft.net.TCPIOClient;
import cn.larry.consensus.raft.net.TCPIOServer;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BootStrap {

    private static ExecutorService coreExecutor = Executors.newFixedThreadPool(8);

    //public static   Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("usage : RaftServer  configPath");
            System.exit(1);
        }

        Configurations configs = new Configurations();
        // setDefaultEncoding是个静态方法,用于设置指定类型(class)所有对象的编码方式。
        // 本例中是PropertiesConfiguration,要在PropertiesConfiguration实例创建之前调用。
        FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, "UTF-8");
        PropertiesConfiguration propConfig = configs.properties(args[0]);
        Configuration configuration = propConfig.subset("server");
        Iterator<String> iterator = configuration.getKeys();
        Integer selfId = propConfig.getInt("self.id");
        String logdir = propConfig.getString("log.dir");
        if(logdir!=null){
            System.setProperty("log.subdir",logdir);
        }else {
            System.setProperty("log.subdir","log");
        }
        ServerInfo thisServer = null;
        List<ServerInfo> serverInfos = new ArrayList<>();
        String bindIp = null;
        while (iterator.hasNext()) {
            String id = iterator.next();
            String conf = configuration.getString(id);
            int serverId = Integer.parseInt(id);
            String[] ss = conf.split(":");
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServerId(serverId);
            serverInfo.setHost(ss[0]);
            serverInfo.setPort(Integer.parseInt(ss[1]));
            serverInfo.setClientPort(Integer.parseInt(ss[2]));
            serverInfos.add(serverInfo);
            if (serverId == selfId) {
                thisServer = serverInfo;
                bindIp = serverInfo.getHost();
            }
        }
        if (serverInfos.size() == 0 || thisServer == null) {
            System.err.println("config error");
            System.exit(1);
        }
        System.out.println(("start server at {} {}"+thisServer.getPort()+"," +thisServer.getClientPort()));

        ServerMessageSender messageSender = new ServerMessageSender(new TCPIOClient());
        RaftAlgorithm raftAlgorithm = new RaftAlgorithm(serverInfos, thisServer, messageSender);
        /**
         * 接收集群间消息的server
         */
        System.out.println("start cluster service");
        CommMessageProcessor serverprocessor = new ServerMessageProcessor(raftAlgorithm);
        TCPIOServer clusterService = new TCPIOServer(bindIp, thisServer.getPort(), serverprocessor);
        coreExecutor.submit(clusterService);

        /**
         * 接收集群间消息的server
         */
        System.out.println("start client service");
        CommMessageProcessor clientProcessor = new ClientMessageProcessor(raftAlgorithm);
        TCPIOServer clientService = new TCPIOServer(bindIp, thisServer.getClientPort(), clientProcessor);
        coreExecutor.submit(clientService);
        System.out.println("start core loop");
        coreExecutor.submit(raftAlgorithm);
    }

    /**
     * 获取本机ip
     *
     * @return
     */
    public static String getIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
