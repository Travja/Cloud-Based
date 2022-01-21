package me.travja.performances;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class Performance {

    @Setter(AccessLevel.PRIVATE) private long id;
    private static                       long _id = 0;

    private String   title   = "No Title";
    private String   address = "No Address";
    @JsonSerialize(using = PersonSerializer.class)
    private Director director;

    @JsonSerialize(using = PersonSerializer.class)
    private CastingDirector     castingDirector;
    @JsonSerialize(contentUsing = ZonedDateTimeSerializer.class)
    private List<ZonedDateTime> performanceDates = new ArrayList<>();
    private List<Audition>      auditionList     = new ArrayList<>();
    @JsonSerialize(contentUsing = PersonSerializer.class)
    private List<Performer>     performers       = new ArrayList<>();

    public Performance() {
        this.id = _id++;
    }

    public Performance(String title, String address,
                       Director director, CastingDirector castingDirector,
                       List<ZonedDateTime> performanceDates, List<Performer> performers) {
        this.id = _id++;
        this.title = title;
        this.address = address;
        this.director = director;
        this.castingDirector = castingDirector;
        this.performanceDates = performanceDates;
        this.performers = performers;
    }

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
