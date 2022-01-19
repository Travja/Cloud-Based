package me.travja.performances;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class Audition {

    @Setter(AccessLevel.PRIVATE)
    private long          id;
    private Performer     performer;
    private ZonedDateTime date;

}
