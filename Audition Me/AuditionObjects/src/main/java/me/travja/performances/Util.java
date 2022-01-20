package me.travja.performances;

import java.util.Map;

public class Util {

    public static long getLong(Map<String, String> event, String key) {
        return Long.parseLong(event.get(key));
    }

    public static void ensureExists(Map<String, String> event, String key) {
        if (!event.containsKey(key))
            throw new RuntimeException("Missing `" + key + "` in request body.");
    }

}
