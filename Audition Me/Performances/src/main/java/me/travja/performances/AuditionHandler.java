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
import java.util.*;

import static me.travja.performances.api.Util.*;

// '/auditions' endpoint
@LambdaController("auditions")
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
            if (path.length < 2)
                throw new IllegalArgumentException("Please provide /status/{performanceId} OR /status/{performanceId}/{performerId}");

            long performanceId = Long.parseLong(path[1]);
            long performerId   = path.length >= 3 ? Long.parseLong(path[2]) : authUser.getId();
            if (authUser instanceof Performer && performerId != authUser.getId())
                return constructResponse(403, "message", "You don't have permission for this endpoint");

            Performer             performer   = state.getPerformerById(performerId);
            Optional<Performance> performance = state.getPerformanceById(performanceId);
            if (performance.isEmpty())
                return constructResponse(404, "errorMessage", "Performance with ID '" + performanceId + "' doesn't exist");

            Audition audition = performance.get().getAudition(performer.getId());
            if (audition != null) {
                List<Object> list = new LinkedList<>();
                list.add("audition");
                list.add(audition);
                if (performerId == authUser.getId()) {
                    sendEmail(performer, "Audition Status", audition.getStatus());
                    list.add("message");
                    list.add("Email sent");
                }
                return constructResponse(200, list.toArray());
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

        ensureExists(event, "performanceId", "performerId", "date");

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
        ensureExists(event, "id", "auditionId");

        long performanceId = getLong(event, "id");
        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        performance.removeAudition(getLong(event, "auditionId"));
        return constructResponse(200);
    }
}
