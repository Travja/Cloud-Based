package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static me.travja.performances.Util.ensureExists;
import static me.travja.performances.Util.getLong;

// '/audition' endpoint
public class AuditionHandler implements RequestHandler<Map<String, String>, Map<String, Object>> {

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
        ensureExists(event, "id");
        return Map.of("statusCode", 200, "auditionList",
                state.getPerformanceById(Long.parseLong(event.get("id"))).getAuditionList());
    }


    public Map<String, Object> handlePost(Map<String, String> event, Context context) {
        ensureExists(event, "performanceId");
        ensureExists(event, "performerId");

        long performanceId = getLong(event, "performanceId");
        long performerId   = getLong(event, "performerId");

        Performance   performance = state.getPerformanceById(performanceId);
        Performer     performer   = state.getPerformerById(performerId);
        ZonedDateTime date        = ZonedDateTime.parse(event.get("date"), format);

        performance.scheduleAudition(performer, date);
        return Map.of("statusCode", 200, "totalAuditions", performance.getAuditionList().size());
    }

    public Map<String, Object> handleDelete(Map<String, String> event, Context context) {
        ensureExists(event, "id");
        ensureExists(event, "auditionId");

        Performance performance = state.getPerformanceById(Long.parseLong(event.get("id")));
        performance.removeAudition(Long.parseLong(event.get("auditionId")));
        return Map.of("statusCode", 200);
    }
}
