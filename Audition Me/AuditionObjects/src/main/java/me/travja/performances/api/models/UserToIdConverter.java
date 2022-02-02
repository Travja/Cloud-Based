package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import me.travja.performances.api.StateManager;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class UserToIdConverter implements DynamoDBTypeConverter<String, Person> {
    private static StateManager      state  = StateManager.getInstance();

    @Override
    public String convert(Person person) {
        return person.getId().toString();
    }

    @Override
    public Person unconvert(String id) {
        return state.getById(UUID.fromString(id)).orElse(null);
    }
}
