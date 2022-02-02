package me.travja.performances;

import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.models.LambdaRequest;
import me.travja.performances.api.models.Performer;
import me.travja.performances.processor.LambdaController;

import java.util.Map;
import java.util.UUID;

// '/performance' endpoint
@LambdaController("performers")
public class PerformerHandler extends AuditionRequestHandler {

    @Override
    public Map<String, Object> handleGet(LambdaRequest request, String[] path) {
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
                return constructResponse(200, "performer", state.getPerformerById(UUID.fromString(action)));
            } catch (NumberFormatException e) {}

        return constructResponse(200, "performers", state.getPerformers());
    }

}
