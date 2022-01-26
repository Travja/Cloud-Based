package me.travja.performances.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performer extends Person {

    private List<Performance> currentPerformances = new ArrayList<>();
    private List<Performance> pastPerformances    = new ArrayList<>();

    public Performer(String name) {
        this(name, "example@example.com", null);
    }

    public Performer(String name, String email, String phone) {
        super(name, email, phone);
    }

    public void addCurrentPerformance(Performance performance) {
        currentPerformances.add(performance);
    }

    public void addPastPerformance(Performance performance) {
        currentPerformances.remove(performance);
        pastPerformances.add(performance);
    }

}
