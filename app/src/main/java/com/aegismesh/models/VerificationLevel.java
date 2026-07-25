package com.aegismesh.models;

import java.io.Serializable;

public class VerificationLevel implements Serializable {
    private boolean phoneVerified;
    private boolean idVerified;
    private boolean faceMatchVerified;

    public VerificationLevel() {
        this.phoneVerified = true; // Required at signup as per comments
    }

    public boolean hasPhone() { return phoneVerified; }
    public boolean hasNationalId() { return idVerified; }
    public boolean hasFaceMatch() { return faceMatchVerified; }

    public void setIdVerified(boolean idVerified) { this.idVerified = idVerified; }
    public void setFaceMatchVerified(boolean faceMatchVerified) { this.faceMatchVerified = faceMatchVerified; }
}
