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

}
