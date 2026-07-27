package com.aegismesh.models;

import java.io.Serializable;

public class VerificationLevel implements Serializable {
<<<<<<< HEAD
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
=======
    private static final long serialVersionUID = 1L;
    private boolean nationalIdVerified;
    private boolean faceMatchVerified;
    public VerificationLevel() { }
    public VerificationLevel(boolean nationalIdVerified, boolean faceMatchVerified) {
        this.nationalIdVerified = nationalIdVerified;
        this.faceMatchVerified = faceMatchVerified;
    }
    public boolean hasNationalId() { return nationalIdVerified; }
    public void setNationalIdVerified(boolean value) { nationalIdVerified = value; }
    public boolean hasFaceMatch() { return faceMatchVerified; }
    public void setFaceMatchVerified(boolean value) { faceMatchVerified = value; }
>>>>>>> origin/main
}
