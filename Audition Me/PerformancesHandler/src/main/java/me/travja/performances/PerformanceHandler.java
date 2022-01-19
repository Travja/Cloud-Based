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
                    Collections.emptyList(),
                    Collections.singletonList(new Audition(new Performer(1), ZonedDateTime.now().plusDays(10)))),
            new Performance());

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, Context context) {

        String method = event.getOrDefault("httpMethod", "none");
        if (method.equals("GET"))
            return handleGet(event, context);
        else if (method.equals("POST"))
            return handlePost(event, context);
        else if (method.equals("DEL"))
            return handleDelete(event, context);

        return Map.of("statusCode", 400, "error", "Method not supported.");
    }

    public Map<String, Object> handleGet(Map<String, String> event, Context context) {
        if (event.containsKey("id"))
            return Map.of("statusCode", 200, "performance", getPerformanceById(Long.parseLong(event.get("id"))));

        return Map.of("statusCode", 200, "performances", performances);
    }


    public Map<String, Object> handlePost(Map<String, String> event, Context context) {
        ensureExists(event, "address");
        ensureExists(event, "date");

        String        address = event.get("address");
        ZonedDateTime date    = ZonedDateTime.parse(event.get("date"), format);

        performances.add(new Performance(1, address, Collections.singletonList(date), new ArrayList<>()));

        return Map.of("statusCode", 200, "numPerformances", performances.size());
    }

    public Map<String, Object> handleDelete(Map<String, String> event, Context context) {
        ensureExists(event, "id");

        Performance performance = getPerformanceById(Long.parseLong(event.get("id")));
        performances.remove(performance);
        return Map.of("statusCode", 200, "numPerformances", performances.size());
    }

    public Performance getPerformanceById(long id) {
        return performances.stream()
                .filter(performance -> performance.getId() == id)
                .findFirst().orElse(null);
    }

    public void ensureExists(Map<String, String> event, String key) {
        if (!event.containsKey(key))
            throw new RuntimeException("Missing `" + key + "` in request body.");
    }
}
