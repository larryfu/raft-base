package cn.larry.consensus.raft.net.client;

import cn.larry.consensus.raft.msg.Msg;

import java.util.function.Consumer;

public interface MessageSender {

    void sendMessage(Msg msg, Consumer<Msg> responseHandler);
}
