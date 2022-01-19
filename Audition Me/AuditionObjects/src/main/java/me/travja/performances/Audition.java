package me.travja.performances;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class Audition {

    private Performer     performer;
    private ZonedDateTime date;

}
