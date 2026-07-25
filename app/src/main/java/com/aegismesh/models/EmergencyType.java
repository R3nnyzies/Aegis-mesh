package com.aegismesh.models;

public enum EmergencyType {
    MEDICAL("Medical"),
    FIRE("Fire"),
    POLICE("Police"),
    GENERAL("General");

    private final String displayName;

    EmergencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
