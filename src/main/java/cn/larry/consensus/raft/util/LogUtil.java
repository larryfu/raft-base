package cn.larry.consensus.raft.util;

public class LogUtil {
    static  final  String RPC_FLOW = "rpc_flow"; //远程调用日志
    static final  String STATUS_FLOW = "status_flow"; //raft核心状态变迁日志
    static final  String MSG_FLOW = "msg_flow";//消息流水
    static final String DATA_FLOW = "data_flow"; //数据操作日志
}
