package cn.larry.consensus.raft.msg;

import cn.larry.consensus.raft.proto.CommProtocolProto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CallbackMessageHandler {

    private static Map<Long,Consumer<CommProtocolProto.CommonResponse>> msgCallbackMap = new ConcurrentHashMap<>();


    public void processResponse(CommProtocolProto.CommonResponse response){
        Consumer<CommProtocolProto.CommonResponse> consumer = msgCallbackMap.get(response.getSeq());
        consumer.accept(response);
    }

    public void putCallback(long seq, Consumer<CommProtocolProto.CommonResponse> callback){
        msgCallbackMap.put(seq,callback);
    }

    public Consumer<CommProtocolProto.CommonResponse> getCallBack(long seq){
        return msgCallbackMap.get(seq);
    }
}
