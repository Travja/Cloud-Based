package me.travja.performances;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
public class Person {

    private static long   _id = 0;
    @Setter(AccessLevel.PRIVATE)
    private final  long   id  = _id++;
    private final  String name;
    private        String email, phone;

    protected Person() {
        this.name = "John Doe";
    }

}
