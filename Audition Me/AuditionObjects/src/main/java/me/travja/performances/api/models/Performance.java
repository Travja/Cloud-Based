package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import me.travja.performances.api.Util;
import me.travja.performances.serializers.PersonSerializer;
import me.travja.performances.serializers.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@DynamoDBTable(tableName = "Performances")
public class Performance {

    private static long              _id    = 0;
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O");

    @Setter(AccessLevel.PRIVATE)
    @DynamoDBHashKey(attributeName = "id")
    private long                id               = _id++;
    @DynamoDBAttribute(attributeName = "title")
    private String              title            = "No Title";
    @DynamoDBAttribute(attributeName = "venue")
    private String              venue            = "No Address";
    @DynamoDBAttribute(attributeName = "director")
    @JsonSerialize(using = PersonSerializer.class)
    private Director            director;
    @DynamoDBAttribute(attributeName = "castingDirector")
    @JsonSerialize(using = PersonSerializer.class)
    private CastingDirector     castingDirector;
    @DynamoDBAttribute(attributeName = "performanceDates")
    @JsonSerialize(contentUsing = ZonedDateTimeSerializer.class)
    private List<ZonedDateTime> performanceDates = new ArrayList<>();
    @DynamoDBAttribute(attributeName = "auditionList")
    private List<Audition>      auditionList     = new ArrayList<>();
    @DynamoDBAttribute(attributeName = "cast")
    @JsonSerialize(contentUsing = PersonSerializer.class)
    private List<Performer>     cast             = new ArrayList<>();

    public Performance() {
        this.id = _id++;
    }

    public Performance(String title, String venue,
                       Director director, CastingDirector castingDirector,
                       List<ZonedDateTime> performanceDates, List<Performer> cast) {
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
            performer.addCurrentPerformance(this);
            Util.sendEmail(performer, "You have been cast!",
                    "Congratulations! You have been cast in " +
                            getTitle() + "! We look forward to having you!");
        }
    }

    public Audition getAudition(Performer performer) {
        return getAudition(performer.getId());
    }

    public Audition getAudition(UUID performerId) {
        return auditionList.stream().filter(audition -> audition.getPerformer().getId().equals(performerId))
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

}
