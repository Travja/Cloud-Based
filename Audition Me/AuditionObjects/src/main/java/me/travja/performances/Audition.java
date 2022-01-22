package me.travja.performances;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class Audition {

    @Setter(AccessLevel.PRIVATE)
    private        long          id;
    private static long          _id = 0;
    @JsonSerialize(using = PersonSerializer.class)
    private        Performer     performer;
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    private        ZonedDateTime date;

    private String status = "Waiting";

    public Audition(Performer performer, ZonedDateTime date) {
        this.id = _id++;
        this.performer = performer;
        this.date = date;
    }

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

}
