package com.pilates.app.model;

public enum UserRole {
    TRAINEE, TRAINER, NO_ROLE;


    public static UserRole fromString(final String value) {
        System.out.println("Searching user role by value: " + value);
        for (UserRole role : UserRole.values()) {

            if (role.toString().equalsIgnoreCase(value)) {
                return role;
            }
        }

        throw new RuntimeException("User role not found");

    }

}
