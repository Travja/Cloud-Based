package me.travja.performances;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Person {

    @Setter(AccessLevel.PRIVATE)
    private        long   id;
    private static long   _id = 0;
    private        String name, email, phone;

    public Person() {
        this.id = _id++;
    }

}
