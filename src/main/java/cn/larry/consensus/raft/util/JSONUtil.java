package cn.larry.consensus.raft.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }
}
