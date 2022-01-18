package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PerformanceHandler implements RequestHandler<Map<String, String>, Map<String, Object>> {

    private static DateTimeFormatter format       = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    // This will be offloaded to Dynamo
    private static List<Performance> performances = List.of(new Performance(1, "asdf",
            Collections.emptyList(), Collections.singletonList(new Audition())), new Performance());

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, Context context) {

        String method = event.getOrDefault("httpMethod", "none");
        if(method.equals("GET"))
            return handleGet(event, context);
        else if(method.equals("POST"))
            return handlePost(event, context);

        return Map.of("statusCode", 400, "error", "Invalid HTTP Method.");
    }

    public Map<String, Object> handleGet(Map<String, String> event, Context context) {
        return Map.of("statusCode", 200, "performances", performances);
    }


    public Map<String, Object> handlePost(Map<String, String> event, Context context) {
        String        address = event.get("address");
        ZonedDateTime date    = ZonedDateTime.parse(event.get("date"), format);

        performances.add(new Performance(1, address, Collections.singletonList(date), new ArrayList<>()));

        return Map.of("statusCode", 200, "numPerformances", performances.size());
    }
}
