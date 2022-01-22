package me.travja.performances;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NoSuchElementException;

import static me.travja.performances.Util.*;

// '/audition' endpoint
public class AuditionHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, String> event, String[] path) {
        if (path.length == 0)
            throw new IllegalArgumentException("Missing path information");

        if (path[0].equalsIgnoreCase("status")) {
            if (path.length < 3)
                throw new IllegalArgumentException("Please provide /status/{performanceId}/{performerId");

            long performanceId = Long.parseLong(path[1]);
            long performerId   = Long.parseLong(path[2]);

            Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                    new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
            Performer performer = state.getPerformerById(performerId);
            Audition  audition  = performance.getAudition(performerId);
            if (audition != null) {
                sendEmail(performer, "Audition Status", audition.getStatus());
                return constructResponse("statusCode", 200,
                        "audition", audition,
                        "message", "Email sent");
            } else {
                return constructResponse("statusCode", 200, "message", "No audition exists for that performer and " +
                        "this performance");
            }
        } else {
            try {
                long id = Long.parseLong(path[0]);
                return constructResponse("statusCode", 200, "auditionList",
                        state.getPerformanceById(id).orElseThrow(() ->
                                        new NoSuchElementException("Performance with ID '" + id + "' doesn't exist"))
                                .getAuditionList());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("To get an audition list, use a proper integer id as the first " +
                        "path variable.");
            }
        }
    }

    @Override
    public Map<String, Object> handlePost(Map<String, String> event, String[] path) {
        ensureExists(event, "performanceId");
        ensureExists(event, "performerId");
        ensureExists(event, "date");

        long performanceId = getLong(event, "performanceId");
        long performerId   = getLong(event, "performerId");

        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        Performer     performer = state.getPerformerById(performerId);
        ZonedDateTime date      = ZonedDateTime.parse(event.get("date"), format);

        Audition audition = performance.scheduleAudition(performer, date);
        sendEmail(performance.getDirector(), "Someone signed up for an audition!", performer.getId() + " has signed up to " +
                "audition for performance " + performanceId);
        sendEmail(performance.getCastingDirector(), "Someone signed up for an audition!", performer.getId() +
                " has signed up to audition for performance " + performanceId);
        return constructResponse("statusCode", 200, "audition", audition,
                "totalAuditions", performance.getAuditionList().size());
    }

    @Override
    public Map<String, Object> handleDelete(Map<String, String> event, String[] path) {
        ensureExists(event, "id");
        ensureExists(event, "auditionId");

        long performanceId = Long.parseLong(event.get("id"));
        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        performance.removeAudition(Long.parseLong(event.get("auditionId")));
        return constructResponse("statusCode", 200);
    }
}
