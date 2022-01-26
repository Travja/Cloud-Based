package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.models.Performance;
import me.travja.performances.api.models.Performer;
import me.travja.performances.api.models.Person;
import me.travja.performances.processor.LambdaController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import static me.travja.performances.api.Util.*;

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
            Performance performance = state.getPerformanceByName(title);
            return constructResponse(200, "performance", performance);
        }

        if (!action.trim().isEmpty())
            try {
                return constructResponse(200, "performance",
                        state.getPerformanceById(Long.parseLong(action)).orElse(null));
            } catch (NumberFormatException e) {}

        return constructResponse(200, "performances", state.getPerformances().toArray());
    }

    @Override
    public Map<String, Object> handlePost(Map<String, Object> event, String[] path, Person authUser) {
        String action = path.length == 0 ? "create" : path[0].toLowerCase();

        if (action.equals("create")) {
            ensureExists(event, "venue");
            ensureExists(event, "title");
            ensureExists(event, "date");

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
            ensureExists(event, "performanceId");
            ensureExists(event, "performerId");

            //TODO Get authenticated user as casting director. Ensure he owns the performance

            long performanceId = getLong(event, "performanceId");
            Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                    new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
            Performer performer = state.getPerformerById(getLong(event, "performerId"));

            performance.cast(performer);

            return constructResponse(200, "message", "Performer cast");
        }

        throw new IllegalArgumentException("Invalid action");
    }

    @Override
    public Map<String, Object> handleDelete(Map<String, Object> event, String[] path, Person authUser) {
        String id = path[0];
        ensureNotNull("/{id}", id);
        //TODO Get authenticated user as director. Ensure he owns the performance

        long performanceId = Long.parseLong(id);
        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        state.getPerformances().remove(performance);
        return constructResponse(200, "numPerformances", state.getPerformances().size());
    }

}
