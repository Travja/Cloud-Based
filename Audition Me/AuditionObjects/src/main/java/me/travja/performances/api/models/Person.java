package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import me.travja.performances.api.Util;

import java.util.List;
import java.util.Map;

@Data
public class Person {

    private static long   _id   = 0;
    private final  String name;
    @Setter(AccessLevel.PRIVATE)
    private        long   id    = _id++;
    private        String email = "", phone = "";
    @JsonIgnore
    private String password;

    protected Person() {
        this("John Doe", "example@example.com", "", "password");
    }

    public Person(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = Util.hash(password);
    }

    protected Person(long id, String name, String email, String phone, String password) {
        if (id >= _id)
            _id = id + 1;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public static Person deserialize(Item item) {
        String name     = item.getString("name");
        String email    = item.getString("email");
        String phone    = item.getString("phone");
        String password = item.getString("password");
        long   id       = item.getLong("id");

        String type = item.getString("type");

        if (type.equals("Performer")) {
            List<Map<String, Object>> current  = item.getList("currentPerformances");
            List<Map<String, Object>> previous = item.getList("pastPerformances");

            return new Performer(id, name, email, phone, password);
        } else if (type.equals("Director"))
            return new Director(id, name, email, phone, password);
        else if (type.equalsIgnoreCase("CastingDirector"))
            return new CastingDirector(id, name, email, phone, password);
        else
            return new Person(id, name, email, phone, password);
    }
}
