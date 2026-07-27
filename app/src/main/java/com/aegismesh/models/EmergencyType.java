package com.aegismesh.models;

public enum EmergencyType {
<<<<<<< HEAD
    MEDICAL("Medical"),
    FIRE("Fire"),
    POLICE("Police"),
    GENERAL("General");
=======
    GENERAL("General Emergency"),
    MEDICAL("Medical Emergency"),
    FIRE("Fire Emergency"),
    POLICE("Police Emergency");
>>>>>>> origin/main

    private final String displayName;

    EmergencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
