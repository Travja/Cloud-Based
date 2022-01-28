package me.travja.performances.api;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import me.travja.performances.api.models.*;
import me.travja.performances.serializers.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class StateManager {

    @Setter(AccessLevel.PRIVATE)
    private static StateManager instance;

    // This will be offloaded to Dynamo
    // TODO Load data from Dynamo
    private List<Performer>       performers       = new ArrayList<>();
    private List<Director>        directors        = new ArrayList<>();
    private List<CastingDirector> castingDirectors = new ArrayList<>();
    private List<Performance>     performances     = new ArrayList<>();

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

        save(new Performer("Travis Eggett", "teggett@student.neumont.edu", "phone number", "test"));
        save(new Performer("Chris Cantera", "ccantera@neumont.edu", "phone number", "test2")); //Hey look, we're performers
        save(new Director("Steven Spielberg", "spiel@berg.com", "123-456-7890", "test3"));

        performances.add(
                new Performance(
                        "Swan Lake",
                        "111 East St.",
                        (Director) getByEmail("spiel@berg.com").get(),
                        save(new CastingDirector()),
                        new ArrayList<>(Arrays.asList(ZonedDateTime.now().plusDays(10))),
                        new ArrayList<>()
                )
        );
        performances.add(new Performance());

        instance.getPerformanceById(0).orElseThrow(() ->
                        new NoSuchElementException("Performance with ID '0' doesn't exist"))
                .setAuditionList(new ArrayList(
                        Arrays.asList(
                                new Audition(instance.getPerformerById(0),
                                        ZonedDateTime.now().plusDays(-5)
                                )
                        )
                ));
    }

    public <T extends Person> T save(T person) {
        if (person instanceof CastingDirector) {
            castingDirectors.add((CastingDirector) person);
        } else if (person instanceof Director) {
            directors.add((Director) person);
        } else if (person instanceof Performer) {
            performers.add((Performer) person);
        }

        //TODO Save data off to Dynamo

        return person;
    }

    public boolean hasPerformance(Performance performance) {
        return getPerformanceById(performance.getId()).isPresent();
    }

    public Performer getPerformerById(long id) {
        return performers.stream()
                .filter(performer -> performer.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Performer with ID '" + id + "' doesn't exist"));
    }

    public Performer getPerformerByName(String name) {
        Pattern regex = Pattern.compile(".*" + name.toLowerCase() + ".*");

        return performers.stream().filter(perf -> {
            Matcher matcher = regex.matcher(perf.getName().toLowerCase());
            return matcher.find();
        }).findFirst().orElse(null);
    }

    public Optional<Performance> getPerformanceById(long id) {
        return performances.stream()
                .filter(performance -> performance.getId() == id)
                .findFirst();
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

    public Optional<Person> getByEmail(String email) {
        List<Person> allPeople = new ArrayList<>();
        allPeople.addAll(getPerformers());
        allPeople.addAll(getDirectors());
        allPeople.addAll(getCastingDirectors());

        return allPeople.stream()
                .filter(person -> {
                    System.out.println(person.getEmail());
                    return person.getEmail().equalsIgnoreCase(email);
                })
                .findFirst();
    }

}
