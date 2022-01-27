package me.travja.performances.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import me.travja.performances.api.Util;

@Data
public class Person {

    private static long   _id = 0;
    @Setter(AccessLevel.PRIVATE)
    private final  long   id  = _id++;
    private final  String name;
    private        String email, phone;
    @JsonIgnore
    private String password;

    protected Person() {
        this("John Doe", "example@example.com", null, "password");
    }

    public Person(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = Util.hash(password);
    }
}
