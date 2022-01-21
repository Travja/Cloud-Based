package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static me.travja.performances.Util.*;

// '/audition' endpoint
public class AuditionHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, String> event, Context context) {
        ensureExists(event, "id");
        return Map.of("statusCode", 200, "auditionList",
                state.getPerformanceById(Long.parseLong(event.get("id"))).getAuditionList());
    }

    @Override
    public Map<String, Object> handlePost(Map<String, String> event, Context context) {
        ensureExists(event, "performanceId");
        ensureExists(event, "performerId");
        ensureExists(event, "date");

        long performanceId = getLong(event, "performanceId");
        long performerId   = getLong(event, "performerId");

        Performance   performance = state.getPerformanceById(performanceId);
        Performer     performer   = state.getPerformerById(performerId);
        ZonedDateTime date        = ZonedDateTime.parse(event.get("date"), format);

        performance.scheduleAudition(performer, date);
        sendEmail("tjeggett@yahoo.com", "Someone signed up for an audition!", performer.getId() + " has signed up to " +
                "audition for performance " + performanceId);
        return Map.of("statusCode", 200, "totalAuditions", performance.getAuditionList().size());
    }

    @Override
    public Map<String, Object> handleDelete(Map<String, String> event, Context context) {
        ensureExists(event, "id");
        ensureExists(event, "auditionId");

        Performance performance = state.getPerformanceById(Long.parseLong(event.get("id")));
        performance.removeAudition(Long.parseLong(event.get("auditionId")));
        return Map.of("statusCode", 200);
    }
}
