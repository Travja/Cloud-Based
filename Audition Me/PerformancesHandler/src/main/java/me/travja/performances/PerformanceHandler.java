package me.travja.performances;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static me.travja.performances.Util.ensureExists;
import static me.travja.performances.Util.ensureNotNull;

// '/performance' endpoint
public class PerformanceHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, String> event, String[] path) {
        String action = path.length == 0 ? "" : path[0].toLowerCase();

        if (action.equalsIgnoreCase("search")) {
            if (path.length < 2)
                throw new IllegalArgumentException("Missing '/search/{title}'");

            String      title       = path[1];
            Performance performance = state.getPerformanceByName(title);
            return constructResponse("statusCode", 200, "performance", performance);
        } else if (action.equalsIgnoreCase("performer")) {
            if (path.length == 1)
                return constructResponse("statusCode", 200, "performers", state.getPerformers());
            else
                return constructResponse("statusCode", 200, "performer", state.getPerformerById(Long.parseLong(path[1])));
        }

        if (!action.trim().isEmpty())
            try {
                return constructResponse("statusCode", 200, "performance", state.getPerformanceById(Long.parseLong(action)));
            } catch (NumberFormatException e) {}

        return constructResponse("statusCode", 200, "performances", state.getPerformances().toArray());
    }

    @Override
    public Map<String, Object> handlePost(Map<String, String> event, String[] path) {
        String action = path.length == 0 ? "create" : path[0].toLowerCase();

        if (action.equals("create")) {
            ensureExists(event, "venue");
            ensureExists(event, "title");
            ensureExists(event, "date");

            String        title = event.get("title");
            String        venue = event.get("venue");
            ZonedDateTime date  = ZonedDateTime.parse(event.get("date"), format);

            Performance performance = new Performance(title, venue, null, null,
                    new ArrayList<>(Arrays.asList(date)), new ArrayList<>());
            state.addPerformance(performance);

            return constructResponse("statusCode", 200,
                    "performance", performance,
                    "numPerformances", state.getPerformances().size());
        } else if (action.equals("cast")) {
            ensureExists(event, "performanceId");
            ensureExists(event, "performerId");

            //TODO Get authenticated user as casting director. Ensure he owns the performance

            Performance performance = state.getPerformanceById(Long.parseLong(event.get("performanceId")));
            Performer   performer   = state.getPerformerById(Long.parseLong(event.get("performerId")));

            performance.cast(performer);

            return constructResponse("statusCode", 200, "message", "Performer cast");
        }

        throw new IllegalArgumentException("Invalid action");
    }

    @Override
    public Map<String, Object> handleDelete(Map<String, String> event, String[] path) {
        String id = path[0];
        ensureNotNull("/{id}", id);
        //TODO Get authenticated user as director. Ensure he owns the performance

        Performance performance = state.getPerformanceById(Long.parseLong(event.get("id")));
        state.getPerformances().remove(performance);
        return constructResponse("statusCode", 200, "numPerformances", state.getPerformances().size());
    }

}
