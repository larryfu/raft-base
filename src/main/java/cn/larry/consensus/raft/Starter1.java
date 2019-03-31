package cn.larry.consensus.raft;


public class Starter1 {

  //  public static ExecutorService executorService = Executors.newFixedThreadPool(4);
    public static void main(String[] args) throws Exception {
        String[] arg1 = new String[]{"D:\\home\\raft\\cluster1.conf"};
       BootStrap.main(arg1);
    }

    public static void start(String[] args)  {
        try {
            BootStrap.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
