package cn.larry.consensus.raft.util;

import java.util.Random;

public class RandomString {


    private static final String BASE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public static String randomString(int len) {
        int total = BASE_CHARS.length();
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int index = random.nextInt(total);
            sb.append(BASE_CHARS.charAt(index));
        }
        return sb.toString();
    }

    public static String genMsgId() {
        String date = String.valueOf(System.currentTimeMillis());
        String randomStr = RandomString.randomString(8);
        return date + randomStr;
    }


}
