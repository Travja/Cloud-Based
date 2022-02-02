package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.models.*;
import me.travja.performances.processor.LambdaController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static me.travja.performances.api.Util.sendEmail;

// '/auditions' endpoint
@LambdaController("auditions")
public class AuditionHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

    @Override
    public Map<String, Object> handleGet(LambdaRequest request, String[] path) {
        Person authUser = request.getAuthUser();
        if (authUser == null)
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        if (path.length == 0)
            throw new IllegalArgumentException("Missing path information");

        if (path[0].equalsIgnoreCase("status")) {
            if (path.length < 2)
                throw new IllegalArgumentException("Please provide /status/{performanceId} OR /status/{performanceId}/{performerId}");

            UUID performanceId = UUID.fromString(path[1]);
            UUID performerId   = path.length >= 3 ? UUID.fromString(path[2]) : authUser.getId();
            if (authUser.getType().equals("Performer") && !performerId.equals(authUser.getId()))
                return constructResponse(403, "message", "You don't have permission for this endpoint");

            Performer             performer   = state.getPerformerById(performerId);
            Optional<Performance> performance = state.getPerformanceById(performanceId);
            if (performance.isEmpty())
                return constructResponse(404, "errorMessage", "Performance with ID '" + performanceId + "' doesn't exist");

            Audition audition = performance.get().getAudition(performer);
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
                UUID                  id   = UUID.fromString(path[0]);
                Optional<Performance> perf = state.getPerformanceById(id);
                return constructResponse(200, "auditionList",
                        perf.orElseThrow(() ->
                                        new NoSuchElementException("Performance with ID '" + id + "' doesn't exist"))
                                .getAuditionList());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("To get an audition list, use a proper integer id as the first " +
                        "path variable.");
            }
        }
    }

    @Override
    public Map<String, Object> handlePost(LambdaRequest request, String[] path) {
        Person authUser = request.getAuthUser();
        if (!authUser.getType().equalsIgnoreCase("Performer"))
            return constructResponse(403, "message", "You don't have permission for this endpoint");

        request.ensureExists("performanceId", "performerId", "date");

        UUID performanceId = request.getUUID("performanceId");
        UUID performerId   = request.getUUID("performerId");

        Optional<Performance> perf = state.getPerformanceById(performanceId);
        Performance performance = perf.orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        Performer     performer = state.getPerformerById(performerId);
        ZonedDateTime date      = ZonedDateTime.parse(request.getString("date"), format);

        Audition audition = performance.scheduleAudition(performer, date);
        state.save(performance);
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
    public Map<String, Object> handleDelete(LambdaRequest request, String[] path) {
        Person authUser = request.getAuthUser();
        if (authUser == null)
            return constructResponse(403, "message", "You don't have permission for this endpoint");
        request.ensureExists("id", "auditionId");

        UUID performanceId = request.getUUID("id");
        UUID auditionId    = request.getUUID("auditionId");

        Optional<Performance> perf = state.getPerformanceById(performanceId);
        Performance performance = perf.orElseThrow(() ->
                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
        state.deleteAudition(auditionId);
        performance.removeAudition(auditionId);
        state.save(performance);
        return constructResponse(200);
    }
}
