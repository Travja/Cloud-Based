package me.travja.performances.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import me.travja.performances.api.models.Person;

import java.io.IOException;

public class PersonSerializer extends StdSerializer<Person> {


    public PersonSerializer() {
        this(null);
    }

    protected PersonSerializer(Class<Person> t) {
        super(t);
    }

    @Override
    public void serialize(Person person, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(person.getName() + " (" + person.getId() + ")");
    }

}
