package me.travja.performances;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    @Setter(AccessLevel.PRIVATE) private long id;

    private String              address;
    private List<ZonedDateTime> performanceDates;
    private List<Audition>      auditionList;
    private List<Performer>     performers;

    public void cast(Performer performer) {
        if (!performers.contains(performer))
            performers.add(performer);
    }

    public void scheduleAudition(Performer performer, ZonedDateTime date) {
        auditionList.add(new Audition(new Random().nextLong(), performer, date));
    }

    public void removeAudition(long id) {
        Audition aud = auditionList.stream().filter(audition -> audition.getId() == id).findFirst().orElse(null);
        if (aud != null)
            auditionList.remove(aud);
    }

}
