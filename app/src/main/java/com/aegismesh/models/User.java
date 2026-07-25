package com.aegismesh.models;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {

    public String fullName;
    public MedicalProfile medicalProfile;
    public VerificationLevel verificationLevel;

    // Keep old fields for backward compatibility if needed, 
    // but ProfileActivity seems to use medicalProfile.
    private String age;
    private String allergies;
    private String chronicConditions;

    public User() {
        this.medicalProfile = new MedicalProfile();
        this.verificationLevel = new VerificationLevel();
    }

    public User(String fullName, String age, String allergies, String chronicConditions) {
        this.fullName = fullName;
        this.age = age;
        this.allergies = allergies;
        this.chronicConditions = chronicConditions;
        this.medicalProfile = new MedicalProfile("", 
            allergies != null ? allergies.split(",") : new String[0], 
            chronicConditions != null ? chronicConditions.split(",") : new String[0], 
            new String[0]);
        this.verificationLevel = new VerificationLevel();
    }

    public String getFullName() {
        return fullName;
    }

    public String getAge() {
        return age;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getChronicConditions() {
        return chronicConditions;
    }

    public JSONObject toProfileJsonObject() throws JSONException {
        JSONObject profileJson = new JSONObject();
        profileJson.put("age", age != null ? age : "");
        profileJson.put("allergies", allergies != null ? allergies : "");
        profileJson.put("chronic_conditions", chronicConditions != null ? chronicConditions : "");
        return profileJson;
    }
}
