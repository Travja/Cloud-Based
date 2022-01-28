package me.travja.performances.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.Util;
import me.travja.performances.api.models.Person;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

// '/' endpoint
public class LambdaHandler
        extends AuditionRequestHandler
        implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public static final  String                              PACK     = "me.travja.performances";
    private static final String                              basePath = "api";
    private static final Map<String, AuditionRequestHandler> handlers = new HashMap<>();
    public static        ObjectMapper                        mapper   = Util.getMapper();
    private final        StateManager                        state    = StateManager.getInstance();

    public LambdaHandler() {
        registerEvents(PACK);
    }

    public static void registerEvents(String pkg) {
        Reflections reflections = new Reflections(pkg, new SubTypesScanner(false));

        Set<Class<? extends AuditionRequestHandler>> classes =
                reflections.getSubTypesOf(AuditionRequestHandler.class);

        for (Class<? extends AuditionRequestHandler> clazz : classes) {
            LambdaController annotation = clazz.getAnnotation(LambdaController.class);
            if (annotation == null) continue;

            try {
                AuditionRequestHandler instance = clazz.getDeclaredConstructor().newInstance();
                instance.postConstruct();
                LambdaHandler.registerHandler(annotation.value(), instance);
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static void registerHandler(String path, AuditionRequestHandler handler) {
        path = path.toLowerCase();
        handlers.put(path, handler);
        System.out.println("Registered new handler '" + path + "'");
    }

    private static AuditionRequestHandler getHandler(String handle) {
        System.out.println(handlers);
        return handlers.get(handle);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String rawPath = (String) event.getOrDefault("path", "");
        System.out.println("Raw Path is: '" + rawPath + "'");

        LinkedList<String> path = new LinkedList<>();
        path.addAll(List.of(rawPath.trim().isEmpty() ? new String[0] : rawPath.split("/")));
        if (path.size() > 0 && path.getFirst().trim().isEmpty()) path.removeFirst();
        if (path.size() > 0 && (path.getLast().equalsIgnoreCase("{proxy+}") || path.getLast().equalsIgnoreCase("{proxy}"))) {
            path.removeLast();
            LinkedList<String> proxyPath = new LinkedList<>();
            proxyPath.addAll(List.of(((Map<String, String>) event.get("pathParameters")).get("proxy").split("/")));
            if (proxyPath.size() > 0 && proxyPath.getFirst().trim().isEmpty())
                proxyPath.removeFirst();
            path.addAll(proxyPath);
        }
        if (path.size() > 0 && path.getFirst().trim().equalsIgnoreCase(basePath)) path.removeFirst();

        String handleParam = (path.size() > 0 ? path.removeFirst() : "").toLowerCase();
        System.out.println("Handler Param is " + handleParam);
        AuditionRequestHandler handler = getHandler(handlers.containsKey(handleParam) ? handleParam : "");

        if (handler != null) {
            String authHeader = getAuthHeader(event);
            Person authUser   = authHeader != null ? getAuthUser(authHeader.replace("Basic ", "")) : null;

            try {
                String[] pathArray = path.toArray(new String[0]);
                System.out.println("Path is: '" + pathArray + "'");
                if (event.containsKey("body") && event.get("body") != null) {
                    if ((boolean) event.get("isBase64Encoded"))
                        event.putAll((Map<String, Object>) mapper.readValue(Base64.getDecoder()
                                        .decode((String) event.get("body")),
                                Map.class));
                    else
                        event.putAll((Map<String, Object>) mapper.readValue((String) event.get("body"), Map.class));
                }
                return convert(handler.handleRequest(event, context, authUser, pathArray));
            } catch (IOException ioe) {
                return convert(constructResponse(400, "errorMessage", "Hit IOException when converting body.",
                        "exception", ioe));
            } catch (Exception e) {
                return convert(constructResponse(500, "errorMessage",
                        "Something went severely wrong..    " + e.getMessage(),
                        "exception", e));
            }
        }

        return convert(constructResponse(400, "errorMessage",
                event.getOrDefault("httpMethod", "GET") + " not supported on this endpoint"));
    }

    private Map<String, Object> convert(Map<String, Object> map) {
        return mapper.convertValue(map, Map.class);
    }

    protected String getAuthHeader(Map<String, Object> event) {
        if (event == null || !event.containsKey("headers")) return null;

        Map<String, String> headers = ((Map<String, String>) event.get("headers"));
        if (headers == null || !headers.containsKey("Authorization")) return null;

        return headers.get("Authorization");
    }

    protected Person getAuthUser(String authHeader) {
        String[]         b64      = new String(Base64.getDecoder().decode(authHeader.getBytes())).split(":", 2);
        String           username = b64[0];
        Optional<Person> authUser = state.getByEmail(username);
        return authUser.orElse(null);
    }

}
