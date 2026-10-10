package com.airtribe.ridewise.util;

public class IdGenerator {
    private int count = 1;
    public synchronized int generateId() {
        return count++;
    }
}
