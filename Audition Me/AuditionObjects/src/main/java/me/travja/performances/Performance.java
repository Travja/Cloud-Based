package me.travja.performances;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
public class Performance {

    @Setter(AccessLevel.PRIVATE) private long id;
    private static                       long _id = 0;

    private String title = "No Title";
    private String venue = "No Address";

    @JsonSerialize(using = PersonSerializer.class)
    private Director            director;
    @JsonSerialize(using = PersonSerializer.class)
    private CastingDirector     castingDirector;
    @JsonSerialize(contentUsing = ZonedDateTimeSerializer.class)
    private List<ZonedDateTime> performanceDates = new ArrayList<>();
    private List<Audition>      auditionList     = new ArrayList<>();
    @JsonSerialize(contentUsing = PersonSerializer.class)
    private List<Performer>     cast             = new ArrayList<>();

    public Performance() {
        this.id = _id++;
    }

    public Performance(String title, String venue,
                       Director director, CastingDirector castingDirector,
                       List<ZonedDateTime> performanceDates, List<Performer> cast) {
        this.id = _id++;
        this.title = title;
        this.venue = venue;
        this.director = director;
        this.castingDirector = castingDirector;
        this.performanceDates = performanceDates;
        this.cast = cast;
    }

    public boolean ownsPerformance(Director director) {
        return this.director.equals(director) || castingDirector.equals(director);
    }

    public void cast(Performer performer) {
        Audition audition      = getAudition(performer);
        boolean  hasAuditioned = audition != null && audition.getDate().isBefore(ZonedDateTime.now());
        if (!hasAuditioned)
            throw new IllegalArgumentException("That performer has not auditioned for this performance.");

        audition.setStatus("Cast");

        if (!cast.contains(performer)) {
            cast.add(performer);
            Util.sendEmail(performer, "You have been cast!",
                    "Congratulations! You have been cast in " +
                            getTitle() + "! We look forward to having you!");
        }
    }

    public Audition getAudition(Performer performer) {
        return getAudition(performer.getId());
    }

    public Audition getAudition(long performerId) {
        return auditionList.stream().filter(audition -> audition.getPerformer().getId() == performerId)
                .findFirst().orElse(null);
    }

    public Audition scheduleAudition(Performer performer, ZonedDateTime date) {
        if (isInAuditionList(performer))
            throw new IllegalArgumentException("That performer has already scheduled an audition for this performance.");

        Audition audition = new Audition(performer, date);
        auditionList.add(audition);
        return audition;
    }

    public boolean isInAuditionList(Performer performer) {
        return auditionList.stream()
                .filter(audition -> audition.getPerformer().getId() == performer.getId())
                .findAny().isPresent();
    }

    public void removeAudition(long id) {
        Audition aud = auditionList.stream().filter(audition -> audition.getId() == id).findFirst().orElse(null);
        if (aud != null)
            auditionList.remove(aud);
    }

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

}
