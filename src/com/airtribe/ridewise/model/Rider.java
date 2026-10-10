package com.airtribe.ridewise.model;

import com.airtribe.ridewise.util.Validator;

public class Rider {
    private final String id;
    private final String name;
    private int location;

    public Rider(String id, String name, int location) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider ID cannot be blank.");
        }
        Validator.validateName(name, "Rider");
        if (location < 0) {
            throw new IllegalArgumentException("Location cannot be negative.");
        }
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getLocation() { return location; }
    public void setLocation(int location) { this.location = location; }
}
