package me.travja.performances.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import me.travja.performances.MethodNotSupportedException;
import me.travja.performances.api.models.LambdaRequest;

import java.util.HashMap;
import java.util.Map;

public abstract class AuditionRequestHandler {

    protected static final StateManager state                  = StateManager.getInstance();
    protected              boolean      clearCacheOnNewRequest = true;

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

    public void postConstruct() {}

    public Map<String, Object> handleRequest(LambdaRequest request, Context context,
                                             String[] path) {
        if (clearCacheOnNewRequest)
            state.clearCache();
        String method  = request.getString("httpMethod", "GET");
        String rawPath = request.getString("path", "");
        System.out.println("Raw Path is: '" + rawPath + "'");

        try {
            switch (method) {
                case "GET":
                    return handleGet(request, path);
                case "POST":
                    return handlePost(request, path);
                case "PATCH":
                    return handlePatch(request, path);
                case "DELETE":
                    return handleDelete(request, path);
            }
        } catch (Exception e) {
            System.out.println("Something went severely wrong..\n" + e.getMessage());
            throw e;
        }

        throw new MethodNotSupportedException(method + " not supported on this endpoint");
    }

    public Map<String, Object> handleGet(LambdaRequest request, String[] path) {
        throw new MethodNotSupportedException("GET not supported on this endpoint");
    }

    public Map<String, Object> handlePost(LambdaRequest request, String[] path) {
        throw new MethodNotSupportedException("POST not supported on this endpoint");
    }

    public Map<String, Object> handlePatch(LambdaRequest request, String[] path) {
        throw new MethodNotSupportedException("PATCH not supported on this endpoint");
    }

    public Map<String, Object> handleDelete(LambdaRequest request, String[] path) {
        throw new MethodNotSupportedException("DEL not supported on this endpoint");
    }

    protected String getAuthHeader(LambdaRequest request) {
        if (request.contains("headers")
                && ((Map<String, String>) request.get("headers")).containsKey("Authorization"))
            return ((Map<String, String>) request.get("headers")).get("Authorization");

        return null;
    }

}
