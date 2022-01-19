package me.travja.performances;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Person {

    @Setter(AccessLevel.PRIVATE)
    private long   id;
    private String name, email, phone;

    public Person(long id) {
        this.id = id;
    }

}
