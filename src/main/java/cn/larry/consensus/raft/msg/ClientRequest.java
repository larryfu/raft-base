package cn.larry.consensus.raft.msg;

public class ClientRequest  extends Msg {

    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
