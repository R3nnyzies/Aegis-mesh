package com.aegismesh.models;

public enum EmergencyType {
<<<<<<< HEAD
=======
<<<<<<< HEAD
    MEDICAL("Medical"),
    FIRE("Fire"),
    POLICE("Police"),
    GENERAL("General");
=======
>>>>>>> origin/main
    GENERAL("General Emergency"),
    MEDICAL("Medical Emergency"),
    FIRE("Fire Emergency"),
    POLICE("Police Emergency");
<<<<<<< HEAD
=======
>>>>>>> origin/main
>>>>>>> origin/main

    private final String displayName;

    EmergencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
