package me.travja.performances.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.travja.performances.MethodNotSupportedException;
import me.travja.performances.api.AuditionRequestHandler;
import me.travja.performances.api.Util;
import me.travja.performances.api.models.Person;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

// '/' endpoint
public class LambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public static final  String                              PACK     = "me.travja.performances";
    private static final String                              basePath = "api";
    private static final Map<String, AuditionRequestHandler> handlers = new HashMap<>();
    public static        ObjectMapper                        mapper   = Util.getMapper();

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
        if (path.getFirst().trim().isEmpty()) path.removeFirst();
        if (path.getLast().equalsIgnoreCase("{proxy+}")) path.removeLast();
        if (path.getFirst().trim().equalsIgnoreCase(basePath)) path.removeFirst();

        String handleParam = (path.size() > 0 ? path.getFirst() : "base").toLowerCase();
        System.out.println("Handler Param is " + handleParam);
        AuditionRequestHandler handler = getHandler(handleParam);

        if (handler != null) {
            String authHeader = getAuthHeader(event);
            Person authUser   = getAuthUser(authHeader);

            try {
                String[] pathArray = path.toArray(new String[0]);
                System.out.println("Path is: '" + pathArray + "'");
                return mapper.convertValue(handler.handleRequest(event, context, authUser, pathArray), Map.class);
            } catch (Exception e) {
                System.out.println("Something went severely wrong..\n" + e.getMessage());
                throw e;
            }
        }

        throw new MethodNotSupportedException(event.getOrDefault("httpMethod", "GET")
                + " not supported on this endpoint");
    }

    protected String getAuthHeader(Map<String, Object> event) {
        if (event.containsKey("headers") && ((Map<String, String>) event.get("headers")).containsKey("Authorization"))
            return ((Map<String, String>) event.get("headers")).get("Authorization");

        return null;
    }

    protected Person getAuthUser(String authHeader) {
        //TODO Get valid user
        return null;
    }

}
