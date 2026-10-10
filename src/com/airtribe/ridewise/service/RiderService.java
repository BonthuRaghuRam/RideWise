package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.util.IdGenerator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RiderService {
    private final Map<String, Rider> riders = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();

    public Rider registerRider(String name, int location) {
        String id = "R" + idGenerator.generateId();
        Rider rider = new Rider(id, name, location);
        riders.put(id, rider);
        return rider;
    }

    public Rider getRider(String id) {
        if (!riders.containsKey(id)) {
            throw new IllegalArgumentException("Rider not found with ID: " + id);
        }
        return riders.get(id);
    }
}
