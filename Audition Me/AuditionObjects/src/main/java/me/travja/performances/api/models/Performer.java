package me.travja.performances.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@DynamoDBDocument
@NoArgsConstructor
@AllArgsConstructor
public class Performer extends Person {

    @DynamoDBAttribute(attributeName = "currentPerformances")
    private List<Performance> currentPerformances = new ArrayList<>();
    @DynamoDBAttribute(attributeName = "pastPerformances")
    private List<Performance> pastPerformances    = new ArrayList<>();

    public Performer(String name) {
        this(name, "example@example.com", null, "password");
    }

    public Performer(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public Performer(UUID id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }

    public void addCurrentPerformance(Performance performance) {
        currentPerformances.add(performance);
    }

    public void addPastPerformance(Performance performance) {
        currentPerformances.remove(performance);
        pastPerformances.add(performance);
    }

}
