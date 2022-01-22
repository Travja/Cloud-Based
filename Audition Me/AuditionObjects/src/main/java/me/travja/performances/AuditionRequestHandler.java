package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public abstract class AuditionRequestHandler implements RequestHandler<Map<String, String>, Map<String, Object>> {

    protected static ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .registerModule(new SimpleModule().addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer()));

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, Context context) {
        String method  = event.getOrDefault("httpMethod", "none");
        String rawPath = event.getOrDefault("path", "");
        System.out.println("Raw Path is: '" + rawPath + "'");
        String[] path = rawPath.trim().isEmpty() ? new String[0] : rawPath.split("/");
        System.out.println("Path is: '" + path + "'");
        try {
            switch (method) {
                case "GET":
                    return mapper.convertValue(handleGet(event, path), Map.class);
                case "POST":
                    return mapper.convertValue(handlePost(event, path), Map.class);
                case "PATCH":
                    return mapper.convertValue(handlePatch(event, path), Map.class);
                case "DELETE":
                    return mapper.convertValue(handleDelete(event, path), Map.class);
            }
        } catch (Exception e) {
            System.out.println("Something went severely wrong..\n" + e.getMessage());
            throw e;
        }

        throw new MethodNotSupportedException(method + " not supported on this endpoint");
    }

    public Object handleGet(Map<String, String> event, String[] path) {
        throw new MethodNotSupportedException("GET not supported on this endpoint");
    }

    public Object handlePost(Map<String, String> event, String[] path) {
        throw new MethodNotSupportedException("POST not supported on this endpoint");
    }

    public Object handlePatch(Map<String, String> event, String[] path) {
        throw new MethodNotSupportedException("PATCH not supported on this endpoint");
    }

    public Map<String, Object> handleDelete(Map<String, String> event, String[] path) {
        throw new MethodNotSupportedException("DEL not supported on this endpoint");
    }

    protected Map<String, Object> constructResponse(Object... data) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < data.length; i++) {
            map.put(String.valueOf(data[i]), data[++i]);
        }

        return map;
    }

}
