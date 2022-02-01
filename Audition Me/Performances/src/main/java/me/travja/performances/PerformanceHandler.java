package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.models.*;
import me.travja.performances.processor.LambdaController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static me.travja.performances.api.Util.ensureExists;
import static me.travja.performances.api.Util.ensureNotNull;

// '/performance' endpoint
@LambdaController("performances")
public class PerformanceHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, Object> event, String[] path, Person authUser) {
        String action = path.length == 0 ? "" : path[0].toLowerCase();

        System.out.println("EVENT");
        event.forEach((key, val) -> System.out.println(key + ": " + val));
        System.out.println();

        if (action.equalsIgnoreCase("search")) {
            if (path.length < 2)
                throw new IllegalArgumentException("Missing '/search/{title}'");

            String      title       = path[1];
            Performance performance = state.getPerformanceByName(title).orElse(null);
            return constructResponse(200, "performance", performance);
        }

        if (!action.trim().isEmpty())
            try {
                return constructResponse(200, "performance",
                        state.getPerformanceById(UUID.fromString(action)).orElse(null));
            } catch (NumberFormatException e) {}

        return constructResponse(200, "performances", state.getPerformances().toArray());
    }

    @Override
    public Map<String, Object> handlePost(Map<String, Object> event, String[] path, Person authUser) {
        if (!(authUser instanceof Director || authUser instanceof CastingDirector))
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        String action = path.length == 0 ? "create" : path[0].toLowerCase();

        if (action.equals("create")) {
            ensureExists(event, "venue", "title", "date");

            String        title = (String) event.get("title");
            String        venue = (String) event.get("venue");
            ZonedDateTime date  = ZonedDateTime.parse((String) event.get("date"), format);

            Performance performance = new Performance(title, venue, null, null,
                    new ArrayList<>(Arrays.asList(date)), new ArrayList<>());
            state.addPerformance(performance);

            return constructResponse(200,
                    "performance", performance,
                    "numPerformances", state.getPerformances().size());
        } else if (action.equals("cast")) {
            ensureExists(event, "performanceId", "performerId");

            UUID                  performanceId = UUID.fromString(String.valueOf(event.get("performanceId")));
            Optional<Performance> perf          = state.getPerformanceById(performanceId);
            Performance performance = perf.orElseThrow(() ->
                    new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
            Performer performer = state.getPerformerById(UUID.fromString(String.valueOf(event.get("performerId"))));

            performance.cast(performer);

            return constructResponse(200, "message", "Performer cast");
        }

        throw new IllegalArgumentException("Invalid action '" + action + "'");
    }

    @Override
    public Map<String, Object> handleDelete(Map<String, Object> event, String[] path, Person authUser) {
        if (!(authUser instanceof Director || authUser instanceof CastingDirector))
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        String id = path[0];
        ensureNotNull("/{id}", id);

        UUID                  performanceId = UUID.fromString(id);
        Optional<Performance> perf          = state.getPerformanceById(performanceId);
        Performance           performance   = perf.orElse(null);
        if (performance == null)
            return constructResponse(200, "numPerformances", state.getPerformances().size(), "error",
                    "Performance " + performanceId + " does not exist.");
        state.getPerformances().remove(performance);
        return constructResponse(200, "numPerformances", state.getPerformances().size());
    }

}
