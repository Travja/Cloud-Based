package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.models.Audition;
import me.travja.performances.api.models.Performance;
import me.travja.performances.api.models.Performer;
import me.travja.performances.api.models.Person;
import me.travja.performances.processor.LambdaController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.NoSuchElementException;

import static me.travja.performances.api.Util.*;

// '/audition' endpoint
@LambdaController("performers")
public class AuditionHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, Object> event, String[] path, Person authUser) {
        if (authUser == null)
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        if (path.length == 0)
            throw new IllegalArgumentException("Missing path information");

        if (path[0].equalsIgnoreCase("status")) {
            if (path.length < 3)
                throw new IllegalArgumentException("Please provide /status/{performanceId}/{performerId");

            if (!(authUser instanceof Performer))
                return constructResponse(403, "message", "You don't have permission for this endpoint");

            long performanceId = Long.parseLong(path[1]);

            Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                    new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
            Performer performer = (Performer) authUser;
            Audition  audition  = performance.getAudition(performer.getId());
            if (audition != null) {
                sendEmail(performer, "Audition Status", audition.getStatus());
                return constructResponse(200,
                        "audition", audition,
                        "message", "Email sent");
            } else {
                return constructResponse(200, "message", "No audition exists for that performer and " +
                        "this performance");
            }
        } else {
            try {
                long id = Long.parseLong(path[0]);
                return constructResponse(200, "auditionList",
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
    public Map<String, Object> handlePost(Map<String, Object> event, String[] path, Person authUser) {
        if (!(authUser instanceof Performer))
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        ensureExists(event, "performanceId");
        ensureExists(event, "performerId");
        ensureExists(event, "date");

        long performanceId = getLong(event, "performanceId");
        long performerId   = getLong(event, "performerId");

        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        Performer     performer = state.getPerformerById(performerId);
        ZonedDateTime date      = ZonedDateTime.parse((String) event.get("date"), format);

        Audition audition = performance.scheduleAudition(performer, date);
        if (performance.getDirector() != null)
            sendEmail(performance.getDirector(), "Someone signed up for an audition!", performer.getId() + " has signed up to " +
                    "audition for performance " + performanceId);
        if (performance.getCastingDirector() != null)
            sendEmail(performance.getCastingDirector(), "Someone signed up for an audition!", performer.getId() +
                    " has signed up to audition for performance " + performanceId);
        return constructResponse(200, "audition", audition,
                "totalAuditions", performance.getAuditionList().size());
    }

    @Override
    public Map<String, Object> handleDelete(Map<String, Object> event, String[] path, Person authUser) {
        if (authUser == null)
            return constructResponse(403, "message", "You don't have permission for this endpoint");
        ensureExists(event, "id");
        ensureExists(event, "auditionId");

        long performanceId = getLong(event, "id");
        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        performance.removeAudition(getLong(event, "auditionId"));
        return constructResponse(200);
    }
}
