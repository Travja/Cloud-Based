package me.travja.performances.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.travja.performances.api.Util;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performer extends Person {

    private List<Performance> currentPerformances = new ArrayList<>();
    private List<Performance> pastPerformances    = new ArrayList<>();

    public Performer(String name) {
        this(name, "example@example.com", null, "password");
    }

    public Performer(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public void addCurrentPerformance(Performance performance) {
        currentPerformances.add(performance);
    }

    public void addPastPerformance(Performance performance) {
        currentPerformances.remove(performance);
        pastPerformances.add(performance);
    }

}
