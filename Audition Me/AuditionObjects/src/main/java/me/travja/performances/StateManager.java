package me.travja.performances;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class StateManager {

    // This will be offloaded to Dynamo
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static Performer       perf1      = new Performer(1);

    private List<Performer> performers = List.of(perf1, new Performer(2));
    private List<Performance> performances = List.of(new Performance(1, "asdf",
                    Collections.emptyList(),
                    Collections.singletonList(new Audition(perf1, ZonedDateTime.now().plusDays(10)))),
            new Performance());

    public Performer getPerformerById(long id) {
        return performers.stream()
                .filter(performer -> performer.getId() == id)
                .findFirst().orElse(null);
    }

    public Performance getPerformanceById(long id) {
        return performances.stream()
                .filter(performance -> performance.getId() == id)
                .findFirst().orElse(null);
    }

}
