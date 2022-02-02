package me.travja.performances.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LambdaRequest {
    private Map<String, Object> data     = new HashMap<>();
    private Person              authUser = null;

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        return String.valueOf(data.get(key));
    }

    public String getString(String key, String def) {
        String ret = getString(key);
        return ret != null ? ret : def;
    }

    public Long getLong(String key) {
        return Long.valueOf(getString(key));
    }

    public UUID getUUID(String key) {
        return UUID.fromString(getString(key));
    }

    public void ensureExists(String... keys) {
        for (String key : keys) {
            if (!contains(key))
                throw new RuntimeException("Missing `" + key + "` in request body.");
        }
    }
}