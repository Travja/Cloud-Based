package me.travja.performances;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Performer extends Person {

    private List<Performance> currentPerformances = new ArrayList<>();
    private List<Performance> pastPerformances    = new ArrayList<>();

    public void addCurrentPerformance(Performance performance) {
        currentPerformances.add(performance);
    }

    public void addPastPerformance(Performance performance) {
        currentPerformances.remove(performance);
        pastPerformances.add(performance);
    }

}
