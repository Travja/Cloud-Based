package me.travja.performances;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static me.travja.performances.Util.ensureExists;

// '/performance' endpoint
public class PerformanceHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, String> event, Context context) {
        if (event.containsKey("id"))
            return Map.of("statusCode", 200, "performance", state.getPerformanceById(Long.parseLong(event.get("id"))));

        if (event.containsKey("action")) {
            String action = event.get("action");
            if (action.equals("SEARCH")) {
                String      title       = event.get("title");
                Performance performance = state.getPerformanceByName(title);
                return Map.of("statusCode", 200, "performance", performance);
            }
        }

        return Map.of("statusCode", 200, "performances", state.getPerformances());
    }

    @Override
    public Map<String, Object> handlePost(Map<String, String> event, Context context) {
        ensureExists(event, "action");
        String action = event.get("action");

        if (action.equals("CREATE")) {
            ensureExists(event, "address");
            ensureExists(event, "title");
            ensureExists(event, "date");

            String        title   = event.get("title");
            String        address = event.get("address");
            ZonedDateTime date    = ZonedDateTime.parse(event.get("date"), format);

            state.getPerformances().add(new Performance(title, address, null, null,
                    new ArrayList<>(Arrays.asList(date)), new ArrayList<>()));

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

    @Override
    public Map<String, Object> handleDelete(Map<String, String> event, Context context) {
        ensureExists(event, "id");

        Performance performance = state.getPerformanceById(Long.parseLong(event.get("id")));
        state.getPerformances().remove(performance);
        return Map.of("statusCode", 200, "numPerformances", state.getPerformances().size());
    }

}
