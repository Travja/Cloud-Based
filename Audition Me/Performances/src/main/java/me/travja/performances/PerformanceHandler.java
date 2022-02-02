package me.travja.performances;

import com.fasterxml.jackson.annotation.JsonView;
import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.models.LambdaRequest;
import me.travja.performances.api.models.Performance;
import me.travja.performances.api.models.Performer;
import me.travja.performances.api.models.Person;
import me.travja.performances.api.views.PerformanceView;
import me.travja.performances.processor.LambdaController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static me.travja.performances.api.Util.ensureNotNull;

// '/performance' endpoint
@LambdaController("performances")
public class PerformanceHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

    @Override
    @JsonView(PerformanceView.class)
    public Map<String, Object> handleGet(LambdaRequest request, String[] path) {
        String action = path.length == 0 ? "" : path[0].toLowerCase();

        System.out.println("EVENT");
        request.getData().forEach((key, val) -> System.out.println(key + ": " + val));
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
    public Map<String, Object> handlePost(LambdaRequest request, String[] path) {
        Person authUser = request.getAuthUser();
        if (!(authUser.getType().contains("Director")))
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        String action = path.length == 0 ? "create" : path[0].toLowerCase();

        if (action.equals("create")) {
            request.ensureExists("venue", "title", "date");

            String        title = request.getString("title");
            String        venue = request.getString("venue");
            ZonedDateTime date  = ZonedDateTime.parse(request.getString("date"), format);

            Performance performance = new Performance(title, venue, null, null,
                    new ArrayList<>(Arrays.asList(date)), new ArrayList<>());
            state.save(performance);

            return constructResponse(200,
                    "performance", performance,
                    "numPerformances", state.getPerformances().size());
        } else if (action.equals("cast")) {
            request.ensureExists("performanceId", "performerId");

            UUID                  performanceId = request.getUUID("performanceId");
            Optional<Performance> perf          = state.getPerformanceById(performanceId);
            Performance performance = perf.orElseThrow(() ->
                    new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
            Performer performer = state.getPerformerById(request.getUUID("performerId"));

            performance.cast(performer);
            state.save(performance);
            state.save(performer);

            return constructResponse(200, "message", "Performer cast");
        }

        throw new IllegalArgumentException("Invalid action '" + action + "'");
    }

    @Override
    public Map<String, Object> handleDelete(LambdaRequest request, String[] path) {
        Person authUser = request.getAuthUser();
        if (!(authUser.getType().contains("Director")))
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        String id = path[0];
        ensureNotNull("/{id}", id);

        UUID                  performanceId = UUID.fromString(id);
        Optional<Performance> perf          = state.getPerformanceById(performanceId);
        Performance           performance   = perf.orElse(null);
        if (performance == null)
            return constructResponse(200, "numPerformances", state.getPerformances().size(), "error",
                    "Performance " + performanceId + " does not exist.");
        state.delete(performance);
        return constructResponse(200, "numPerformances", state.getPerformances().size());
    }

}
