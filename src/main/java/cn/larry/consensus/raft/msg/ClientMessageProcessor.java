package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.RaftAlgorithm;
import cn.larry.consensus.raft.proto.CommProtocolProto;
import io.netty.channel.ChannelHandlerContext;

public class ClientMessageProcessor implements CommMessageProcessor {

    private RaftAlgorithm raftAlgorithm;

    public ClientMessageProcessor(RaftAlgorithm raftAlgorithm){
        this.raftAlgorithm = raftAlgorithm;
    }

    @Override
    public CommProtocolProto.CommonResponse process(ChannelHandlerContext context, CommProtocolProto.CommonRequest request) {
        return null;
    }
}
