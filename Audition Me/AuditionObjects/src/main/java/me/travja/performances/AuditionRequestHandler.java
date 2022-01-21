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
        String method = event.getOrDefault("httpMethod", "none");
        if (method.equals("GET"))
            return convertToMap(mapper.convertValue(handleGet(event, context), Map.class));
        else if (method.equals("POST"))
            return convertToMap(mapper.convertValue(handlePost(event, context), Map.class));
        else if (method.equals("DEL"))
            return convertToMap(mapper.convertValue(handleDelete(event, context), Map.class));

        throw new MethodNotSupportedException(method + " not supported on this endpoint");
    }

    public Object handleGet(Map<String, String> event, Context context) {
        throw new MethodNotSupportedException("GET not supported on this endpoint");
    }

    public Object handlePost(Map<String, String> event, Context context) {
        throw new MethodNotSupportedException("POST not supported on this endpoint");
    }

    public Object handlePatch(Map<String, String> event, Context context) {
        throw new MethodNotSupportedException("PATCH not supported on this endpoint");
    }

    public Map<String, Object> handleDelete(Map<String, String> event, Context context) {
        throw new MethodNotSupportedException("DEL not supported on this endpoint");
    }

    private static Map<String, Object> convertToMap(Map<String, Object> map) {
        Map<String, Object> temp = new HashMap<>();
        map.forEach((k, v) -> temp.put(k, v));
        return temp;
    }

}
