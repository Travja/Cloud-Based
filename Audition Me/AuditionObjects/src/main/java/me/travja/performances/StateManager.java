package me.travja.performances;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class StateManager {

    @Setter(AccessLevel.PRIVATE)
    private static StateManager instance;

    // This will be offloaded to Dynamo
    private List<Performer>   performers   = new ArrayList<>(
            Arrays.asList(
                    new Performer("Travis Eggett"),
                    new Performer("Chris Cantera") //Hey look, we're performers
            )
    );
    private List<Performance> performances = new ArrayList<>(Arrays.asList(new Performance(
                    "Swan Lake",
                    "111 East St.",
                    new Director(),
                    new CastingDirector(),
                    new ArrayList<>(Arrays.asList(ZonedDateTime.now().plusDays(10))),
                    new ArrayList<>()),
            new Performance()));

    public static StateManager getInstance() {
        if (instance == null) {
            instance = new StateManager();
            instance.setup();
        }
        return instance;
    }

    private void setup() {
        ObjectMapper mapper     = new ObjectMapper();
        SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, "", null, null));
        testModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        mapper.registerModule(testModule);

        instance.getPerformanceById(0).setAuditionList(new ArrayList(
                Arrays.asList(
                        new Audition(instance.getPerformerById(0),
                                ZonedDateTime.now().plusDays(-5)
                        )
                )
        ));
    }

    public boolean hasPerformance(Performance performance) {
        return getPerformanceById(performance.getId()) != null;
    }

    public Performer getPerformerById(long id) {
        return performers.stream()
                .filter(performer -> performer.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Performer with ID '" + id + "' doesn't exist"));
    }

    public Performance getPerformanceById(long id) {
        return performances.stream()
                .filter(performance -> performance.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Performance with ID '" + id + "' doesn't exist"));
    }

    public Performance getPerformanceByName(String title) {
        Pattern regex = Pattern.compile(".*" + title.toLowerCase() + ".*");

        return performances.stream().filter(perf -> {
            Matcher matcher = regex.matcher(perf.getTitle().toLowerCase());
            return matcher.find();
        }).findFirst().orElse(null);
    }

    public void addPerformance(Performance performance) {
        if (!hasPerformance(performance))
            performances.add(performance);
    }

}
