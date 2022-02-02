package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import me.travja.performances.api.StateManager;

import java.util.*;
import java.util.stream.Collectors;

public class CastListConverter implements DynamoDBTypeConverter<String, List<Performer>> {
    private static StateManager state = StateManager.getInstance();

    @Override
    public String convert(List<Performer> cast) {
        List<String> list = cast.stream()
                .map(audition -> audition.getId().toString())
                .collect(Collectors.toList());
        return String.join(";", list);
    }

    @Override
    public List<Performer> unconvert(String input) {
        if (input.trim().isEmpty())
            return new ArrayList<>();

        return new LazyList<>() {
            @Override
            public void load() {
                List<Performer> cast = Arrays.stream(input.split(";"))
                        .map(str -> {
                            System.out.println("Input str is '" + str + "'");
                            String[] split = str.split(" ");
                            UUID     id    = UUID.fromString(split[split.length - 1]);
                            System.out.println("Getting performer " + id);
                            Performer perf = state.getPerformerById(id);
                            return perf;
                        })
                        .distinct()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                this.addAll(cast);
            }
        };

    }
}
