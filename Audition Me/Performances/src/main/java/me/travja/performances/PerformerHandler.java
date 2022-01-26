package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.models.Performer;
import me.travja.performances.api.models.Person;
import me.travja.performances.processor.LambdaController;

import java.time.format.DateTimeFormatter;
import java.util.Map;

// '/performance' endpoint
@LambdaController("performers")
public class PerformerHandler extends AuditionRequestHandler {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");
    private final  StateManager      state  = StateManager.getInstance();

    @Override
    public Map<String, Object> handleGet(Map<String, Object> event, String[] path, Person authUser) {
        String action = path.length == 0 ? "" : path[0].toLowerCase();

        if (action.equalsIgnoreCase("search")) {
            if (path.length < 2)
                throw new IllegalArgumentException("Missing '/search/{name}'");

            String    name      = path[1];
            Performer performer = state.getPerformerByName(name);
            return constructResponse(200, "performer", performer);
        }

        if (!action.trim().isEmpty())
            try {
                return constructResponse(200, "performer", state.getPerformerById(Long.parseLong(action)));
            } catch (NumberFormatException e) {}

        return constructResponse(200, "performers", state.getPerformers());
    }

//    @Override
//    public Map<String, Object> handlePost(Map<String, Object> event, String[] path) {
//        String action = path.length == 0 ? "create" : path[0].toLowerCase();
//
//        if (action.equals("create")) {
//            ensureExists(event, "venue");
//            ensureExists(event, "title");
//            ensureExists(event, "date");
//
//            String        title = (String) event.get("title");
//            String        venue = (String) event.get("venue");
//            ZonedDateTime date  = ZonedDateTime.parse((String) event.get("date"), format);
//
//            Performance performance = new Performance(title, venue, null, null,
//                    new ArrayList<>(Arrays.asList(date)), new ArrayList<>());
//            state.addPerformance(performance);
//
//            return constructResponse(200,
//                    "performance", performance,
//                    "numPerformances", state.getPerformances().size());
//        } else if (action.equals("cast")) {
//            ensureExists(event, "performanceId");
//            ensureExists(event, "performerId");
//
//            //TODO Get authenticated user as casting director. Ensure he owns the performance
//
//            long performanceId = getLong(event, "performanceId");
//            Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
//                    new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
//            Performer performer = state.getPerformerById(getLong(event, "performerId"));
//
//            performance.cast(performer);
//
//            return constructResponse(200, "message", "Performer cast");
//        }
//
//        throw new IllegalArgumentException("Invalid action");
//    }
//
//    @Override
//    public Map<String, Object> handleDelete(Map<String, Object> event, String[] path) {
//        String id = path[0];
//        ensureNotNull("/{id}", id);
//        //TODO Get authenticated user as director. Ensure he owns the performance
//
//        long performanceId = Long.parseLong(id);
//        Performance performance = state.getPerformanceById(performanceId).orElseThrow(() ->
//                new NoSuchElementException("Performance with ID '" + performanceId + "' doesn't exist"));
//        state.getPerformances().remove(performance);
//        return constructResponse(200, "numPerformances", state.getPerformances().size());
//    }

}
