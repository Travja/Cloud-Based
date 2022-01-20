package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import static me.travja.performances.Util.ensureExists;

public class PerformanceHandler implements RequestHandler<Map<String, String>, Map<String, Object>> {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = new StateManager();

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
            return Map.of("statusCode", 200, "performance", state.getPerformanceById(Long.parseLong(event.get("id"))));

        return Map.of("statusCode", 200, "performances", state.getPerformances());
    }


    public Map<String, Object> handlePost(Map<String, String> event, Context context) {
        ensureExists(event, "action");
        String action = event.get("action");

        if (action.equals("CREATE")) {
            ensureExists(event, "address");
            ensureExists(event, "date");

            String        address = event.get("address");
            ZonedDateTime date    = ZonedDateTime.parse(event.get("date"), format);

            state.getPerformances().add(new Performance(1, address, Collections.singletonList(date),
                    Collections.emptyList(), Collections.emptyList()));

            return Map.of("statusCode", 200, "numPerformances", state.getPerformances().size());
        } else if (action.equals("CAST")) {
            ensureExists(event, "performanceId");
            ensureExists(event, "performerId");

            Performance performance = state.getPerformanceById(Long.parseLong(event.get("performanceId")));
            Performer   performer   = state.getPerformerById(Long.parseLong(event.get("performerId")));

            performance.cast(performer);

            return Map.of("statusCode", 200, "message", "Performer cast");
        }

        throw new IllegalArgumentException("Invalid action");
    }

    public Map<String, Object> handleDelete(Map<String, String> event, Context context) {
        ensureExists(event, "id");

        Performance performance = state.getPerformanceById(Long.parseLong(event.get("id")));
        state.getPerformances().remove(performance);
        return Map.of("statusCode", 200, "numPerformances", state.getPerformances().size());
    }

}
