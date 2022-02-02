package me.travja.performances.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import me.travja.performances.api.StateManager;
import me.travja.performances.api.models.Person;

import java.io.IOException;
import java.util.UUID;

public class PersonDeserializer extends StdDeserializer<Person> {

    private StateManager state = StateManager.getInstance();

    public PersonDeserializer() {
        this(null);
    }

    protected PersonDeserializer(Class<Person> t) {
        super(t);
    }

    @Override
    public Person deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String[] split = p.getValueAsString().split(" ");
        String   uid   = split[split.length - 1].replace("(", "").replace(")", "");
        return state.getById(UUID.fromString(uid)).orElse(null);
    }

}
