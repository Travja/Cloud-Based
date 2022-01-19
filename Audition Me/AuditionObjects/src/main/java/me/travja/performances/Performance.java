package me.travja.performances;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    @Setter(AccessLevel.PRIVATE) private long id;

    private String              address;
    private List<ZonedDateTime> performanceDates;
    private List<Audition>      auditionList;

    public void scheduleAudition(Performer performer, ZonedDateTime date) {
        auditionList.add(new Audition(performer, date));
    }

}
