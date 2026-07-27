package com.aegismesh.models;

import java.io.Serializable;

/**
 * Represents a nearby person or professional who has accepted the emergency
 * call and is en route to the victim.
 */
public class Responder implements Serializable {

    private static final long serialVersionUID = 1L;

    public String displayName;
    public int trustScore;
    public int completedAssists;
    public int etaMinutes;
    public boolean isVerified;

    public Responder() {
    }

    public Responder(String displayName, int trustScore, int completedAssists, int etaMinutes, boolean isVerified) {
        this.displayName = displayName;
        this.trustScore = trustScore;
        this.completedAssists = completedAssists;
        this.etaMinutes = etaMinutes;
        this.isVerified = isVerified;
    }

    public String getName() { return displayName; }
    public int getTrustScore() { return trustScore; }
    public int getAssists() { return completedAssists; }
    public int getEtaMinutes() { return etaMinutes; }
    public boolean isVerified() { return isVerified; }
}
