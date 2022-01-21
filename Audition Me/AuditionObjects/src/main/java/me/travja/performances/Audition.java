package me.travja.performances;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    @JsonSerialize(using = PersonSerializer.class)
    private Performer     performer;
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    private ZonedDateTime date;

}
