package cn.larry.consensus.raft.net;

import java.net.InetSocketAddress;

public class RouterInfo {
    public final String ip;
    public final int port;
    public final Object attach;
    InetSocketAddress addr = null;

    public RouterInfo(String ip, int port, Object attach) {
        this.ip = ip;
        this.port = port;
        this.attach = attach;
    }

    public InetSocketAddress getSocketAddr() {
        if (addr == null)
            addr = new InetSocketAddress(ip, port);
        return addr;
    }
}