package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import me.travja.performances.api.StateManager;

import java.util.*;
import java.util.stream.Collectors;

public class AuditionListConverter implements DynamoDBTypeConverter<String, List<Audition>> {
    private static StateManager state = StateManager.getInstance();

    @Override
    public String convert(List<Audition> auditions) {
        List<String> aud = auditions.stream()
                .map(audition -> audition.getId().toString())
                .collect(Collectors.toList());
        return String.join(";", aud);
    }

    @Override
    public List<Audition> unconvert(String input) {
        if (input.trim().isEmpty())
            return new ArrayList<>();
        List<Audition> auditions = Arrays.stream(input.split(";"))
                .map(str -> state.getAudition(UUID.fromString(str)).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return auditions;
    }
}
