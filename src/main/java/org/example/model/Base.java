package org.example.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Base {
    private Coordinate location;

    public Base(Coordinate location) {
        this.location = location;
    }
}