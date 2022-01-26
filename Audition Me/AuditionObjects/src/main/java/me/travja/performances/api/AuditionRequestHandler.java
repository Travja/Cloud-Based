package me.travja.performances.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import me.travja.performances.MethodNotSupportedException;
import me.travja.performances.api.models.Person;

import java.util.HashMap;
import java.util.Map;

public abstract class AuditionRequestHandler {

    public void postConstruct() {}

    public Map<String, Object> handleRequest(Map<String, Object> event, Context context,
                                             Person authUser, String[] path) {
        String method  = (String) event.getOrDefault("httpMethod", "GET");
        String rawPath = (String) event.getOrDefault("path", "");
        System.out.println("Raw Path is: '" + rawPath + "'");

        try {
            switch (method) {
                case "GET":
                    return handleGet(event, path, authUser);
                case "POST":
                    return handlePost(event, path, authUser);
                case "PATCH":
                    return handlePatch(event, path, authUser);
                case "DELETE":
                    return handleDelete(event, path, authUser);
            }
        } catch (Exception e) {
            System.out.println("Something went severely wrong..\n" + e.getMessage());
            throw e;
        }

        throw new MethodNotSupportedException(method + " not supported on this endpoint");
    }

    public Map<String, Object> handleGet(Map<String, Object> event, String[] path, Person authUser) {
        throw new MethodNotSupportedException("GET not supported on this endpoint");
    }

    public Map<String, Object> handlePost(Map<String, Object> event, String[] path, Person authUser) {
        throw new MethodNotSupportedException("POST not supported on this endpoint");
    }

    public Map<String, Object> handlePatch(Map<String, Object> event, String[] path, Person authUser) {
        throw new MethodNotSupportedException("PATCH not supported on this endpoint");
    }

    public Map<String, Object> handleDelete(Map<String, Object> event, String[] path, Person authUser) {
        throw new MethodNotSupportedException("DEL not supported on this endpoint");
    }

    protected static Map<String, Object> constructResponse(int statusCode, Object... data) {
        Map<String, Object> map = new HashMap<>();
        map.put("statusCode", statusCode);
        map.put("isBase64Encoded", false);
        map.put("headers", Map.of("Content-Type", "application/json"));

        Map<String, Object> body = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            body.put("statusCode", statusCode);
            body.put(String.valueOf(data[i]), data[++i]);
        }

        try {
            map.put("body", Util.getMapper().writeValueAsString(body));
        } catch (JsonProcessingException e) {
            System.err.println("Could not map value to string");
            e.printStackTrace();
        }

        return map;
    }

}
