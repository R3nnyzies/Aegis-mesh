package com.aegismesh.models;

import java.io.Serializable;
<<<<<<< HEAD

=======
>>>>>>> origin/main
import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {

<<<<<<< HEAD
    private String fullName;
=======
    public String fullName;
    public MedicalProfile medicalProfile;
    public VerificationLevel verificationLevel;

    // Keep old fields for backward compatibility if needed, 
    // but ProfileActivity seems to use medicalProfile.
>>>>>>> origin/main
    private String age;
    private String allergies;
    private String chronicConditions;
    private MedicalProfile medicalProfile;
    private VerificationLevel verificationLevel;

<<<<<<< HEAD
=======
    public User() {
        this.medicalProfile = new MedicalProfile();
        this.verificationLevel = new VerificationLevel();
    }

>>>>>>> origin/main
    public User(String fullName, String age, String allergies, String chronicConditions) {
        this.fullName = fullName;
        this.age = age;
        this.allergies = allergies;
        this.chronicConditions = chronicConditions;
<<<<<<< HEAD
=======
        this.medicalProfile = new MedicalProfile("", 
            allergies != null ? allergies.split(",") : new String[0], 
            chronicConditions != null ? chronicConditions.split(",") : new String[0], 
            new String[0]);
        this.verificationLevel = new VerificationLevel();
>>>>>>> origin/main
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
>>>>>>> origin/main
    public MedicalProfile getMedicalProfile() {
        return medicalProfile;
    }

    public void setMedicalProfile(MedicalProfile medicalProfile) {
        this.medicalProfile = medicalProfile;
    }

    public VerificationLevel getVerificationLevel() {
        return verificationLevel;
    }

    public void setVerificationLevel(VerificationLevel verificationLevel) {
        this.verificationLevel = verificationLevel;
    }

    /**
     * Converts the medical profile into the nested JSON object required by the
     * FastAPI backend. Null fields are coalesced to empty strings so the key is
     * always present in the payload — org.json.JSONObject.put() silently omits
     * a key when given a null value, which would otherwise cause incomplete
     * profiles (e.g. a user who triggers SOS before finishing onboarding) to
     * send a payload missing required backend fields.
     */
<<<<<<< HEAD
=======
>>>>>>> origin/main
>>>>>>> origin/main
    public JSONObject toProfileJsonObject() throws JSONException {
        JSONObject profileJson = new JSONObject();
        profileJson.put("age", age != null ? age : "");
        profileJson.put("allergies", allergies != null ? allergies : "");
        profileJson.put("chronic_conditions", chronicConditions != null ? chronicConditions : "");
        return profileJson;
    }
}
