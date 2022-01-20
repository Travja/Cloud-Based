package me.travja.performances;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class Person {

    @Setter(AccessLevel.PRIVATE)
    private long   id;
    private String name, email, phone;

    public Person(long id) {
        this.id = id;
    }

}
