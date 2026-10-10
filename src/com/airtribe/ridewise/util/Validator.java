package com.airtribe.ridewise.util;

public final class Validator {

    private Validator() {
        // Prevent instantiation
    }

    public static void validateName(String name, String entityType) {
        if (name == null || name.trim().isEmpty() || !name.matches("^[a-zA-Z\\s]+$")) {
            throw new IllegalArgumentException("Invalid " + entityType + " name. Can only contain letters and spaces.");
        }
    }
}
