package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Objective {
    private Coordinate location;
    private boolean isReached;
    private String etiket;

    public Objective(Coordinate location, String etiket) {
        this.location = location;
        this.etiket = etiket;
        this.isReached=false;
    }
}